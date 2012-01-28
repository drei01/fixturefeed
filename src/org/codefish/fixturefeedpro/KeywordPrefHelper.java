/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codefish.fixturefeedpro;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 *Helper class for getting the keywords from shared preferences
 * @author Matthew
 */
public class KeywordPrefHelper {
    /**
     * get keywords an a string array from shared preferences
     * @param sharedPreferences
     * @param context
     * @return
     */
    public static String[] getKeywords(Context context, String preference, String selectedPref, int defaults) {
        SharedPreferences sharedPreferences = getSharedPrefs(context);

        String keywordsPref = sharedPreferences.getString(preference, null);
        //if the keywords aren't initialised, do that and then return the defaults
        if (keywordsPref == null) {
            return resetKeywords(context, preference, selectedPref, defaults);
        }
        return keywordsPref.split(AppConstants.KEYWORD_PREF_SPLIT);
    }

        /**
     * set the keywords in shared preferences
     * @param sharedPreferences
     * @param context
     * @param keywords
     * @return
     */
    public static String setKeywords(Context context, String[] keywords, String preference) {
        SharedPreferences sharedPreferences = getSharedPrefs(context);
        if (keywords == null || keywords.length == 0) {
            return null;
        }
        String keywordsPref;
        StringBuilder keywordBuilder = new StringBuilder();
        for (int i = 0; i < keywords.length; i++) {
            keywordBuilder.append(keywords[i]);
            if (i < keywords.length - 1) {
                keywordBuilder.append(AppConstants.KEYWORD_PREF_SPLIT);
            }
        }
        keywordsPref = keywordBuilder.toString();
        //put the list of key words in the shared preference
        sharedPreferences.edit().putString(preference, keywordsPref).commit();
        return keywordsPref;
    }

    /**
     * get keywords an a string array from shared preferences
     * @param sharedPreferences
     * @param context
     * @return
     */
    public static String[] getKeywordValues(Context context, String preference, String selectedPreference,  int defaults) {
        SharedPreferences sharedPreferences = getSharedPrefs(context);

        String selectedPref = sharedPreferences.getString(selectedPreference, null);
        //if its null return a blanked array of the same size as the keywords
        if (selectedPref == null) {
            String[] defaultKeywords = getKeywords(context,preference,selectedPreference,defaults);
            for (int i = 0; i < defaultKeywords.length; i++) {
                defaultKeywords[i] = AppConstants.KEYWORD_SELECTED;
            }
            return defaultKeywords;
        }
        return selectedPref.split(AppConstants.KEYWORD_PREF_SPLIT);
    }

    /**
     * set the keywords in shared preferences
     * @param sharedPreferences
     * @param context
     * @param keywords
     * @return
     */
    public static String setKeywordValues(Context context, String[] keywordValues, String preference) {
        SharedPreferences sharedPreferences = getSharedPrefs(context);

        if (keywordValues == null || keywordValues.length == 0) {
            return null;
        }
        String keywordsPref;
        StringBuilder keywordBuilder = new StringBuilder();
        for (int i = 0;i < keywordValues.length;i++) {
            keywordBuilder.append(keywordValues[i]);
            if (i < (keywordValues.length - 1)) {
                keywordBuilder.append(AppConstants.KEYWORD_PREF_SPLIT);
            }
        }
        keywordsPref = keywordBuilder.toString();
        //put the list of key words in the shared preference
        sharedPreferences.edit().putString(preference, keywordsPref).commit();
        return keywordsPref;
    }

    /**
     * reset the keywords back to their original values
     * @param context
     * @return
     */
    public static String[] resetKeywords(Context context, String preference, String selectedPref, int defaults) {
        String[] defaultKeywords = context.getResources().getStringArray(defaults);
        setKeywords(context, defaultKeywords, preference);
        String[] defaultKeywordValues = new String[defaultKeywords.length];
        for (int i = 0; i < defaultKeywords.length; i++) {
            defaultKeywordValues[i] = AppConstants.KEYWORD_SELECTED;
        }
        setKeywordValues(context, defaultKeywordValues, selectedPref);
        return defaultKeywords;
    }

    /**
     * get the name of the category to search in
     * returns null if we want to search all categories
     * @param context
     * @return
     */
    public static String getCategory(Context context){
        SharedPreferences sharedPreferences = getSharedPrefs(context);
        //get the category from shared prefs, should never reach the appconstants default, the preference default is set in the xml
        String category = sharedPreferences.getString(AppConstants.CATEGORY_PREF,AppConstants.DEFAULT_CAT);
        return category.toLowerCase().equals("all")? null : category.trim().toLowerCase();//return null if the we want to search all categories
    }

    public static boolean searchSkySports(Context context){
         SharedPreferences sharedPreferences = getSharedPrefs(context);
         return sharedPreferences.getBoolean(AppConstants.ChannelPrefs.SKY_SPORTS, false);
    }

    public static boolean searchEspn(Context context){
         SharedPreferences sharedPreferences = getSharedPrefs(context);
         return sharedPreferences.getBoolean(AppConstants.ChannelPrefs.ESPN, false);
    }

    /**
     * get the boolean preference to determine whether to auto update the shows every few minutes
     * @param context
     * @return
     */
    public static boolean getAutoUpdate(Context context){
        return getSharedPrefs(context).getBoolean(AppConstants.AUTO_UPDATE_PREF, true);
    }

    /**
     * get a shared preferences object from a context
     * @param context
     * @return
     */
    public static SharedPreferences getSharedPrefs(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
