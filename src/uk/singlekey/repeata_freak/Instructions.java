package uk.singlekey.repeata_freak;

import uk.ac.surrey.so00076.repeater.R;
import android.os.Bundle;
import android.app.Activity;

/**
 * This class is used when the user selects instructions.
 * It simply sets the view to be activity_instructions.xml
 * 
 * @author Samuel O'Sullivan
 */
public class Instructions extends Activity {

	/**
	 * Sets the view to be the instructions page
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
	}
	
}