package game;

import java.util.Vector;

// index: player's identification

public class Player {
	
	private int index;
	private int score;
	private Vector<Integer> hand;
	
	public Player (int index) {
		this.index = index;
		hand = new Vector<Integer>();
		score = 0;
	}
	
	public int getIndex () {
		return index;
	}
	
	public int getScore () {
		return score;
	}
	
	public void setScore (int score) {
		this.score = score;
	}
	
	public void draw (Deck deck) {
		hand.add(deck.topDeck());
	}
	
	// undraw: in case of error in input -> rollback
	
	public void undraw (Deck deck) {
		deck.putBack(hand.remove(hand.size()-1));
	}
	
	// play1: removes a card of the specific unit from hand
	
	public void play1 (int unit) throws Exception {
		if (hand.contains(unit)) {
			hand.removeElement(unit);
		} else {
			throw new Exception("Card unavailable.");
		}
	}
	
	public Vector<Integer> getHand () {
		return hand;
	}
	
	// countUnits: counts how many units the player has on the board b
	
	public int countUnits (Tile[][] b) {
		int count = 0;
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (b[i][j].getOwner() == index) {
					count += 1;
				}
			}
		}
		
		return count;
	}
	
}
