package uk.singlekey.repeata_freak;

import java.util.Iterator;

import uk.ac.surrey.so00076.repeater.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * The main class used when displaying a puzzle, this class gets information from an
 * instance of puzzle and uses this to draw it
 * 
 * @author Samuel O'Sullivan
 */
public class PuzzleView extends View {
	
	// The amount of time the user has left (in timed mode)
	private int timeCounter = 30;
	
	// The amount of puzzles the user has solved (in timed mode)
	private int solvedCounter = 0;
	
	// A Handler to run the timer
	private Handler timeHandler = new Handler();
	
	/*
	 * Create a new Runnable that decrements the timer and causes itself to run
	 * again after 1 second
	 */
	private Runnable timerTask = new Runnable() {
		/**
		 * Triggered when the timerTask is run by a Handler
		 */
		public void run() {
			// decrement the timer
			timeCounter--;
			// refresh the screen
			invalidate();
			// run again in 1 second using timeHandler
			timeHandler.postDelayed(timerTask, 1000);
		}
	};

	// The coordinates where the user is 'dragging'
	private float[] dragCoords = new float[2];
	
	// If the user does not want to use drag and drop, manualSelect is true
	private boolean manualSelect;
	
	// Whether we want sound effects or not
	private boolean sfxOn;
	
	// Whether the puzzle is solved or not
	private boolean isSolved = false;
	
	/*
	 *  Whether we want to show the block being dragged in its exact dragging position
	 *  (we do not want to show it if it has been moved to the board during dragging as
	 *  this would mean the user will see the same block duplicated).
	 */ 
	private boolean showSelectedBlock = false;
	
	// Whether or not we are in the "30 Second Challenge" mode or not
	private boolean isTimedMode;
	
	// The MediaPlayer used to play sounds
	private MediaPlayer mp;
	
	// The amount of tiles width the puzzle should be
	private int length;
	
	// Measurements describing how the puzzle will be displayed and how many sections it has.
	// Almost all other measurements (non-pixel related) can be derived from these three fields.
	private int sectionLength, sectionsPerLine, noOfLines;
	
	// width and height of a single cell (in pixels)
	private int width, height;
	
	// the dimensions of the entire puzzle (pixels)
	private int top, bottom, left, right;
	
	// Where the top of the storage area is positioned (pixels).
	// This stays constant throughout the puzzle, as long as the screen size doesn't change.
	private int storageTop;
	
	// Where the first block in the storage area is positioned.
	// This changes as the user 'scrolls' the storage area.
	private int storageTopBlock;
	
	// Create a rectangle that will be used to draw squares anywhere needed, 
	// we define it here to save using "new" for every square that needs to be drawn
	private Rect squareRect = new Rect();
	
	// The rectangle that represents the storage area
	private Rect storageRect = new Rect();
	
	// Two rectangles the same color as the background, used to hide blocks that have been
	// scrolled out of the storage area
	private Rect aboveStorageRect = new Rect();
	private Rect underStorageRect = new Rect();
	
	// Bitmap used as a 'retry' button at end of time in timed mode
	private Bitmap retry;
	
	// A rectangle representing the position and size of the retry button.
	private Rect retryRect = new Rect();
	
	/*
	 * The paints that will be used to draw the puzzle
	 */
	private Paint linePaint;
	private Paint selectedPaint;
	private Paint squarePaint;
	private Paint separatorPaint;
	private Paint textPaint;
	
	// The puzzle object that creates and controls the puzzle itself
	private Puzzle puzzle;
	
	// The colors that are stored on the board
	private Iterator<Integer> puzzleColors;
	
	// The block that the user currently has selected
	private Block selectedBlock;
	
	// The position that the user has scrolled to (pixels)
	private float scrollPosition;
	
	/**
	 * Constructor for if we are in edit mode (which will never happen)
	 * @param context
	 */
	public PuzzleView(Context context) {
		this(context, 2, false, true, false);
	}

