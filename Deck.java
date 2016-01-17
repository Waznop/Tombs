package game;

import java.util.Stack;
import java.util.Arrays;
import java.util.Collections;

// nbr: number of cards -> evenly distributed between the 4 character types

public class Deck {
	
	private int nbr;
	private Stack<Integer> cards;
	
	public Deck (int nbr) {
		this.nbr = nbr;
		cards = new Stack<Integer>();
		
		for (int i = 0; i < nbr/4; i++) {
			cards.push(2);
			cards.push(3);
			cards.push(4);
			cards.push(5);
		}
		
	}
	
	public int getNbr () {
		return nbr;
	}
	
	public void shuffle () {
		Collections.shuffle(cards);
	}
	
	public int topDeck () {
		return cards.pop();
	}
	
	public void putBack (int c) {
		cards.push(c);
	}
	
	public String deckList () {
		return Arrays.toString(cards.toArray());
	}

}
