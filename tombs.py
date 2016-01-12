import copy
import random
from collections import Counter

'''

TOMBS - Python Version

Input example: [0,1,5] or 0,1,5
Places a Blade at (0,1)

Activating Thorn example: [0,1,2,1,2] or 0,1,2,1,2
Places a Thorn at (0,1) while removing target at (1,2)

Discard input example: 3
Discards an Ombra from hand

Quit input: [0]

Playing board (x,y):

 00 | 10 | 20
--------------
 01 | 11 | 21
--------------
 02 | 12 | 22

Tile types:
0 - Empty
1 - Tombstone
2 - Thorn (old name: priest)
3 - Ombra (old name: rogue)
4 - Magus (old name: mage)
5 - Blade (old name: warrior)

Tile owner:
0 - neutral (for empty tiles and tombstones)
1-n: Player 1-n

Win conditions:
1. Field-lock
2. Kill-count
3. Unit-count

To do: GUI

'''


class Tile:

    # ex: Tile(0, 1, 5, 1): Player 1's Blade at position (x,y) = (0,1)

    def __init__(self, x, y, type, owner):
        self.x = x
        self.y = y
        self.type = type
        self.owner = owner

    def __str__(self):
        return "(" + str(self.type) + " " + str(self.owner) + ")"


class Error(Exception):
    pass


