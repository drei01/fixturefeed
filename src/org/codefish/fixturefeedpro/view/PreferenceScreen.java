package org.codefish.fixturefeedpro.view;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import org.codefish.fixturefeedpro.AppConstants;
import org.codefish.fixturefeedpro.KeywordPrefHelper;
import org.codefish.fixturefeedpro.R;

/**
 *
 * @author Matthew
 */
public class PreferenceScreen extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        ListPreferenceMultiSelect keywords = (ListPreferenceMultiSelect) findPreference("keywordsMulti");
        keywords.setDefaults(R.array.search_words);
        keywords.setPreference(AppConstants.KEYWORD_LIST_PREF);
        keywords.setPreferenceValues(AppConstants.KEYWORD_SELECTED_PREF);


        ListPreferenceMultiSelect excludeWords = (ListPreferenceMultiSelect) findPreference("exlcudewordsMulti");
        excludeWords.setDefaults(R.array.search_exclude);
        excludeWords.setPreference(AppConstants.EXCLUDE_LIST_PREF);
        excludeWords.setPreferenceValues(AppConstants.EXCLUDE_SELECTED_PREF);
        
        // click listener for the clear keywords button
        Preference customPref = (Preference) findPreference("clearKeywords");
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                KeywordPrefHelper.resetKeywords(getBaseContext(), AppConstants.KEYWORD_LIST_PREF,AppConstants.KEYWORD_SELECTED_PREF, R.array.search_words);
                //clear keywords and keyword values back to default here
                Toast.makeText(getBaseContext(),
                        "Keywords Cleared",
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });

        // click listener for the clear keywords button
        customPref = (Preference) findPreference("clearExcludewords");
        customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference preference) {
                KeywordPrefHelper.resetKeywords(getBaseContext(), AppConstants.EXCLUDE_LIST_PREF,AppConstants.EXCLUDE_SELECTED_PREF, R.array.search_exclude);
                //clear keywords and keyword values back to default here
                Toast.makeText(getBaseContext(),
                        "Keywords Cleared",
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });

    }
}
