package uk.singlekey.repeata_freak;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;

/**
 * This class is created when the view needs to switch to a puzzle
 * 
 * @author Samuel O'Sullivan
 */
public class PuzzleActivity extends Activity {
	
	/**
	 * Called when the class is instantiated, this creates and displays a new puzzle
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the difficulty from the preferences menu
		String difficulty = PreferenceManager.getDefaultSharedPreferences(this).getString("difficulty", "2");
		
		// Get the state of the manual_select boolean
		boolean manual_select = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("manual_select", false);
		
		// Get the state of the sfx boolean
		boolean sfx = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("sfx", false);
		
		// Get the state of the timed_mode boolean
		boolean timed_mode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("timed_mode", false);
		
		// Create a new PuzzleView to display, giving preference states as parameters
		PuzzleView puzzle = new PuzzleView(this, Integer.parseInt(difficulty), manual_select, sfx, timed_mode);
		
		// Show the puzzle
		setContentView(puzzle);
		
	}
}