	/**
	 * Constructor for the PuzzleView class
	 * @param context
	 * @param difficulty - the difficulty the puzzle should be (determines the length of the board)
	 * @param manualSelect - true if the user doesn't want to use drag-and-drop
	 * @param sfx - true if the user wants sound effects, false otherwise
	 * @param timedMode - true if we are in a "30 Second Challenge"
	 */
	public PuzzleView(Context context, int difficulty, boolean manualSelect, boolean sfx, boolean timedMode) {
		super(context);
		
		// Set the background color to our choosing
		this.setBackgroundColor(getResources().getColor(R.color.background));
		
		// Set the length of the puzzle to be the difficulty * 6
		this.length = difficulty * 6;
		
		// Set whether we want to use manual select or not
		this.manualSelect = manualSelect;
		
		// Set whether we want sound effects or not
		this.sfxOn = sfx;
		
		// Set whether or not we are in timed mode
		isTimedMode = timedMode;
		
		// Create a new puzzle with the specified difficulty
		newPuzzle(difficulty);
		
		// If we're in timed mode, initialise the Bitmap for the retry button
		if (isTimedMode) {
			retry = BitmapFactory.decodeResource(getResources(), R.drawable.retry);
		}
		
		// Set up basic paint that other paints can be based on (saves repeating)
		Paint basePaint = new Paint();
		basePaint.setColor(getResources().getColor(R.color.lineColor));
		basePaint.setStyle(Paint.Style.STROKE);
		basePaint.setAntiAlias(true);
		
		// Used to draw thin lines
		linePaint = new Paint(basePaint);
		
		// Used to show when a block has been selected
		selectedPaint = new Paint(basePaint);
		selectedPaint.setColor(getResources().getColor(R.color.selected));
		selectedPaint.setStrokeWidth(5);
		
		// Used to fill squares (and the storage area) with a color
		squarePaint = new Paint(basePaint);
		squarePaint.setStyle(Paint.Style.FILL);
		
		// Thick paint used to show separation between sections on the board
		separatorPaint = new Paint(basePaint);
		separatorPaint.setStrokeWidth(10);
		
		// Used to draw text
		textPaint = new Paint(basePaint);
		textPaint.setTextAlign(Align.CENTER);
	}
	
	/**
	 * Creates a new puzzle and resets certain fields.
	 * @param difficulty - the difficulty of the new puzzle (determines its size)
	 */
	private void newPuzzle(int difficulty) {
		this.isSolved = false;
		this.selectedBlock = null;
		
		// Create a new puzzle, setting the difficulty
		puzzle = new Puzzle(difficulty);
		
		// Make the sectionLength whatever puzzle calculated it should be
		this.sectionLength = puzzle.getSectionLength();
		
		// Calculate how the puzzle should be displayed
		this.onSizeChanged(this.getWidth(), this.getHeight(), 0, 0);
		
		// Display the puzzle
		this.invalidate();
	}
	
