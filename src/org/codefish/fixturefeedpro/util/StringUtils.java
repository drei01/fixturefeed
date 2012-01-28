/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codefish.fixturefeedpro.util;

import java.util.List;

/**
 *
 * @author Matthew
 */
public class StringUtils {
     /**
     * implode for regex match
     *
     * @param objs  List of objects to implode (elements may not be null)
     * @param delim String to place inbetween elements
     * @return A string with objects in the list seperated by delim
     */
    public static String implode(List<String> objs, String delim) {
        StringBuilder buf = new StringBuilder();
        int size = objs.size();

        for(int i =0; i<objs.size(); i++){
            buf.append(objs.get(i));
            if(i<(objs.size()-1)){
                buf.append(delim);
            }
        }

        return buf.toString();
    }

}
