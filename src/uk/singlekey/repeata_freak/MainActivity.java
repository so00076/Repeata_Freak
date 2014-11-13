package uk.singlekey.repeata_freak;

import uk.ac.surrey.so00076.repeater.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/**
 * This class represents the 'main menu' of the app
 * 
 * @author Samuel O'Sullivan
 */
public class MainActivity extends Activity {

	/**
	 * Sets the view to be activity_main.xml when the app is first loaded
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	/**
	 * Creates the options bar using R.menu.main
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Checks what action bar option has been selected, and starts the appropriate activity
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent i = new Intent(this, Prefs.class);
			startActivity(i);
			return true;
		}
		return false;
	}
	
	/**
	 * Method called if a button is pressed.
	 * Checks which button has been pressed and starts the appropriate activity.
	 * @param view
	 */
	public void buttonClickHandler(View view) {
		// Make Button objects of the buttons in the main menu
		Button new_game = (Button) findViewById(R.id.new_game);
		Button timed_challenge = (Button) findViewById(R.id.timed_challenge);
		Button instructions = (Button) findViewById(R.id.instructions);
		Button key_features = (Button) findViewById(R.id.key_features);
		
		if (new_game.isPressed()) {
			// Set the invisible option timed_mode to false
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("timed_mode", false).commit();
			Intent i = new Intent(this, PuzzleActivity.class);
			startActivity(i);
		}
		
		else if (timed_challenge.isPressed()) {
			// Set the invisible option timed_mode to true
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("timed_mode", true).commit();
			Intent i = new Intent(this, PuzzleActivity.class);
			startActivity(i);
		}
		
		else if (instructions.isPressed()) {
			Intent i = new Intent(this, Instructions.class);
			startActivity(i);
		}
		
		else if (key_features.isPressed()) {
			Intent i = new Intent(this, KeyFeatures.class);
			startActivity(i);
		}
	}
}