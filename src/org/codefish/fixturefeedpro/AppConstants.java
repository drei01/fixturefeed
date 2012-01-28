/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codefish.fixturefeedpro;

/**
 * Constants for use internally within the app
 * all other constants are stored in strings.xml
 * @author Matthew
 */
public interface AppConstants {
    public static final String KEYWORD_PREF_SPLIT ="-";
    public static final String KEYWORD_SELECTED ="true";
    public static final String KEYWORD_UNSELECTED ="false";

    /*Words to include*/
    public static final String KEYWORD_LIST_PREF ="keywords";
    public static final String KEYWORD_SELECTED_PREF ="selectedkeywords";

    /*Words to exclude*/
    public static final String EXCLUDE_LIST_PREF ="excludewords";
    public static final String EXCLUDE_SELECTED_PREF ="selectedexcludewords";

    public static final String CATEGORY_PREF="categoryPreference";
    public static final String DEFAULT_CAT="sport";
    public static final String SHOWN_WELCOME_PREF="shownWelcome";
    public static final String AUTO_UPDATE_PREF="autoUpdate";
    public static final int CHANNEL_WAIT_TIME = 30000;//number of seconds to wait for a channel to finish loading in the background updaters

    public interface ChannelPrefs{
        public static final String SKY_SPORTS="searchSkySports";
        public static final String ESPN="searchEspn";
    }
}

