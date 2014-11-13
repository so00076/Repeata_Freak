package uk.singlekey.repeata_freak;

import uk.ac.surrey.so00076.repeater.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * This class is used to display preferences
 * 
 * @author Samuel O'Sullivan
 */
public class Prefs extends PreferenceActivity {

	/**
	 * On creation, add the preferences that are stored in resources
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		// Remove the 'timed_mode' setting from the preferences - this option is for 
		// internal purposes as this setting will depend on which button is pressed on the main menu.
		this.getPreferenceScreen().removePreference(this.getPreferenceManager().findPreference("timed_mode"));
	}
}