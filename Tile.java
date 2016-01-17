package game;

// ex: Tile(0, 1, 5, 1): Player 1's Blade at position (x,y) = (0,1)

public class Tile {
	
	private int x;
	private int y;
	private int type;
	private int owner;
	
	public Tile (int x, int y, int type, int owner) {
		this.x = x;
		this.y = y;
		this.type = type;
		this.owner = owner;
	}
	
	public int getX () {
		return x;
	}
	
	public int getY () {
		return y;
	}
	
	public int getType () {
		return type;
	}
	
	public int getOwner () {
		return owner;
	}
	
	public String toString () {
		return ("(" + type + " " + owner + ")");
	}
	
}
