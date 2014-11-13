package uk.singlekey.repeata_freak;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class represents blocks that contains a number of squares.
 * Visually, blocks are the shapes that are moved around by the user.
 * 
 * @author Samuel O'Sullivan
 *
 */
public class Block {
	
	/**
	 * The coloured squares that a block contains
	 * The Integer represents a color from a resource
	 */
	private ArrayList<Integer> squares = new ArrayList<Integer>();
	
	/**
	 * The position of the block on the board (where it's left most square is positioned)
	 */
	private int boardPosition;
	
	/**
	 * Constructor for a block that sets its initial board position to -1
	 */
	public Block() {
		super();
		// Represents the block not being on the game board
		this.boardPosition = -1;
	}
	
	/**
	 * Returns the board position of the block
	 * @return the position of the left-most square of the block
	 */
	public int getBoardPosition() {
		return this.boardPosition;
	}
	
	/**
	 * Sets the position of the board on the board
	 * @param position - where the left-most square of the block is positioned
	 */
	public void setBoardPosition(int position) {
		this.boardPosition = position;
	}
	
	/**
	 * @return the amount of squares that the block contains
	 */
	public int getSize() {
		return this.squares.size();
	}
	
	/**
	 * Adds a new square to the block
	 * @param color - the color that the new square should be
	 */
	public void addSquare(int color) {
		this.squares.add(color);
	}
	
	/**
	 * Creates an iterator to go through the squares contained in the block
	 * @return an iterator of the array of squares
	 */
	public Iterator<Integer> create_iterator() {
		return this.squares.iterator();
	}
}