package uk.singlekey.repeata_freak;

import uk.ac.surrey.so00076.repeater.R;
import android.os.Bundle;
import android.app.Activity;

/**
 * This class is used when the user selects key features.
 * It simply sets the View to be activity_key_features.xml
 * 
 * @author Samuel O'Sullivan
 */
public class KeyFeatures extends Activity {

	/**
	 * Sets the view to be the key features page
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_features);
	}

}