class Deck:

    # nb: number of cards -> evenly distributed between the 4 character types

    def __init__(self, nb):
        self.nb = nb
        self.cards = []
        for x in range(nb//4):
            self.cards += [2, 3, 4, 5]

    def __str__(self):
        return str(self.cards)

    def shuffle(self):
        random.shuffle(self.cards)

    # take1: returns first card of deck, which is then removed from it

    def take1(self):
        drawn = self.cards[0]
        self.cards = self.cards[1:]
        return drawn


class Player:

    # index: player's identification
    # d: deck

    def __init__(self, index, d):
        self.index = index
        self.hand = []
        self.draw(d)
        self.draw(d)

    def draw(self, d):
        self.hand += [d.take1()]

    # undraw: in case of error in input -> rollback

    def undraw(self, d):
        d.cards = [self.hand[-1]] + d.cards
        self.hand = self.hand[0:-1]

    # play1: removes a card of unit u from hand

    def play1(self, u):
        if u in self.hand:
            self.hand.remove(u)
        else:
            raise Error("Card unavailable.")


class Game:

    def __init__(self, numP):

        self.numP = numP  # number of players
        self.currentP = 1  # current player
        self.board = [[Tile(i, j, 0, 0) for j in range(3)] for i in range(3)]
        # initialize an empty board
        self.turnCount = 1
        self.stuckCount = 0  # to check for Field-lock win condition

        try:
            if numP == 2:

                self.score = [0, 0]
                self.safeTurns = 3  # no violence during first 3 turns
                self.killWin = 8  # need 8 kills to win
                self.deck = Deck(32)  # deck of 32 cards
                self.lastDraw = self.deck.nb - 2*numP  # turn on which the last card is drawn
                self.deck.shuffle()
                self.players = [Player(1, self.deck), Player(2, self.deck)]  # initialize 2 players

            elif numP == 3:

                self.score = [0, 0, 0]
                self.safeTurns = 2
                self.killWin = 7
                self.deck = Deck(36)
                self.lastDraw = self.deck.nb - 2*numP
                self.deck.shuffle()
                self.players = [Player(1, self.deck), Player(2, self.deck), Player(3, self.deck)]

            elif numP == 4:

                self.score = [0, 0, 0, 0]
                self.safeTurns = 3
                self.killWin = 6
                self.deck = Deck(40)
                self.lastDraw = self.deck.nb - 2*numP
                self.deck.shuffle()
                self.players = [Player(1, self.deck), Player(2, self.deck),
                                Player(3, self.deck), Player(4, self.deck)]

            else:

                raise Error("Game is unsupported for " + str(numP) + " players.")

        except Error as msg:
            print(msg)
            raise SystemExit

    # printBoard: prints the board state and other info

    def printBoard(self, b):

        for j in range(3):
            print("\n")
            for i in range(3):
                print(b[int(i)][int(j)], end=" ")
        print("\n-----------------")

        print("Score:", self.score, "\n")

        '''

        # for testing purposes

        for x in self.players:
            print(x.hand)

        print(self.deck.cards, len(self.deck.cards))

        '''

    # btile: board x-coord y-coord -> list of tiles
    # returns tiles in the threat zones of a Blade at (x,y)

    def btile(self, b, x, y):
        lst = []
        if y < 2:
            lst = [b[x][y+1]] + lst  # down
        if x < 2:
            lst = [b[x+1][y]] + lst  # right
        if x > 0:
            lst = [b[x-1][y]] + lst  # left
        if y > 0:
            lst = [b[x][y-1]] + lst  # up
        return lst

    # mtile1: board x-coord y-coord -> list of tiles
    # returns adjacent diagonal tiles to a character at (x,y)

    def mtile1(self, b, x, y):
        lst = []
        if x < 2 and y < 2:
            lst = [b[x+1][y+1]] + lst  # dr
        if x > 0 and y < 2:
            lst = [b[x-1][y+1]] + lst  # dl
        if x < 2 and y > 0:
            lst = [b[x+1][y-1]] + lst  # tr
        if x > 0 and y > 0:
            lst = [b[x-1][y-1]] + lst  # tl
        return lst

    # mtile: board x-coord y-coord -> list of tiles
    # returns tiles in the threat zones of a Magus at (x,y)

    def mtile(self, b, x, y):
        lst = self.mtile1(b, x, y)
        if x == 0 and y == 0:
            lst += [b[2][2]]
        elif x == 2 and y == 0:
            lst += [b[0][2]]
        elif x == 0 and y == 2:
            lst = [b[2][0]] + lst
        elif x == 2 and y == 2:
            lst = [b[0][0]] + lst
        return lst

    # ttile: board x-coord y-coord -> list of tiles
    # returns tiles in the threat zones of a Thorn at (x,y)

    def ttile(self, b, x, y):
        return self.btile(b, x, y) + self.mtile1(b, x, y)

    # enemy: tile list-of-tiles unit -> boolean
    # returns whether there is a specific enemy unit to the given tile in the list of tiles

    def enemy(self, t, lst, u):
        truth = False
        for x in lst:
            if x.type == u and x.owner != t.owner:
                truth = True
        return truth

    # target: tile list-of-tiles -> boolean
    # returns whether there is an enemy to the given tile in the list of tiles

    def target(self, t, lst):
        truth = False
        for x in lst:
            if not (x.owner == t.owner or x.owner == 0):
                truth = True
        return truth

    # singletarget: tile1 tile2 -> boolean
    # returns whether tile2 is an enemy to tile1

    def singletarget(self, t1, t2):
        if not (t2.owner == t1.owner or t2.owner == 0):
            truth = True
        else:
            truth = False
        return truth

    # check1: board tile -> boolean
    # checks if the tile can be placed* on the board

    '''

    Checking process:

    1. check1
    2. place (maybe thornkill)
    3. check2
    4. movef (maybe killupdate)

    '''

    def check1(self, b, t):

        if self.turnCount == 1 and t.x == 1 and t.y == 1:
            truth = False  # turn 1: cannot place in middle tile

        elif self.turnCount <= self.safeTurns \
                and ((t.type == 5 and self.target(t, self.btile(b, t.x, t.y)))
                     or (t.type == 4 and self.target(t, self.mtile(b, t.x, t.y)))):
            truth = False  # no violence turns

        elif t.type == 2 and b[t.x][t.y].type == 1 and self.target(t, self.ttile(b, t.x, t.y)) \
                and self.turnCount > self.safeTurns and not (self.enemy(t, self.ttile(b, t.x, t.y), 2)):
            truth = True  # activated Thorn on tombstone with target to kill and no adjacent enemy Thorn

        elif t.type == 3 and self.target(t, [b[t.x][t.y]]) and b[t.x][t.y].type != 3 \
                and self.turnCount > self.safeTurns:
            truth = True  # Ombra assassinating target

        elif b[t.x][t.y].type == 0:
            truth = True  # normal placement on empty space

        else:
            truth = False

        return truth

    # thornkill: board tile x-coord y-coord -> board
    # updates the board by removing Thorn's target at (i, j)

    def thornkill(self, b, t, i, j):

        new = []

        for e in self.ttile(b, t.x, t.y):
            if (e.x == i and e.y == j) and (not (e.owner == t.owner or e.owner == 0)):
                new = copy.deepcopy(b)
                new[i][j] = Tile(i, j, 0, 0)
                new[t.x][t.y] = t

        if not new:
            raise Error("Invalid move.")

        return new

    # check2: board tile -> boolean
    # checks if the tile would be threatened by something after it is placed on the board

    def check2(self, b, t):
        if self.enemy(t, self.btile(b, t.x, t.y), 5):
            truth = False  # threatened by enemy Blade
        elif self.enemy(t, self.mtile(b, t.x, t.y), 4):
            truth = False  # threatened by enemy Magus
        else:
            truth = True
        return truth

    # maketomb: tile list-of-tiles -> list-of-tiles
    # checks for the tile's kills in the input list and returns a list of tombstone tiles

    def maketomb(self, t, lst):
        lst1 = []
        for e in lst:
            if self.singletarget(t, e):
                lst1 = [Tile(e.x, e.y, 1, 0)] + lst1
        return lst1

    # update: board list-of-tiles -> board
    # returns board updated with tiles from the list

    def update(self, b, lst):
        new = copy.deepcopy(b)
        for e in lst:
            new[e.x][e.y] = e
        return new

    # killupdate: board tile -> board
    # returns board after checking for tombstones created

    def killupdate(self, b, t):
        if t.type == 5:
            new = self.update(b, self.maketomb(t, self.btile(b, t.x, t.y)))
        elif t.type == 4:
            new = self.update(b, self.maketomb(t, self.mtile(b, t.x, t.y)))
        else:
            new = copy.deepcopy(b)
        return new

    # place: board tile x-coord y-coord -> board
    # returns board with the tile placed on it, and maybe call thornkill
    # (i,j) only matters when activating a Thorn
    # otherwise: (1,1) by default

    def place(self, b, t, i, j):
        if (t.type == 2 and b[t.x][t.y].type == 1) and self.check1(b, t):
            new = self.thornkill(b, t, i, j)
        elif self.check1(b, t):
            new = copy.deepcopy(b)
            new[t.x][t.y] = t
        else:
            raise Error("Cannot place here.")
        return new

    # movef: board tile x-coord y-coord -> board
    # purely functional one-turn action

    def movef(self, b, t, i, j):
        if self.check2(self.place(b, t, i, j), t):
            new = self.killupdate(self.place(b, t, i, j), t)
        else:
            raise Error("Zone is threatened.")
        return new

    # nodupe: list -> list
    # removes duplicates from the list

    def nodupe(self, lst):
        seen = set()
        seen_add = seen.add
        return [x for x in lst if not (x in seen or seen_add(x))]

    # choices: board player -> list-of-moves
    # returns all legal moves the player can make

    def choices(self, b, p):

        sol = []
        for u in self.nodupe(p.hand):
            for x in range(3):
                for y in range(3):

                    if u == 2 and b[int(x)][int(y)].type == 1:

                        for i in range(3):
                            for j in range(3):

                                try:
                                    self.movef(b, Tile(x, y, u, p.index), i, j)
                                except:
                                    pass
                                else:  # moves involving activating Thorn
                                    sol += [[x, y, u, i, j]]

                    else:

                        try:
                            self.movef(b, Tile(x, y, u, p.index), 1, 1)
                        except:
                            pass
                        else:  # normal moves
                            sol += [[x, y, u]]

        return sol

    # move: tile x-coord y-coord
    # non-functional version of movef, changes the board
    # example: move(Tile(0, 1, 2, 4), 1, 1)

    def move(self, t, i, j):

        temp = copy.deepcopy(self.board)

        self.board = self.movef(self.board, t, i, j)

        # score incrementation

        if t.type == 5:
            self.score[t.owner-1] += len(self.maketomb(t, self.btile(temp, t.x, t.y)))
        elif t.type == 4:
            self.score[t.owner-1] += len(self.maketomb(t, self.mtile(temp, t.x, t.y)))

    # hire: x-coord y-coord owner type
    # shortcut for move: normal character at (x,y)

    def hire(self, x, y, o, t):
        self.move(Tile(x, y, t, o), 1, 1)

    # drain: x-coord y-coord owner x-coord y-coord
    # shortcut for move: activating Thorn at (x,y) with target at (i,j)

    def drain(self, x, y, o, i, j):
        self.move(Tile(x, y, 2, o), i, j)

    # countUnits: board -> list-of-owners
    # returns list of owners who have the most units on the board

    def countUnits(self, b):

        lst = []
        for i in range(3):
            for j in range(3):
                lst += [b[i][j].owner]

        lst = list(filter((0).__ne__, lst))
        count = Counter(lst)
        freq = count.values()
        total = list(freq).count(max(freq))

        return [elem[0] for elem in count.most_common(total)]

    # compare: list-of-owners -> owner or list-of-owners
    # takes list from countUnits and compares the owners' scores if their unit count ties
    # returns winner(s)

    def compare(self, lst):

        if len(lst) == 1:
            winner = lst[0]

        else:
            kills = []
            for i in lst:
                for x in range(self.score[i-1]):
                    kills += [i]

            count = Counter(kills)
            freq = count.values()
            total = list(freq).count(max(freq))
            top = [elem[0] for elem in count.most_common(total)]

            if len(top) == 1:
                winner = top[0]
            else:
                winner = top

        return winner

    # main code for one turn's actions

    def turn(self):

        # (for testing purposes)
        # print("Possible moves: ", self.choices(self.board, self.players[self.currentP-1]))

        if not self.choices(self.board, self.players[self.currentP - 1]):

            discard = eval(input("No possible moves for Player " + str(self.currentP) +
                                 ", please discard a card.\nYour hand is: " +
                                 str(self.players[self.currentP-1].hand) + "\n"))

            self.players[self.currentP-1].play1(discard)
            self.stuckCount += 1

        else:

            try:
                command = list(eval(input("Show me your move, Player " +
                                          str(self.currentP) + "!\nYour hand is: " +
                                          str(self.players[self.currentP-1].hand) + "\n")))
            except:
                raise Error("Unknown command.")

            if len(command) == 1 and command[0] == 0:
                raise SystemExit

            elif len(command) == 3:
                self.hire(command[0], command[1], self.currentP, command[2])
                self.players[self.currentP-1].play1(command[2])

            elif len(command) == 5 and command[2] == 2:
                self.drain(command[0], command[1], self.currentP, command[3], command[4])
                self.players[self.currentP-1].play1(command[2])

            else:
                raise Error("Unknown command.")

            self.stuckCount = 0

            self.printBoard(self.board)

    # main code for the game's execution
    # includes the 3 win conditions
    # recursion of turns until a win condition is met (or an error occurs)

    def play(self):

        try:
            if self.turnCount <= self.lastDraw:
                self.players[self.currentP-1].draw(self.deck)

            self.turn()

            # win by kills

            if self.score[self.currentP-1] >= self.killWin:
                print("Player " + str(self.currentP) + " wins!")
                raise SystemExit

            # win by units

            if self.turnCount == self.deck.nb:
                win = self.compare(self.countUnits(self.board))

                if isinstance(win, list):
                    print("It's a tie between Players " + str(win) + "!")
                else:
                    print("Player " + str(win) + " wins!")
                raise SystemExit

            self.currentP += 1
            self.turnCount += 1

            if self.currentP > self.numP:
                self.currentP = 1

        except Error as msg:
            if self.turnCount <= self.lastDraw:
                self.players[self.currentP-1].undraw(self.deck)
            print(msg)

        # win by field lock

        if self.stuckCount == self.numP - 1:
            print("Player " + str(self.currentP) + " wins!")
            raise SystemExit

        self.play()

start = eval(input("Number of players:\n"))
Game(start).play()
