package game;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

/*

TOMBS - Java Version

Input example: 015
Places a Blade at (0,1)

Activating Thorn example: 01212
Places a Thorn at (0,1) while removing target at (1,2)

Discard input example: 3
Discards an Ombra from hand

Quit input: 0

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

*/

public class Tombs {
	
	private int numP; // number of players
	private Tile[][] board;
	
	/*
	For testing purposes:
	
	private Tile t00 = new Tile(0, 0, 3, 1);
	private Tile t01 = new Tile(0, 1, 3, 2);
	private Tile t02 = new Tile(0, 2, 3, 3);
	private Tile t10 = new Tile(1, 0, 3, 4);
	private Tile t11 = new Tile(1, 1, 3, 1);
	private Tile t12 = new Tile(1, 2, 3, 2);
	private Tile t20 = new Tile(2, 0, 0, 0);
	private Tile t21 = new Tile(2, 1, 0, 0);
	private Tile t22 = new Tile(2, 2, 0, 0);
	private Tile[][] board = {{t00, t01, t02}, {t10, t11, t12}, {t20, t21, t22}};
	*/
	
	private int currentP; // current player
	private int turnCount;
	private int stuckCount; // to check for Field-lock win condition
	private Player[] players;
	private Deck deck;
	private int lastDraw; // turn on which the last card is drawn
	private int killWin; // how many kills a player needs to win
	private int safeTurns; // no killing during first x turns
	private Scanner scan;
	
	public Tombs () {
		
		currentP = 1;
		turnCount = 1;
		stuckCount = 0;
		scan = new Scanner(System.in);
		
		System.out.println("Number of players:");
		numP = scan.nextInt();
		
		board = new Tile[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				board[i][j] = new Tile(i, j, 0, 0);
			}
		}
		
		players = new Player[numP];
		for (int i = 0; i < numP; i++) {
			players[i] = new Player(i+1);
		}
		
		deck = new Deck(4*numP + 24);
		lastDraw = deck.getNbr() - 2*numP;
		killWin = 10 - numP;
		
		if (numP == 2) {
			safeTurns = 3;
		} else {
			safeTurns = numP - 1;
		}
		
		deck.shuffle();
		
