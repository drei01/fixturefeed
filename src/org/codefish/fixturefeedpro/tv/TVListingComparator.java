/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codefish.fixturefeedpro.tv;

import java.util.Comparator;
import java.util.Date;

/**
 * a custom comparator to sort tvlistings into dat order
 * @author Matthew
 */
public class TVListingComparator implements Comparator {

    public int compare(Object list1, Object list2) {
        Date date1 = ((TVListing) list1).getStart();
        Date date2 = ((TVListing) list2).getStart();

        return date1.compareTo(date2);
    }
}
