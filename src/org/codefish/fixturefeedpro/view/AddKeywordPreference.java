package org.codefish.fixturefeedpro.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import org.codefish.fixturefeedpro.AppConstants;
import org.codefish.fixturefeedpro.KeywordPrefHelper;
import org.codefish.fixturefeedpro.R;

/**
 *
 * @author Matthew
 */
public class AddKeywordPreference extends EditTextPreference {

    private String preference;
    private String preferenceValues;
    private int defaults;

    public AddKeywordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.ListPreferenceMultiSelect);
        preference = a.getString(R.styleable.ListPreferenceMultiSelect_preferenceKey);
        preferenceValues = a.getString(R.styleable.ListPreferenceMultiSelect_preferenceValKey);
        defaults = a.getInteger(R.styleable.ListPreferenceMultiSelect_defaults, R.array.search_words);
    }

    @Override
    /**
     * Always return an empty string because we are only using this to add keywords
     */
    public String getText() {
        //return getSharedPreferences().getString(getKey(), "");
        return "";
    }

    @Override
    public void setText(String text) {
        if(text==null || "".equals(text)){
            return;
        }
        //add the text to the keywords preference
        String[] keywords = KeywordPrefHelper.getKeywords(getContext(),preference,preferenceValues,defaults);
        String[] newKeywords = new String[keywords.length+1];
        System.arraycopy(keywords, 0, newKeywords, 0, keywords.length);//copy the old elements into the new array
        newKeywords[newKeywords.length-1] = text.replace(AppConstants.KEYWORD_PREF_SPLIT, "");//add the new element
        //then set it in preferences
        KeywordPrefHelper.setKeywords(getContext(), newKeywords, preference);
        String[] keywordValues = KeywordPrefHelper.getKeywordValues(getContext(),preference, preferenceValues, defaults);
        String[] newkeywordValues = new String[keywordValues.length+1];
        System.arraycopy(keywordValues, 0, newkeywordValues, 0, keywordValues.length);//copy the old elements into the new array
        newkeywordValues[newkeywordValues.length-1]=AppConstants.KEYWORD_SELECTED;//set the new keyword to be selected
        KeywordPrefHelper.setKeywordValues(getContext(), newkeywordValues, preferenceValues);

    }

    public int getDefaults() {
        return defaults;
    }

    public void setDefaults(int defaults) {
        this.defaults = defaults;
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


}