		for (Player p : players) {
			p.draw(deck);
			p.draw(deck);
		}
		
	}
	
	// btile: board x-coord y-coord -> list of tiles
	// returns tiles in the threat zones of a Blade at (x, y)
	// (horizontal and vertical adjacent tiles)
	
	public static Vector<Tile> btile (Tile[][] b, int x, int y) {
		Vector<Tile> lst = new Vector<Tile>();
		if (y < 2) {
			lst.add(0, b[x][y+1]); // down
		}
		if (x < 2) {
			lst.add(0, b[x+1][y]); // right
		}
		if (x > 0) {
			lst.add(0, b[x-1][y]); // left
		}
		if (y > 0) {
			lst.add(0, b[x][y-1]); // up
		}
		return lst;
	}
	
    // mtile1: board x-coord y-coord -> list of tiles
    // returns adjacent diagonal tiles to a character at (x,y)
	
	public static Vector<Tile> mtile1 (Tile[][] b, int x, int y) {
		Vector<Tile> lst = new Vector<Tile>();
		if (x < 2 && y < 2) {
			lst.add(0, b[x+1][y+1]); // down-right
		}
		if (x > 0 && y < 2) {
			lst.add(0, b[x-1][y+1]); // down-left
		}
		if (x < 2 && y > 0) {
			lst.add(0, b[x+1][y-1]); // top-right
		}
		if (x > 0 && y > 0) {
			lst.add(0, b[x-1][y-1]); // top-left
		}
		return lst;
	}
	
    // mtile: board x-coord y-coord -> list of tiles
    // returns tiles in the threat zones of a Magus at (x,y)
	
	public static Vector<Tile> mtile (Tile[][] b, int x, int y) {
		Vector<Tile> lst = mtile1(b, x, y);
		if (x == 0 && y == 0) {
			lst.add(b[2][2]);
		} else if (x == 2 && y == 0) {
			lst.add(b[0][2]);
		} else if (x == 0 && y == 2) {
			lst.add(0, b[2][0]);
		} else if (x == 2 && y == 2) {
			lst.add(0, b[0][0]);
		}
		return lst;
	}
	
    // ttile: board x-coord y-coord -> list of tiles
    // returns tiles in the threat zones of a Thorn at (x,y)
	
	public static Vector<Tile> ttile (Tile[][] b, int x, int y) {
		Vector<Tile> lst = btile(b, x, y);
		lst.addAll(mtile1(b, x, y));
		return lst;
	}
	
	// enemy: tile list-of-tiles unit -> boolean
    // returns whether there is a specific enemy unit to the given tile in the list of tiles
	
	public static boolean enemy (Tile t, Vector<Tile> lst, int unit) {
		boolean truth = false;
		for (Tile x : lst) {
			if (x.getType() == unit && x.getOwner() != t.getOwner()) {
				truth = true;
			}
		}
		return truth;
	}
	
    // target: tile list-of-tiles -> boolean
    // returns whether there is an enemy to the given tile in the list of tiles
	
	public static boolean target (Tile t, Vector<Tile> lst) {
		boolean truth = false;
		for (Tile x : lst) {
			if (x.getOwner() != t.getOwner() && x.getOwner() != 0) {
				truth = true;
			}
		}
		return truth;
	}
	
    // singletarget: tile1 tile2 -> boolean
    // returns whether tile2 is an enemy to tile1
	
	public static boolean singletarget (Tile t1, Tile t2) {
		boolean truth;
		if (t2.getOwner() != t1.getOwner() && t2.getOwner() != 0) {
			truth = true;
		} else {
			truth = false;
		}
		return truth;
	}
	
	/*
	
	Checking process:

    1. check1
    2. place (maybe thornkill)
    3. check2
    4. movef (maybe killupdate)
    
	*/
	
    // check1: board tile -> boolean
    // checks if the tile can be placed* on the board
	
	public boolean check1 (Tile[][] b, Tile t) {
		boolean truth;
		
		if (turnCount == 1 && t.getX() == 1 && t.getY() == 1) {
			truth = false; // turn 1: cannot place in middle tile
		} else if (turnCount <= safeTurns &&
				(t.getType() == 5 && target(t, btile(b, t.getX(), t.getY()))
				|| t.getType() == 4 && target(t, mtile(b, t.getX(), t.getY())))) {
			truth = false; // no kill turns
		} else if (t.getType() == 2 && b[t.getX()][t.getY()].getType() == 1
				&& target(t, ttile(b, t.getX(), t.getY()))
				&& turnCount > safeTurns && !enemy(t, ttile(b, t.getX(), t.getY()), 2)) {
			truth = true; // activated Thorn on tombstone with target to kill and no adjacent enemy Thorn
		} else if (t.getType() == 3 && b[t.getX()][t.getY()].getOwner() != t.getOwner()
				&& b[t.getX()][t.getY()].getOwner() != 0
				&& b[t.getX()][t.getY()].getType() != 3 && turnCount > safeTurns) {
			truth = true; // Ombra assassinating target
		} else if (b[t.getX()][t.getY()].getType() == 0) {
			truth = true; // normal placement on empty space
		} else {
			truth = false;
		}
		return truth;
	}
	
	public static Tile[][] deepCopy(Tile[][] b) {
		if (b == null) {
			return null;
		}
		
		final Tile[][] newB = new Tile[b.length][];
		for (int i = 0; i < b.length; i++) {
			newB[i] = Arrays.copyOf(b[i], b[i].length);
		}
		return newB;
	}
	
    // thornkill: board tile x-coord y-coord -> board
    // updates the board by removing Thorn's target at (i, j)
	
	public static Tile[][] thornkill (Tile[][] b, Tile t, int i, int j) throws Exception {
		Tile[][] brd = deepCopy(b);
		boolean hasTarget = false;
		
		for (Tile e : ttile(b, t.getX(), t.getY())) {
			if (e.getX() == i && e.getY() == j
					&& e.getOwner() != t.getOwner() && e.getOwner() != 0) {
				brd[i][j] = new Tile(i, j, 0, 0);
				brd[t.getX()][t.getY()] = t;
				hasTarget = true;
			}
		}
		
		if (hasTarget == false) {
			throw new Exception("Invalid move.");
		}
		
		return brd;
	}
	
    // check2: board tile -> boolean
    // checks if the tile would be threatened by something after it is placed on the board
	
	public static boolean check2 (Tile[][] b, Tile t) {
		boolean truth;
		
		if (enemy(t, btile(b, t.getX(), t.getY()), 5)) {
			truth = false;
		} else if (enemy(t, mtile(b, t.getX(), t.getY()), 4)) {
			truth = false;
		} else {
			truth = true;
		}
		
		return truth;
	}
	
    // maketomb: tile list-of-tiles -> list-of-tiles
    // checks for the tile's kills in the input list and returns a list of tombstone tiles
	
	public static Vector<Tile> maketomb (Tile t, Vector<Tile> lst) {
		Vector<Tile> lst1 = new Vector<Tile>();
		
		for (Tile e : lst) {
			if (singletarget(t, e)) {
				Tile newT = new Tile(e.getX(), e.getY(), 1, 0);
				lst1.add(0, newT);
			}
		}
		
		return lst1;
	}
	
    // update: board list-of-tiles -> board
    // returns board updated with tiles from the list
	
	public static Tile[][] update (Tile[][] b, Vector<Tile> lst) {
		Tile[][] brd = deepCopy(b);
		
		for (Tile e : lst) {
			brd[e.getX()][e.getY()] = e;
		}
		
		return brd;
	}
	
    // killupdate: board tile -> board
    // returns board after checking for tombstones created
	
	public static Tile[][] killupdate (Tile[][] b, Tile t) {
		Tile[][] brd;
		
		if (t.getType() == 5) {
			brd = update(b, maketomb(t, btile(b, t.getX(), t.getY())));
		} else if (t.getType() == 4) {
			brd = update(b, maketomb(t, mtile(b, t.getX(), t.getY())));
		} else {
			brd = deepCopy(b);
		}
		
		return brd;
	}
	
    // place: board tile x-coord y-coord -> board
    // returns board with the tile placed on it, and maybe call thornkill
    // (i,j) only matters when activating a Thorn
    // otherwise: (1,1) by default
	
	public Tile[][] place (Tile[][] b, Tile t, int i, int j) throws Exception {
		Tile[][] brd;
		
		if (t.getType() == 2 && b[t.getX()][t.getY()].getType() == 1 && check1(b, t)) {
			brd = thornkill(b, t, i, j);
		} else if (check1(b, t)) {
			brd = deepCopy(b);
			brd[t.getX()][t.getY()] = t;
		} else {
			throw new Exception("Cannot place here.");
		}
		
		return brd;
	}
	
    // movef: board tile x-coord y-coord -> board
    // purely functional one-turn action
	
	public Tile[][] movef (Tile[][] b, Tile t, int i, int j) throws Exception {
		Tile[][] brd;
		
		if (check2(place(b, t, i, j), t)) {
			brd = killupdate(place(b, t, i, j), t);
		} else {
			throw new Exception("Zone is threatened.");
		}
		
		return brd;
	}
	
    // nodupe: list -> list
    // removes duplicates from the list
	
	public static Vector<Integer> noDupe (Vector<Integer> v) {
		Vector<Integer> newV = new Vector<Integer>();
		Set<Integer> set = new HashSet<Integer>();
		set.addAll(v);
		newV.addAll(set);
		return newV;
	}
	
    // choices: board player -> list-of-moves
    // returns all legal moves the player can make
	
	public Vector<Vector<Integer>> choices (Tile[][] b, Player p) {
		Vector<Vector<Integer>> sol = new Vector<Vector<Integer>>();
		
		for (int u : noDupe(p.getHand())) {
			for (int x = 0; x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					
					if (u == 2 && b[x][y].getType() == 1) {
						
						for (int i = 0; i < 3; i++) {
							for (int j = 0; j < 3; j++) {
								
								boolean legit = true;
								try {
									Tile newT = new Tile(x, y, u, p.getIndex());
									movef(b, newT, i, j);
								} catch (Exception e) {
									legit = false;
								}
								
								if (legit) {
									
									// moves involving activating Thorn
									
									Vector<Integer> newMove = new Vector<Integer>();
									newMove.add(x);
									newMove.add(y);
									newMove.add(u);
									newMove.add(i);
									newMove.add(j);
									sol.add(newMove);
								}
							}
						}
					} else {
						
						boolean legit = true;
						try {
							Tile newT = new Tile(x, y, u, p.getIndex());
							movef(b, newT, 1, 1);
						} catch (Exception e) {
							legit = false;
						}
						
						if (legit) {
							
							// normal moves
							
							Vector<Integer> newMove = new Vector<Integer>();
							newMove.add(x);
							newMove.add(y);
							newMove.add(u);
							sol.add(newMove);
						}
					}
					
				}
			}
		}
		
		return sol;
	}
	
    // move: tile x-coord y-coord
    // non-functional version of movef, changes the board
	
	public void move (Tile t, int i, int j) throws Exception {
		Tile[][] temp = deepCopy(board);
		board = movef(board, t, i, j);
		
		// score incrementation
		
		if (t.getType() == 5) {
			int scr = players[t.getOwner()-1].getScore();
			scr += maketomb(t, btile(temp, t.getX(), t.getY())).size();
			players[t.getOwner()-1].setScore(scr);
		} else if (t.getType() == 4) {
			int scr = players[t.getOwner()-1].getScore();
			scr += maketomb(t, mtile(temp, t.getX(), t.getY())).size();
			players[t.getOwner()-1].setScore(scr);
		}
		
	}
	
    // hire: x-coord y-coord owner type
    // shortcut for move: normal character at (x,y)
	
	public void hire (int x, int y, int o, int t) throws Exception {
		Tile newT = new Tile(x, y, t, o);
		move(newT, 1, 1);
	}
	
    // drain: x-coord y-coord owner x-coord y-coord
    // shortcut for move: activating Thorn at (x,y) with target at (i,j)
	
	public void drain (int x, int y, int o, int i, int j) throws Exception {
		Tile newT = new Tile(x, y, 2, o);
		move(newT, i, j);
	}
	
    // compare: list-of-players -> list-of-players
    // compares the players' units on an ending board, then their scores
	
	public Vector<Player> compare () {
		Vector<Player> winner = new Vector<Player>();
		Vector<Player> unitWinner = new Vector<Player>();
		
		int maxUnits = 0;
		for (Player p : players) {
			if (p.countUnits(board) > maxUnits) {
				maxUnits = p.countUnits(board);
			}
		}
		
		for (Player p : players) {
			if (p.countUnits(board) == maxUnits) {
				unitWinner.add(p);
			}
		}
		
		if (unitWinner.size() == 1) {
			winner = unitWinner;
		} else {
			int maxScore = 0;
			for (Player p : unitWinner) {
				if (p.getScore() > maxScore) {
					maxScore = p.getScore();
				}
			}
			
			for (Player p : unitWinner) {
				if (p.getScore() == maxScore) {
					winner.add(p);
				}
			}
		}
		
		return winner;
	}
	
	// printBoard: prints the board state and other info
	
	public void printBoard (Tile[][] b) {
		for (int j = 0; j < 3; j++) {
			System.out.print("\n");
			for (int i = 0; i < 3; i++) {
				System.out.print(b[i][j] + " ");
			}
		}
		System.out.println("\n-----------------");
		
		Vector<Integer> scores = new Vector<Integer>();
		for (Player p : players) {
			scores.add(p.getScore());
		}
		System.out.println("Score: " + scores);
		
		/*
		for (Player p : players) {
			System.out.println("Player" + p.getIndex() + "'s hand: " + p.getHand());
		}
		
		System.out.println("Deck: " + deck.deckList());
		*/
	}
	
	// main code for one turn's actions
	
	public void turn () throws Exception {
		
		printBoard(board);
		
		// for testing purposes
		// System.out.println("Moves: " + choices(board, players[currentP-1]));
		
		if (choices(board, players[currentP-1]).isEmpty()) {
			
			System.out.println("No possible moves for Player" + currentP + 
					", please discard a card.\nYour hand is: " + players[currentP-1].getHand());
			int discard = scan.nextInt();
			players[currentP-1].play1(discard);
			stuckCount += 1;
			
		} else {
				
			System.out.println("Show me your move, Player" + currentP +
					"!\nYour hand is: " + players[currentP-1].getHand());
			String commandString = scan.next();
			Vector<Integer> command = new Vector<Integer>();
			char[] commandChars = commandString.toCharArray();
			
			for (char c : commandChars) {
				command.add(Character.getNumericValue(c));
			}
			
			if (command.size() == 1 && command.firstElement() == 0) {
				System.exit(0);
			} else if (command.size() == 3) {
				hire(command.elementAt(0), command.elementAt(1), currentP, command.elementAt(2));
				players[currentP-1].play1(command.elementAt(2));
			} else if (command.size() == 5 && command.elementAt(2) == 2) {
				drain(command.elementAt(0), command.elementAt(1), currentP, 
						command.elementAt(3), command.elementAt(4));
				players[currentP-1].play1(command.elementAt(2));
			} else {
				throw new Exception("Unknown command.");
			}
			
			stuckCount = 0;
		}
	}
	
	// main code for the game's execution
	// includes the 3 win conditions
	// recursion of turns until a win condition is met (or an error occurs)
	
	public void play () {
		
		try {
			
			if (turnCount <= lastDraw) {
				players[currentP-1].draw(deck);
			}
			
			turn();
			
			// win by kills
			
			if (players[currentP-1].getScore() >= killWin) {
				System.out.println("Player" + currentP + " wins!");
				System.exit(0);
			}
			
			// win by units
			
			if (turnCount == deck.getNbr()) {
				Vector<Player> win = compare();
				
				if (win.size() == 1) {
					System.out.println("Player" + win.firstElement().getIndex() + "wins!");
				} else {
					Vector<Integer> winners = new Vector<Integer>();
					for (Player p : win) {
						winners.add(p.getIndex());
					}
					System.out.println("It's a tie between Players " + winners + "!");
				}
				
				System.exit(0);
			}
			
			turnCount += 1;
			currentP += 1;
			
			if (currentP > numP) {
				currentP = 1;
			}
			
		} catch (Exception e) {
			
			if (turnCount <= lastDraw) {
				players[currentP-1].undraw(deck);
			}
			System.out.println(e.getMessage());
		}
		
		// win by field lock
		
		if (stuckCount == numP-1) {
			System.out.println("Player" + currentP + " wins!");
			System.exit(0);
		}
		
		play();
	}
	
	public static void main (String[] args) {
		Tombs game = new Tombs();
		game.play();
	}
	
}
