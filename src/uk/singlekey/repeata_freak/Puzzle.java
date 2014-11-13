package uk.singlekey.repeata_freak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import uk.ac.surrey.so00076.repeater.R;

/**
 * Controls the entire puzzle, including the maths and algorithms that make it work,
 * as well as methods that would allow an android-specific class to interpret the puzzle.
 * 
 * @author Samuel O'Sullivan
 */
public class Puzzle {
	
	/**
	 * A list of the colors that are available to use by the puzzle
	 */
	private ArrayList<Integer> colors;
	
	/**
	 * The length of the entire game board
	 */
	private int length;
	
	/**
	 * The length of a single section (the parts of the puzzle that must be identical)
	 */
	private int sectionLength;
	
	/**
	 * Represents every square on the game board
	 */
	private ArrayList<Integer> allSquares;
	
	/**
	 * A list of all of the blocks in the puzzle. Once the puzzle is created, this list 
	 * doesn't change
	 */
	private ArrayList<Block> allBlocks;
	
	/**
	 * A list of the blocks held in the storageArea (will be a sublist of allBlocks)
	 */
	private ArrayList<Block> storageArea;
	
	/**
	 * Constructor for Puzzle class.
	 * Takes a difficulty and creates a random puzzle.
	 * @param difficulty - determines the length of the puzzle
	 */
	public Puzzle(int difficulty) {
		
		// Make the length 6 times the difficulty
		length = difficulty * 6;
		
		// Set up an ArrayList of colors, using resources, so that we can easily pick them randomly
		colors = new ArrayList<Integer>();
		colors.add(R.color.red);
		colors.add(R.color.green);
		colors.add(R.color.blue);
		
		// Initialise ArrayLists for allBlocks and storageArea
		allBlocks = new ArrayList<Block>();
		storageArea = new ArrayList<Block>();
		
		// Randomly allocate a size that all sections will be
		calculateSectionLength();
		
		// Create the new puzzle fully solved (fill AllSquares)
		createPuzzle();
		
		// Split allSquares up into blocks
		createBlocks();
		
		// Shuffle all of the blocks in the storageArea (so they're not already in the right order)
		Collections.shuffle(storageArea);
		
		// Make each square of the game board null, ready for the user to play
		nullPuzzle();
	}
	
	/**
	 * Calculates the possible lengths of a section (given they must all be the same length) 
	 * in the puzzle, then chooses one at random.
	 * The sectionLength is measured by the number of squares it has.
	 */
	private void calculateSectionLength() {
		// We should randomly allocate a size that all sections will be.
		// First find all factors of the 'length', not including 1 or itself.
		// We also want i to be at most 12, as sections longer than this would be too long.
		ArrayList<Integer> factors = new ArrayList<Integer>();
		for(int i = 2; i <= length/2 && i <=12; i++) {
			// If i is a factor, add it to the list of factors
			if (length % i == 0) {
				factors.add(i);
			}
		}
		// Then choose a random factor from the list to be the size of the section
		int randomIndex = (int) (Math.random()*(factors.size()));
		this.sectionLength = factors.get(randomIndex);
	}
	
	/**
	 * Creates a puzzle fully solved, by creating a random section, then duplicating this to
	 * form the whole puzzle, ready to be split up into blocks.
	 */
	private void createPuzzle() {
		// Create an ArrayList representing a single section of the puzzle
		ArrayList<Integer> section = new ArrayList<Integer>();
		
		// Fill the section (up to sectionLength) with random colors from the colors ArrayList
		for (int i = 0; i < sectionLength; i++) {
			// Create a random index
			int randomIndex = (int) (Math.random()*(colors.size()));
			// Keep creating new random i until the random index is not the same as the one of the
			// previous block (so that a color never repeats).
			while (i >= 1 && colors.get(randomIndex) == section.get(i-1)) {
				randomIndex = (int) (Math.random()*(colors.size()));
			}
			// Add the random color to the section
			section.add(colors.get(randomIndex));
		}
		
		// Initialise allSquares, which holds all of the squares on the game board
		allSquares = new ArrayList<Integer>();
		
		// Repeatedly add the same section to allSquares, up to the amount of sections we have
		// calculated to be included in the puzzle
		for(int i=0; i < length/sectionLength; i++) {
			allSquares.addAll(section);
		}
	}
	