	/**
	 * If the user can no longer see the puzzle, e.g. they have quit or gone to main menu,
	 * stop the counter from counting (also called when the puzzle is first shown).
	 */
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// Make sure we're in timed mode, otherwise we don't need to do anything
		if (isTimedMode) {
			// If we can no longer see the view, stop the timerTask from occurring again.
			if (visibility == View.GONE) {
				timeHandler.removeCallbacks(timerTask);
			}
			// If the view is now visible, schedule timerTask again.
			else {
				timeHandler.removeCallbacks(timerTask);
				timeHandler.postDelayed(timerTask, 1000);
			}
		}
		super.onWindowVisibilityChanged(visibility);
	}

	/**
	 * Called whenever the puzzle needs to be redrawn
	 */
	@Override
	protected void onDraw(Canvas canvas) {

		// Draw the storage area
		squarePaint.setColor(getResources().getColor(R.color.storage));
		canvas.drawRect(storageRect, squarePaint);
		canvas.drawRect(storageRect, linePaint);
		
		// Draw the blocks in the storage area
		drawBlocks(canvas);
		
		// Draw rectangles above and below the storage area, to mask blocks that have been scrolled out of view
		squarePaint.setColor(getResources().getColor(R.color.background));
		canvas.drawRect(aboveStorageRect, squarePaint);
		canvas.drawRect(underStorageRect, squarePaint);
		
		// Draw the main puzzle (the squares at the top of the screen), including bold separators
		drawSquares(canvas);
		
		// Highlight the selected block
		highlightSelection(canvas);
		
		// Draw the block being dragged
		if (selectedBlock != null && showSelectedBlock) {
			drawBlockAtCoords(canvas, selectedBlock, dragCoords);
		}
		
		// If the puzzle is solved, and we're not in timed mode, ask the user to start a new puzzle
		if (isSolved && !isTimedMode) {
			// If not in timed mode, ask the user to start a new puzzle
			textPaint.setTextSize(getResources().getDimension(R.dimen.solvedText));
			canvas.drawText(
					getResources().getString(R.string.puzzle_solved), 
					left+(right-left)/2, 
					storageTop+(bottom-storageTop)/4, 
					textPaint
					);
			textPaint.setTextSize(getResources().getDimension(R.dimen.new_puzzleText));
			canvas.drawText(
					getResources().getString(R.string.new_puzzle),
					left+(right-left)/2,
					storageTop+(bottom-storageTop)/2,
					textPaint
					);
		}
		// If we are in timed mode
		if (isTimedMode) {
			// If we are out of time
			if (timeCounter <= 0) {
				// Tell the user their score
				textPaint.setTextSize(getResources().getDimension(R.dimen.solvedText));
				canvas.drawText(getResources().getString(R.string.time_up), left+(right-left)/2, storageTop+(bottom-storageTop)/4,textPaint);
				canvas.drawText(getResources().getString(R.string.you_scored) + " " + solvedCounter, left+(right-left)/2, storageTop+(bottom-storageTop)/2,textPaint);
				
				// Draw the retry button
				canvas.drawBitmap(retry, null, retryRect, squarePaint);
			}
			// If we are in timed mode but not out of time yet
			else {
				textPaint.setTextSize(getResources().getDimension(R.dimen.counterText));
				// Draw the timer
				textPaint.setTextAlign(Align.LEFT);
				canvas.drawText(getResources().getString(R.string.time) + " " + timeCounter, left+10, bottom-10, textPaint);
				// Draw the solved counter
				textPaint.setTextAlign(Align.RIGHT);
				canvas.drawText(getResources().getString(R.string.score) + " " + solvedCounter, right-10, bottom-10, textPaint);
				
				// Reset the textPaint to its default alignment
				textPaint.setTextAlign(Align.CENTER);
			}
		}
		super.onDraw(canvas);
	}
	
	/**
	 * Draws the board at the top of the screen, with all squares that are in it
	 * @param canvas - the canvas to draw on
	 */
	private void drawSquares(Canvas canvas) {
		// Create an iterator that contains all of the squares in the puzzle board
		puzzleColors = puzzle.create_allSquares_iterator();
		// Go through each line
		for (int line = 0; line < noOfLines; line++) {
			// On each line, go through each position
			for (int position = 0; position < sectionLength*sectionsPerLine; position++) {
				Integer currentColor = puzzleColors.next();
				// If the current color isn't null (i.e. there is a square there), print a square 
				// using the current color
				if (currentColor != null) {
					drawSquare(canvas, position, line, getResources().getColor(currentColor));
				}
				// If there isn't a square, print a white square instead
				else {
					drawSquare(canvas, position, line, getResources().getColor(R.color.white));
				}
			}
		}
		
		// Draw bold separators between sections
		// Go through each line
		for (int line = 0; line < noOfLines; line++) {
			// On each line, print i number of separating lines, up to the amount of sections per line
			for (int i=0; i <= (length/sectionLength); i++) {
				canvas.drawLine(
						left+i*sectionLength*width, 
						top+line*(height+10), 
						left+i*sectionLength*width, 
						top+line*(height+10) + height, 
						separatorPaint
						);
			}
		}
	}
	
	/**
	 * Draws a single square on the game board
	 * @param canvas - the canvas to draw on
	 * @param position - the position of the square on the board (x-position only)
	 * @param line - the line that the square is on
	 * @param color - the color that the square should be printed in
	 */
	private void drawSquare(Canvas canvas, int position, int line, int color) {
		// Create a rectangle using position and line to work out where it should be
		squareRect.set(
				left + position*width,
				top + line*(height+10),
				left + (position+1) * width,
				top + line*(height+10) + height
				);
		
		squarePaint.setColor(color);
		// Draw the square color
		canvas.drawRect(squareRect, squarePaint);
		// Draw an outline around the square
		canvas.drawRect(squareRect, linePaint);
	}
	
	/**
	 * Draw the blocks in the storage area
	 * @param canvas
	 */
	private void drawBlocks(Canvas canvas) {
		// Create an iterator containing all of the blocks in the storage area
		Iterator<Block> blocks = puzzle.create_storage_iterator();
		// Start with a position of 0 (the top of the storage area)
		int position = 0;
		// Go through the iterator, drawing the blocks
		while(blocks.hasNext()) {
			float[] coords = {left + 10, storageTopBlock + 10 + position*(height+10)};
			this.drawBlockAtCoords(canvas, blocks.next(), coords);
			position++;
		}
	}
	
	/**
	 * Draws a single block given coordinates
	 * @param canvas - the canvas to draw on
	 * @param block - the block to be drawn
	 * @param coords - the coordinates where the block will be drawn
	 */
	private void drawBlockAtCoords(Canvas canvas, Block block, float[] coords) {
		
		int x = (int) (coords[0]);
		int y = (int) (coords[1]);
		
		// Create an iterator containing the colors of the block
		Iterator<Integer> colors = block.create_iterator();
		// j is the horizontal position of the individual square
		int j=0;
		// Go through the iterator, drawing each square
		while (colors.hasNext()) {
			squareRect.set(
					x + j*width,
					y,
					x + j*width + width,
					y + height
					);
			squarePaint.setColor(getResources().getColor(colors.next()));
			canvas.drawRect(squareRect, squarePaint);
			canvas.drawRect(squareRect, linePaint);
			j++;
		}
	}
	
	/**
	 * Highlights with an orange border the block that the user has selected.
	 * If the block overflows lines, the selection lines will also overflow onto the next line.
	 * @param canvas
	 */
	private void highlightSelection(Canvas canvas) {
		// Make sure the user has selected a block
		if (selectedBlock != null) {
			// Find the length of the block
			int blockLength = selectedBlock.getSize();
			
			// If the block is on the board
			if (selectedBlock.getBoardPosition() != -1) {
				
				int position = selectedBlock.getBoardPosition();
				int lineLength = sectionLength*sectionsPerLine;
				
				// First draw a vertical line representing the start of the block
				canvas.drawLine(
						left + (position % lineLength)*width,
						top + (position / lineLength) * (height+10),
						left + (position % lineLength)*width,
						top + (position / lineLength) * (height+10) + height,
						selectedPaint
						);
				
				// Use a recursive method to go through each line that the block is on,
				// printing the 'tramlines' and the final vertical line
				drawHorizontalSelectedLines(canvas, position, blockLength);
				
			}
			
			// If the block is in storage, just draw a rectangle around it
			else {
				int position = puzzle.getBlockStoragePosition(selectedBlock);
				squareRect.set(
						left+10, 
						storageTopBlock + 10 + position*(10+height), 
						left + 10 + width * blockLength,
						storageTopBlock + 10 + position*(10+height) + height
						);
				canvas.drawRect(squareRect, selectedPaint);
			}
		}
	}
	
	/**
	 * Recursive method that draws the orange 'tramlines' above and below the selected block on the current line.
	 * Once one line is done, if there are squares overflowing, we call the method again for the next line.
	 * @param canvas
	 * @param currentPosition - the position on the board we are currently on
	 * @param squaresLeft - the amount of squares we still need to highlight
	 */
	private void drawHorizontalSelectedLines(Canvas canvas, int currentPosition, int squaresLeft) {
		
		/*
		 *  Set up some values that will be used frequently
		 */
		// The length of a line of squares in the puzzle
		int lineLength = length / noOfLines;
		
		// Where the 'tramlines' start - on the current line (in pixels)
		int startX = left + (currentPosition % lineLength)*width;
		
		// Where the tramlines end - on the current line (in pixels) - this will be different
		// depending on whether we are on a base case or not
		int stopX;
		
		// The y position (in pixels) of the top tramline
		int topY = top + (currentPosition / lineLength) * (height+10);
		
		// The y position (in pixels) of the bottom tramline
		int bottomY = topY + height;
		
		// The distance to the end of the line from the current position (in squares)
		int distToEndOfLine = (lineLength-1) - (currentPosition % lineLength);
		
		/* 
		 * If we are overflowing the line, draw to the end of the current line, 
		 * and recurse onto new line
		 */
		if (squaresLeft > distToEndOfLine+1) {
			stopX = right;
			canvas.drawLine(startX, topY, stopX, topY, selectedPaint);
			canvas.drawLine(startX, bottomY, stopX, bottomY, selectedPaint);
			
			int nextPosition = currentPosition + distToEndOfLine + 1;
			int nextSquaresLeft = (squaresLeft - distToEndOfLine) - 1;
			
			drawHorizontalSelectedLines(canvas, nextPosition, nextSquaresLeft);
			
		}
		
		/*
		 *  The base case:
		 *  If we aren't going to overflow, then we don't need to recurse onto the next line,
		 *  so draw the tramlines up to the end of the block and draw the last vertical line
		 *  to represent the end of the selected block.
		 */
		else {
			stopX = left + (currentPosition % lineLength)*width + width*squaresLeft;
			canvas.drawLine(startX, topY, stopX, topY, selectedPaint);
			canvas.drawLine(startX, bottomY, stopX, bottomY, selectedPaint);
			
			canvas.drawLine(stopX, topY, stopX, bottomY, selectedPaint);
		}		
	}

	/**
	 * Called whenever the screen size changes (including when the puzzle is first created).
	 * This makes sure that the puzzle displays correctly when changing orientation or when 
	 * using different devices.
	 * The way that the puzzle is shown depends on both the pixel density and the physical 
	 * screen size.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		// Simple dimensions of the entire puzzle
		top = 30;
		left = 10;
		right = w-10;
		bottom = h-10;
		
		/*
		 * Decide how many sections to draw on each line, depending on the size of the screen,
		 * ensuring each line has an equal amount of sections
		 */
		int widthPixels = getContext().getResources().getDisplayMetrics().widthPixels;
		int pixelDensity = getContext().getResources().getDisplayMetrics().densityDpi;
		int screenWidth = widthPixels/pixelDensity;
		
		/* 
		 * This is the maximum amount of squares that should be allowed on 1 line, depending
		 * on the screen size. It must be at least 12.
		 */
		int maxSquares;
		
		if (screenWidth*4 >= 12) {
			maxSquares = screenWidth*4;
		}
		else {
			maxSquares = 12;
		}
		
		// Calculate the number of sections the puzzle has in total
		int amountOfSections = length/sectionLength;
		
		/* 
		 * Loop until we find a solution that upholds the following parameters:
		 * 		The length of a line is at most 12 squares (and as close to 12 as possible)
		 * 		Each line has an equal amount of sections
		 */
		for (int i=2; i<=maxSquares; i++) {
			int tempSectionsPerLine = i / sectionLength;
			// If each line would be equal, set this as the sections per line
			if (tempSectionsPerLine != 0  &&  amountOfSections % tempSectionsPerLine == 0) {
				sectionsPerLine =  tempSectionsPerLine;
			}
		}
		
		// Width and height of a single cell
		width = (w-20) / (sectionLength*sectionsPerLine);
		height = width;
		
		// Setting up the remaining fields that are based on screen size.
		this.noOfLines = (length/sectionLength)/sectionsPerLine;
		this.storageTop = top + noOfLines*(height+10) + 10;
		this.storageTopBlock = storageTop;
		this.storageRect.set(left, storageTop, right, bottom);
		this.aboveStorageRect.set(0, 0, w, storageTop-1);
		this.underStorageRect.set(0, bottom+1, w, h);
		this.retryRect.set(
				left+(right-left)/2 - 50,
				storageTop + 3*(bottom-storageTop)/4 - 50,
				left+(right-left)/2 + 50,
				storageTop + 3*(bottom-storageTop)/4 + 50
				);
		
		super.onSizeChanged(w, h, oldw, oldh);
		
		// If the user can't see the storage area (very rare), tell them to change
		// to vertical orientation
		if (top+noOfLines * (height +10) > bottom - height && h > 0) {
			Toast.makeText(getContext(), "Can't see the blocks? Try turning your phone to portrait", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Called whenever their is a change in how the user is touching the screen
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		// Get the coordinates of the touch event
		float x = event.getX();
		float y = event.getY();
		
		// If we are in the storage area without a  but aren't touching the block, we are trying to scroll
		if (y > storageTop && 
				/*
				 * We must either be in manual mode (where the user can scroll even if a block is selected)
				 * OR have no block selected (as in drag and drop mode, we do not want to scroll when
				 * dragging a block).
				 */
				(this.manualSelect || selectedBlock == null)
				&& this.findBlock(x, y) == null) {
			scroll(event);
		}
		
		// If we are in timed mode and out of time, check if the user has touched the retry button
		// (if statements separated for simplicity)
		if (isTimedMode && timeCounter <= 0) {
			if (x >= left+(right-left)/2 - 50 &&
					y >= storageTop + 3*(bottom-storageTop)/4 - 50 &&
					x <= left+(right-left)/2 + 50 &&
					y <= storageTop + 3*(bottom-storageTop)/4 + 50) {
				this.timeCounter = 30;
				this.solvedCounter = 0;
				newPuzzle(length/6);
			}
		}
		// If the puzzle is solved and we tap in the storage area, start a new puzzle
		else if (isSolved && x > left && x < right && y > storageTop && y < bottom) {
			newPuzzle(length/6);
		}
		// If the puzzle hasn't been solved and we are using manual select, pass the event to the appropriate method
		else if (manualSelect) {
			onTouchManualSelectMode(event);
		}
		// If the puzzle hasn't been solved and we are using drag-and-drop, pass the event to the appropriate method
		else {
			onTouchDragAndDropMode(event);
		}
		
		// Refresh the screen
		this.invalidate();
		return true;
	}

	/**
	 * Called whenever the user is trying to scroll
	 * @param event - the touch event
	 */
	private void scroll(MotionEvent event) {
		float y = event.getY();
		
		// If the user has just touched the screen, record this location as the starting location
		// for the scroll, so that we have something to compare to when the touch moves.
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			scrollPosition = y;
		}
		
		// If the touch is moving
		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			// Find the amount that the touch has moved by
			float amountMoved = y - scrollPosition;
			// If we're trying to scroll down
			if (amountMoved < 0) {
				// Check that we are not moving beyond bottom of blocks
				if (storageTopBlock + amountMoved + (height+10)*puzzle.getStorageSize() >= bottom-10) {
					storageTopBlock += amountMoved;
				}
			}
			// If we are trying to scroll up, 
			else {
				// Check that we are not moving beyond the top of the blocks
				if (storageTopBlock + amountMoved <= storageTop) {
					storageTopBlock += amountMoved;
				}
			}
			// Change the scroll position to the place being touched
			scrollPosition = y;
		}
	}
	
	/**
	 * Selects, deselects, and moves blocks depending on the coordinates of the user's touch.
	 * Uses a tap to select, tap again to move approach.
	 * @param event - the touch event that occurred
	 */
	private void onTouchManualSelectMode(MotionEvent event) {
		// Make sure the event is the user touching the screen
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
		
			// Get the x and y coordinates of the touch
			float x = event.getX();
			float y = event.getY();
			
			// If we haven't yet selected a block, and the user has tapped a block, select this block
			if (selectedBlock == null) {
				if (findBlock(x, y) != null) {
					selectedBlock = findBlock(x,y);
				}
			}
			// If a block is already selected
			else {
				// If where the user has touched doesn't already have a block in it, then try
				// moving the selected block here
				if (findBlock(x, y) == null) {
					// If the block is moved, then deselect the block
					if (moveBlock(selectedBlock, x, y)) {
						selectedBlock = null;
						// If the puzzle is solved, set isSolved to true and play a sound
						if (puzzle.isSolved()) {
							isSolved = true;
							playSound(R.raw.puzzle_solved);
							// If we are in timed mode, immediately create a new puzzle
							if (isTimedMode) {
								solvedCounter++;
								newPuzzle(length/6);
							}
						}
						// If the board is full but incorrect, play the puzzle_wrong sound
						else if (this.puzzle.getStorageSize() == 0) {
							playSound(R.raw.puzzle_wrong);
						}
						// Otherwise play the block_placed sound
						else {
							playSound(R.raw.block_placed);
						}
					}
				}
				// If the user has touched a different block, make this the selected block instead
				else {
					selectedBlock = findBlock(x,y);
				}
			}
		}
	}
	
	// We need to know whether at any point during the touch, whether the block has been
	// successfully moved
	boolean hasMoved = false;
	
	/**
	 * Allows the user to move blocks into positions by dragging them.
	 * @param event -  the touch event that occurred
	 */
	private void onTouchDragAndDropMode(MotionEvent event) {
		
		// Get the x and y coordinates of the touch
		float x = event.getX();
		float y = event.getY();
		
		// Check what touch event was performed
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Select the block that has been tapped
			// Which block are we trying to find?
			if (findBlock(x, y) != null) {
				selectedBlock = findBlock(x,y);
			}
		case MotionEvent.ACTION_MOVE:
			// If a block has been selected
			if (selectedBlock != null) {
				// Get the coordinates we want to use to drag the block (the centre of the left-most square)
				dragCoords[0] = x - width/2;
				dragCoords[1] = y - height/2;
					/*
					 * Try moving the block to the position we're moving over - this creates the dynamic
					 * placing effect where the block automatically fits into where the user is pressing.
					 */
					if (this.moveBlock(selectedBlock, x, y)) {
						// make the 'artificial' block that we're dragging disappear
						showSelectedBlock = false;
						hasMoved = true;
						Log.d("Me", "hasMoved is true");
					}
					else {
						// Make the block that we're dragging appear again, so the user can move it to any
						// part of the screen
						showSelectedBlock = true;
					}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			// If the block has been moved
			if (hasMoved) {
				hasMoved = false;
				// If the puzzle is solved, set isSolved to true and play a sound
				if (puzzle.isSolved()) {
					isSolved = true;
					playSound(R.raw.puzzle_solved);
					// If we are in timed mode, immediately create a new puzzle
					if (isTimedMode) {
						solvedCounter++;
						newPuzzle(length/6);
					}
				}
				// If the board is full but incorrect, play the puzzle_wrong sound
				else if (this.puzzle.getStorageSize() == 0) {
					playSound(R.raw.puzzle_wrong);
				}
				// Otherwise, just play the block_placed sound
				else {
					playSound(R.raw.block_placed);
				}
			}
			// Deselect the block that was being moved
			selectedBlock = null;
			break;
		}
	}
	
	/**
	 * Simple method to play a sound given its id.
	 * NOTE: This will give error: "should have subtitle controller already set".
	 * This should not be classed as an error but it not is not important in this
	 * context anyway.
	 * @param id - the id of the sound to be played
	 */
	private void playSound(int id) {
		if (sfxOn) {
			if (mp != null) {
				mp.reset();
				mp.release();
			}
			mp = MediaPlayer.create(this.getContext(), id);
			mp.start();
		}
	}
	
	/**
	 * Finds a block given coordinates
	 * @param xfloat - the x coordinate
	 * @param yfloat - the y coordinate
	 * @return the block at the coordinates 
	 */
	private Block findBlock(float xfloat, float yfloat) {
		
		// Turn the floats into ints
		int x = (int) xfloat;
		int y = (int) yfloat;
		
		// The position of the block on the board OR in the storage area
		int position;
		
		// If we are within the area of the board
		if (y < storageTop) {
			// Get the position of the block on the board
			position = findBoardPosition(x, y);
			// If the block doesn't have a position on the board, return null
			if (position == -1) {
				return null;
			}
			// If the block is on the board, return the block
			else {
				return puzzle.getBlockFromBoard(position);
			}
		}
		// If we are within the area of the storage area
		else {
			// Get the position of the block in storage
			position = findStoragePosition(y);
			/*
			 * Check that:
			 * 		we are looking in an appropriate position
			 * 		there is a block at this position
			 * 		the coordinates are within the x-range of the block
			 */
			if (position == -1 || 
					puzzle.getBlockFromStorage(position) == null || 
					x > left + 10 + puzzle.getBlockFromStorage(position).getSize()*width) {
				return null;
			}
			// If the block is in storage, return the block
			else {
				return puzzle.getBlockFromStorage(position);
			}
		}
	}
	
	/**
	 * Finds the position of a block on the board block given coordinates
	 * @param xfloat - the x-coordinate
	 * @param yfloat - the y-coordinate
	 * @return the position of the block as an int
	 */
	private int findBoardPosition(float xfloat, float yfloat) {
		// The position of the coordinates to return
		int position;
		
		int x = (int) xfloat;
		int y = (int) yfloat;
		
		// How many squares across the coordinates are
		int xposition;
		// How many lines down the coordinates are
		int yposition;
		
		// If the x-coordinate is within the range of the puzzle, calculate the xposition
		if (x > left && x < right) {
			xposition = (x-left)/((right-left)/(sectionLength*sectionsPerLine));
		}
		// Otherwise, return -1
		else {
			xposition = -1;
		}
		
		// Get the line that y is on, including the 10 pixel gap below each line,
		// to make it easier for the user to hit
		yposition = (y-top)/(10+height);
		
		// Use xposition and yposition to find the position on the overall board
		if (xposition == -1 || yposition == -1) {
			return -1;
		}
		else {
			position = yposition*sectionsPerLine*sectionLength + xposition;
			return position;
		}
	}
	
	/**
	 * Finds the position of a block in storage given a y-coordinate - we're not worried 
	 * about the x-coordinate, only if the user has tapped along the line of the block.
	 * We include the 10-pixel gap below lines as part of the line above, to make touch
	 * input easier for the user.
	 * @param yfloat - the y-coordinate
	 * @return the position of the block as an int
	 */
	private int findStoragePosition(float yfloat) {
		int y = (int) yfloat;
		
		// If the coordinate is higher than the top of the storage, or lower than the lowest
		// block in storage, return -1
		if (y < storageTop || y > storageTopBlock + (10+height)*puzzle.getStorageSize()) {
			return -1;
		}
		// If the coordinate is in the storage area, find the position of the block
		else {
			return (y-storageTopBlock-1) / (height + 10);
		}
	}
	
	/**
	 * Moves a block from its original position to the position found from coordinates
	 * @param block - the block being moved
	 * @param x - the x-coordinate of the position the block should be moved to
	 * @param y - the y-coordinate of the position the block should be moved to
	 * @return true if the block has been successfully moved, false otherwise
	 */
	private boolean moveBlock(Block block, float x, float y) {

		int boardPosition = findBoardPosition(x, y);
		
		// If the block is being moved to the board and there is space, move it there
		if (boardPosition != -1 && puzzle.isSpace(block, boardPosition)) {
			puzzle.addBlockToBoard(block, boardPosition);
			return true;
		}
		// If the block is being moved to the storage area, move it there,
		// checking that it's not already in the storage area
		else if (y > storageTop && block.getBoardPosition() != -1) {
			puzzle.removeBlockFromBoard(block);
			return true;
		}
		// If the user has tapped a random other part of the screen, or if there was no space, 
		// return false
		else {
			return false;
		}
	}
}