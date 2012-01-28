/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codefish.fixturefeedpro;

import android.R.color;
import org.codefish.fixturefeedpro.tv.TVListing;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.codefish.fixturefeedpro.util.DateUtil;

/**
 *
 * @author Matthew
 */
public class ListingAdapter extends ArrayAdapter<TVListing> {

    private List<TVListing> items = new ArrayList<TVListing>();
    private Context c;
    private static String BASE_URI = "android.resource://org.codefish.fixturefeedpro/drawable/";
    private static final SimpleDateFormat START_DATE_FORMAT = new SimpleDateFormat("E dd MMM yyyy HH:mm");
    private static final SimpleDateFormat END_TIME_FORMAT = new SimpleDateFormat("HH:mm");

    public ListingAdapter(Context context, int textViewResourceId, ArrayList<TVListing> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.c = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TVListing o = items.get(position);

        View v = convertView;
        if (o != null) {
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row, null);
            }

            /**
             * NOTE: we have to set every piece of data about the rows each time the view is reloaded because android has no concept of the previous state
             */

            //set the icon
            ImageView iv = (ImageView) v.findViewById(R.id.icon);
            if (iv != null) {
                if (!"".equals(o.getChannelLogo())) {
                    Uri path = Uri.parse(BASE_URI + o.getChannelLogo());
                    iv.setImageURI(path);
                } else {
                    iv.setImageResource(R.drawable.icon);
                }
            }
            
            //set the text
            TextView tt = (TextView) v.findViewById(R.id.toptext);
            if (tt != null) {
                tt.setText(o.getTitle());

                TextView tdet = (TextView) v.findViewById(R.id.bottomtext);
                if (tdet != null) {
                    tdet.setText(START_DATE_FORMAT.format(o.getStart()) + " - " + END_TIME_FORMAT.format(o.getEnd()));
                }
            }

            //show the calendar icon if this show has been added to the calendar
            ImageView calIcon = (ImageView) v.findViewById(R.id.calIcon);
            if (calIcon != null) {
                calIcon.setVisibility(o.isAddedToCal()?ImageView.VISIBLE:ImageView.INVISIBLE);
            }

            if (DateUtil.isToday(o.getStart())) {//if the show is on today, then use the view with the highlighted colour
                v.setBackgroundColor(R.color.highlight_colour);
            } else {
                v.setBackgroundColor(color.transparent);
            }
        }

        return v;
    }

    public List<TVListing> getItems() {
        return items;
    }

    @Override
    public void clear() {
        super.clear();
        this.items = new ArrayList<TVListing>();//make sure the list of items is empty
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public void add(TVListing object) {
        super.add(object);
        items.add(object);
    }
}