	/**
	 * Splits the puzzle up into blocks of random length
	 */
	private void createBlocks() {
		// The start of the current block
		int i = 0;
		// The end of the current block
		int j = 0;
		
		// Go through allSquares until we are three from the end
		while (i<allSquares.size()-3) {
			/*
			 * j represents the end position of the block we are currently creating, therefore
			 * this needs to be a random number that is:
			 * 		At least 1 ahead of i
			 * 		Less than half of allSquares entire length, ahead of i
			 * 		Less than 9 ahead of i; to stop blocks overflowing the screen - not the best way but will do
			 * 		Less than the size of allSquares
			 */
			j = getRandomJ(i);
			// Create a block and fill it up with j-i number of squares
			Block block = new Block();
			while (i<=j) {
				block.addSquare(allSquares.get(i));
				i++;
			}
			// Add this block to the puzzle
			allBlocks.add(block);
			
			// Note that i is now equal to j, so the next loop will continue through the array AFTER
			// the last block that was created
		}
		// Once we are within 3 of the end (while loop finished), fill the last block with the remaining elements
		if (i != allSquares.size()) {
			Block block = new Block();
			while (i < allSquares.size()) {
				block.addSquare(allSquares.get(i));
				i++;
			}
			allBlocks.add(block);
		}
		// The storageArea should be filled with all of these blocks
		storageArea.addAll(allBlocks);
	}
	
	/**
	 * Method to generate a random number j based on the following parameters:
	 * 		Must be at least 1 ahead of i
	 * 		Must be less than half of allSquares entire length, ahead of i
	 * 		Must be less than 9 ahead of i
	 * 		Must be less than the size of allSquares
	 * @param i
	 * @return random int j
	 */
	private int getRandomJ(int i) {
		// j must be at least 1 ahead of i
		int lowerBound = i + 1;
		
		// j must be smaller than  i + allSquares.size()/2  AND  allSquares.size()  AND  i+9
		int upperBound = Math.min(i+allSquares.size()/2, Math.min(allSquares.size(),  i+9));
		
		// Subtract the lowerBound from the upperBound
		int difference = upperBound - lowerBound;
		
		// Find a random number and add this to the lowerBound
		int randomNumber = (int) (Math.random()*(difference));
		int j = lowerBound + randomNumber;
		
		return j;
	}
	
	/**
	 * Set all of allSquares to null.
	 * We don't want to simply empty the array as empty squares will also need to be
	 * represented (as null)
	 */
	private void nullPuzzle() {
		for(int i = 0; i < this.length; i++) {
			allSquares.set(i, null);
		}
	}
	
	/**
	 * Check if the puzzle has been solved by checking if each section is the same
	 * @return true if puzzle is solved, false otherwise
	 */
	public boolean isSolved() {
		
		/*
		 * If the storageArea isn't empty (not all of the blocks have been moved to the game board),
		 * then the puzzle definitely hasn't been solved, so return false.
		 */
		if (!storageArea.isEmpty()) {
			return false;
		}
		else {
			/* 
			 * Go through each index of each section, checking they are identical
			 * Here i represents which square of each section we are currently comparing,
			 * and j represents the section we are on
			 * 
			 * For example, we check that the first square of every section is the same, then the
			 * second square of every section and so on.
			 */
			for (int i = 0; i < sectionLength; i++) {
				for (int j = 0; j < (allSquares.size()/sectionLength)-1; j++) {
					
					// Get the value of the square in section j and i positions through that section
					int a = allSquares.get((j)*sectionLength+i);
					// Get the value for the same i in the next section, so that we can compare
					int b = allSquares.get((j+1)*sectionLength+i);
					
					// If any i in any two sections is not equal, return false as the puzzle isn't solved
					if (a != b) {
						return false;
					}
				}
			}
			// If we have checked through all (i)s of all sections and found nothing wrong, return true as
			// the puzzle has been solved.
			return true;
		}
	}
	
