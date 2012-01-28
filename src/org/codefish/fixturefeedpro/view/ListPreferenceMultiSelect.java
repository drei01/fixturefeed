package org.codefish.fixturefeedpro.view;


import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import org.codefish.fixturefeedpro.AppConstants;
import org.codefish.fixturefeedpro.KeywordPrefHelper;
import org.codefish.fixturefeedpro.R;

/**
 * @author mreid
 */
public class ListPreferenceMultiSelect extends ListPreference {

    private boolean[] mClickedDialogEntryIndices;
    private String[] entries;
    private Context context;
    private String preference;
    private String preferenceValues;
    private int defaults;

    public ListPreferenceMultiSelect(Context context, AttributeSet attrs) {
        super(context, attrs);        
        this.context = context;
        
        preference = attrs.getAttributeValue("preferenceKey", AppConstants.KEYWORD_LIST_PREF);
        preferenceValues = attrs.getAttributeValue("preferenceValKey", AppConstants.KEYWORD_SELECTED_PREF);
        defaults = attrs.getAttributeIntValue("defaults", "defaults",  R.array.search_words);

        entries = KeywordPrefHelper.getKeywords(context, preference,preferenceValues, defaults);
        mClickedDialogEntryIndices = new boolean[entries.length];
    }

    @Override
    public void setEntries(CharSequence[] entries) {
    	super.setEntries(entries);
        KeywordPrefHelper.setKeywords(context, (String[]) entries, preference);
        mClickedDialogEntryIndices = new boolean[entries.length];
    }

    public ListPreferenceMultiSelect(Context context) {
        this(context, null);
        entries = KeywordPrefHelper.getKeywords(context, preference,preferenceValues, defaults);
        mClickedDialogEntryIndices = new boolean[entries.length];
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        //get the keywords again just incase they've changed
        entries = KeywordPrefHelper.getKeywords(context, preference,preferenceValues, defaults);
        mClickedDialogEntryIndices = new boolean[entries.length];
    	CharSequence[] entryValues = KeywordPrefHelper.getKeywordValues(context, preference, preferenceValues, defaults);

        if (entries == null || entryValues == null || entries.length != entryValues.length ) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries(entryValues);
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices,
                new DialogInterface.OnMultiChoiceClickListener() {
					public void onClick(DialogInterface dialog, int which, boolean val) {
                    	mClickedDialogEntryIndices[which] = val;
					}
        });
    }

    private void restoreCheckedEntries(CharSequence[] entryValues) {
    	if (entryValues != null ) {
            for(int i=0; i<entryValues.length;i++){
                //set the corresponding boolean
                if(!AppConstants.KEYWORD_SELECTED.equals(entryValues[i].toString().trim())){
                    mClickedDialogEntryIndices[i]=false;
                }else{
                    mClickedDialogEntryIndices[i]=true;
                }
            }
    	}
    }

    /**
     * store the entries that were selected
     * @param positiveResult
     */
	@Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
        	String[] values = new String[entries.length];
        	for ( int i=0; i<mClickedDialogEntryIndices.length; i++ ) {
        		if ( mClickedDialogEntryIndices[i] ) {
                                values[i] = AppConstants.KEYWORD_SELECTED;
        		}else{
                            values[i] = AppConstants.KEYWORD_UNSELECTED;
                        }
        	}
            
                KeywordPrefHelper.setKeywordValues(context, values, preferenceValues);
        }
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getPreferenceValues() {
        return preferenceValues;
    }

    public void setPreferenceValues(String preferenceValues) {
        this.preferenceValues = preferenceValues;
    }

    public int getDefaults() {
        return defaults;
    }

    public void setDefaults(int defaults) {
        this.defaults = defaults;
    }



}

