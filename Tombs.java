package game;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Tombs {
	
	private int numP;
	private Tile[][] board;
	private int currentP;
	private int turnCount;
	private int stuckCount;
	private Player[] players;
	private Deck deck;
	private int lastDraw;
	private int killWin;
	private int safeTurns;
	
	public Tombs (int numP) {
		
		this.numP = numP;
		currentP = 1;
		turnCount = 1;
		stuckCount = 0;
		
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
	
	public static Vector<Tile> btile (Tile[][] b, int x, int y) {
		Vector<Tile> lst = new Vector<Tile>();
		if (y < 2) {
			lst.add(0, b[x][y+1]);
		}
		if (x < 2) {
			lst.add(0, b[x+1][y]);
		}
		if (x > 0) {
			lst.add(0, b[x-1][y]);
		}
		if (y > 0) {
			lst.add(0, b[x][y-1]);
		}
		return lst;
	}
	
	public static Vector<Tile> mtile1 (Tile[][] b, int x, int y) {
		Vector<Tile> lst = new Vector<Tile>();
		if (x < 2 && y < 2) {
			lst.add(0, b[x+1][y+1]);
		}
		if (x > 0 && y < 2) {
			lst.add(0, b[x-1][y+1]);
		}
		if (x < 2 && y > 0) {
			lst.add(0, b[x+1][y-1]);
		}
		if (x > 0 && y > 0) {
			lst.add(0, b[x-1][y-1]);
		}
		return lst;
	}
	
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
	
	public static Vector<Tile> ttile (Tile[][] b, int x, int y) {
		Vector<Tile> lst = btile(b, x, y);
		lst.addAll(mtile1(b, x, y));
		return lst;
	}
	
	public static boolean enemy (Tile t, Vector<Tile> lst, int unit) {
		boolean truth = false;
		for (Tile x : lst) {
			if (x.getType() == unit && x.getOwner() != t.getOwner()) {
				truth = true;
			}
		}
		return truth;
	}
	
	public static boolean target (Tile t, Vector<Tile> lst) {
		boolean truth = false;
		for (Tile x : lst) {
			if (x.getOwner() != t.getOwner() && x.getOwner() != 0) {
				truth = true;
			}
		}
		return truth;
	}
	
	public static boolean singletarget (Tile t1, Tile t2) {
		boolean truth;
		if (t2.getOwner() != t1.getOwner() && t2.getOwner() != 0) {
			truth = true;
		} else {
			truth = false;
		}
		return truth;
	}
	
	public boolean check1 (Tile[][] b, Tile t) {
		boolean truth;
		
		if (turnCount == 1 && t.getX() == 1 && t.getY() == 1) {
			truth = false;
		} else if (turnCount <= safeTurns &&
				(t.getType() == 5 && target(t, btile(b, t.getX(), t.getY()))
				|| t.getType() == 4 && target(t, mtile(b, t.getX(), t.getY())))) {
			truth = false;
		} else if (t.getType() == 2 && b[t.getX()][t.getY()].getType() == 1
				&& target(t, ttile(b, t.getX(), t.getY()))
				&& turnCount > safeTurns && !enemy(t, ttile(b, t.getX(), t.getY()), 2)) {
			truth = true;
		} else if (t.getType() == 3 && b[t.getX()][t.getY()].getOwner() != t.getOwner()
				&& b[t.getX()][t.getY()].getOwner() != 0
				&& b[t.getY()][t.getY()].getType() != 3 && turnCount > safeTurns) {
			truth = true;
		} else if (b[t.getX()][t.getY()].getType() == 0) {
			truth = true;
		} else {
			truth = false;
		}
		return truth;
	}
	
	public static Tile[][] thornkill (Tile[][] b, Tile t, int i, int j) throws Exception {
		Tile[][] brd = b;
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
	
	public static Tile[][] update (Tile[][] b, Vector<Tile> lst) {
		Tile[][] brd = b;
		
		for (Tile e : lst) {
			brd[e.getX()][e.getY()] = e;
		}
		
		return brd;
	}
	
	public static Tile[][] killupdate (Tile[][] b, Tile t) {
		Tile[][] brd;
		
		if (t.getType() == 5) {
			brd = update(b, maketomb(t, btile(b, t.getX(), t.getY())));
		} else if (t.getType() == 4) {
			brd = update(b, maketomb(t, mtile(b, t.getX(), t.getY())));
		} else {
			brd = b;
		}
		
		return brd;
	}
	
	public Tile[][] place (Tile[][] b, Tile t, int i, int j) throws Exception {
		Tile[][] brd;
		
		if (t.getType() == 2 && b[t.getX()][t.getY()].getType() == 1 && check1(b, t)) {
			brd = thornkill(b, t, i, j);
		} else if (check1(b, t)) {
			brd = b;
			brd[t.getX()][t.getY()] = t;
		} else {
			throw new Exception("Cannot place here.");
		}
		
		return brd;
	}
	
	public Tile[][] movef (Tile[][] b, Tile t, int i, int j) throws Exception {
		Tile[][] brd;
		
		if (check2(place(b, t, i, j), t)) {
			brd = killupdate(place(b, t, i, j), t);
		} else {
			throw new Exception("Zone is threatened.");
		}
		
		return brd;
	}
	
	/*
	public static Vector<Integer> noDupe (Vector<Integer> v) {
		Vector<Integer> newV = v;
		
		for (int i = 0; i < newV.size(); i++) {
			for (int j = 0; i < newV.size(); j++) {
				if (i != j) {
					if (newV.elementAt(i).equals(newV.elementAt(j))) {
						newV.removeElementAt(j);
					}
				}
			}
		}
		
		return newV;
	}
	*/
	
	public static Vector<Integer> noDupe (Vector<Integer> v) {
		Vector<Integer> newV = new Vector<Integer>();
		Set<Integer> set = new HashSet<Integer>();
		set.addAll(v);
		newV.addAll(set);
		return newV;
	}
	
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
	
	public void move (Tile t, int i, int j) throws Exception {
		Tile[][] temp = board;
		board = movef(board, t, i, j);
		
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
	
	public void hire (int x, int y, int o, int t) throws Exception {
		Tile newT = new Tile(x, y, t, o);
		move(newT, 1, 1);
	}
	
	public void drain (int x, int y, int o, int i, int j) throws Exception {
		Tile newT = new Tile(x, y, 2, o);
		move(newT, i, j);
	}
	
	public Vector<Player> compare () {
		Vector<Player> winner = new Vector<Player>();
		Vector<Player> unitWinner = new Vector<Player>();
		
		int maxUnits = 0;
		for (Player p : players) {
			if (p.CountUnits(board) > maxUnits) {
				maxUnits = p.CountUnits(board);
			}
		}
		
		for (Player p : players) {
			if (p.CountUnits(board) == maxUnits) {
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
		
		for (Player p : players) {
			System.out.println("Player" + p.getIndex() + "'s hand: " + p.getHand());
		}
		
		System.out.println("Deck: " + deck.deckList());
		System.out.println("Cards remaining: " + deck.deckList().length());
	}
	
	public void turn () throws Exception {
		System.out.println("Moves: " + choices(board, players[currentP-1]));
		
		if (choices(board, players[currentP-1]).isEmpty()) {
			
			System.out.println("No possible moves for Player" + currentP + 
					", please discard a card.\nYour hand is: " + players[currentP-1].getHand());
			Scanner scan = new Scanner(System.in);
			int discard = scan.nextInt();
			players[currentP-1].play1(discard);
			scan.close();
			stuckCount += 1;
			
		} else {
				
			System.out.println("Show me your move, Player" + currentP +
					"!\nYour hand is: " + players[currentP-1].getHand());
			Scanner scan = new Scanner(System.in);
			String commandString = scan.next();
			Vector<Integer> command = new Vector<Integer>();
			char[] commandChars = commandString.toCharArray();
			scan.close();
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
			printBoard(board);
		}
	}
	
	public void play () {
		
		try {
			
			if (turnCount <= lastDraw) {
				players[currentP-1].draw(deck);
			}
			
			turn();
			
			if (players[currentP-1].getScore() >= killWin) {
				System.out.println("Player" + currentP + " wins!");
				System.exit(0);
			}
			
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
		
		if (stuckCount == numP-1) {
			System.out.println("Player" + currentP + " wins!");
			System.exit(0);
		}
		
		play();
	}
	
	public static void main (String[] args) {
		System.out.println("Number of players:");
		Scanner scan = new Scanner(System.in);
		int nbr = scan.nextInt();
		Tombs game = new Tombs(nbr);
		scan.close();
		game.play();
	}
	
}