	/**
	 * Method called when a user adds a block to the game board.
	 * Whether or not there is space for the block should have already been checked
	 * by the caller.
	 * @param block - the block being moved
	 * @param position - the position on the board the block should be moved to
	 */
	public void addBlockToBoard(Block block, int position) {
		// If the block is already on the board, remove it
		if (block.getBoardPosition() != -1) {
			// Set allSquares to null in all of the squares that the block previously occupied
			for (int i = block.getBoardPosition(); i < block.getBoardPosition()+block.getSize(); i++) {
				allSquares.set(i, null);
			}
			// Make sure the block also knows that it is no longer on the board
			block.setBoardPosition(-1);
		}
		// If the block was in the storage area, remove it
		else {
			this.storageArea.remove(block);
		}
		
		// Create an iterator to find the squares contained in the block
		Iterator<Integer> blockContents = block.create_iterator();
		// Add these squares to allSquares (the game board) in the appropriate places
		for(int i = position; i < position + block.getSize(); i++) {
			allSquares.set(i, blockContents.next());
		}
		// Make sure the block knows its new position
		block.setBoardPosition(position);
	}
	
	/**
	 * Moves a block from the game board to the storage area.
	 * @param block - the block to be moved
	 */
	public void removeBlockFromBoard(Block block) {
		if (block.getBoardPosition() != -1) {
			// Set the appropriate allSquares values to null
			for (int i = block.getBoardPosition(); i < block.getBoardPosition()+block.getSize(); i++) {
				allSquares.set(i, null);
			}
			// Add the block to the storage area
			this.storageArea.add(block);
			// Tell the block it is no longer on the board
			block.setBoardPosition(-1);
		}
	}
	
	/** 
	 * Check that there is space to move a block to the board in the position it wants 
	 * to be moved to.
	 * If the block being moved is moving partially over itself and only itself, then isSpace 
	 * will return true, so that a block can be moved by only a few blocks if the user desires.
	 * 
	 * @param block - the block being moved
	 * @param position - the position the block is being moved to
	 * @return true if there is space, false otherwise
	 */
	public boolean isSpace(Block block, int position) {
		// Go through each position (i) that the block would cover once moved
		for (int i = position; i < position + block.getSize(); i++) {
			// If we have gone over the edge of the puzzle, then there is definitely not enough
			// space, so return false
			if (i >= allSquares.size()) {
				return false;
			}
			// If the square has something in it, check if this something is part of the block
			// being moved
			int prevPosition = block.getBoardPosition();
			if (allSquares.get(i) != null) {
				// If it IS part of the block that's being moved, do nothing
				if (prevPosition != -1 && i >= prevPosition && i < prevPosition + block.getSize()) {
					// do nothing
				}
				// Otherwise, return false, as we are trying to overlap over another block
				else {
					return false;
				}
			}
		}
		// If we have got this far (no squares over the edge and all squares either null or part
		// of its previous state), return true.
		return true;
	}
	
	/**
	 * Get the block that occupies a particular square (position) on the board
	 * @param position - the position on the board we are looking at
	 * @return - the block that occupies the position
	 */
	public Block getBlockFromBoard(int position) {
		// Go through all of the blocks, returning the one that covers the position
		for (Block block : allBlocks) {
			if (block.getBoardPosition() != -1 && 
					position >= block.getBoardPosition() && 
					position < block.getBoardPosition() + block.getSize()) {
				return block;
			}
		}
		// If no blocks are found, return null
		return null;
	}
	
	/**
	 * Get a block from the storage area given its position
	 * @param position - its vertical position
	 * @return the block in that position
	 */
	public Block getBlockFromStorage(int position) {
		return this.storageArea.get(position);
	}
	
	/**
	 * Return the storage position of a block
	 * @param block - the block to find
	 * @return - the vertical position of the block in storage
	 */
	public int getBlockStoragePosition(Block block) {
		return this.storageArea.indexOf(block);
	}
	
	/**
	 * Get the section length that we have calculated for the puzzle
	 * @return the section length
	 */
	public int getSectionLength() {
		return this.sectionLength;
	}
	
	/**
	 * Get the current amount of blocks in the storage area
	 * @return - the size of storageArea
	 */
	public int getStorageSize() {
		return this.storageArea.size();
	}
	
	/**
	 * Create an iterator to go through the squares on the game board
	 * @return - iterator of allSquares
	 */
	public Iterator<Integer> create_allSquares_iterator() {
		return this.allSquares.iterator();
	}
	
	/**
	 * Create an iterator to go through the blocks in the storage area
	 * @return - iterator of storageArea
	 */
	public Iterator<Block> create_storage_iterator() {
		return this.storageArea.iterator();
	}
}