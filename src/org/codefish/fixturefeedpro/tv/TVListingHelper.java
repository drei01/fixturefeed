/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codefish.fixturefeedpro.tv;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.codefish.fixturefeedpro.util.DateUtil;

/**
 * Utility class to persist a TVListing object to xml and back again
 * @author Matthew
 *
 * @src http://www.devx.com/Java/Article/9931/1954
 */
public class TVListingHelper {

    public static void write(ArrayList<TVListing> f, String filename, Context c) throws Exception {
        // Use a FileOutputStream to send data to a file
        // called myobject.data.
        FileOutputStream f_out = c.openFileOutput(filename, Context.MODE_PRIVATE);

        // Use an ObjectOutputStream to send object data to the
        // FileOutputStream for writing to disk.
        ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

        // Pass our object to the ObjectOutputStream's
        // writeObject() method to cause it to be written out
        // to disk.
        obj_out.writeObject(f);


        //close the output stream
        obj_out.close();
    }

    public static ArrayList<TVListing> read(String filename, int maxAge, Context c) throws Exception {
        try{
        File file = new File(filename);
        //check the data is not too old
        if(file.exists() && file.lastModified() < DateUtil.getDaysAgo(maxAge).getTime()){
            return null;
        }
        }catch(Exception e){
            //do nothing with a null pointer
        }

        // Read from disk using FileInputStream.
        FileInputStream f_in = c.openFileInput(filename);

        // Read object using ObjectInputStream.
        ObjectInputStream obj_in = new ObjectInputStream(f_in);

        return  (ArrayList<TVListing>)obj_in.readObject();

    }
}
