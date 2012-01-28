/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codefish.fixturefeedpro.tv;

import java.io.Serializable;
import java.util.Date;
import org.codefish.fixturefeedpro.util.DateUtil;

/**
 * Object to define a tv program
 * @author Matthew
 */
public class TVListing implements Serializable{
    private Integer id;
    private String channelName;
    private String title;
    private String subTitle;
    private String description;
    private Date start;
    private Date end;
    private int duration;
    private String channelLogo;
    private boolean addedToCal = false;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Deprecated
    public String getSub_Title() {
        return subTitle;
    }

    @Deprecated
    public void setSub_Title(String sub_Title) {
        this.subTitle = sub_Title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChannelLogo() {
        return channelLogo;
    }

    public void setChannelLogo(String channelLogo) {
        this.channelLogo = channelLogo;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
        if(this.end==null){
            this.end = start;
        }
    }

    public boolean isAddedToCal() {
        return addedToCal;
    }

    public void setAddedToCal(boolean addedToCal) {
        this.addedToCal = addedToCal;
    }



    /**
     * set the time portion of a date to the current start date
     * @param start
     */
    public void setStart_Time(Date startTime){
        if(this.start==null){
            this.start=startTime;
        }else{
            this.start = DateUtil.setTime(this.start, startTime);
        }
    }

    /**
     * set the time portion of the date to the current end date
     * @param endTime
     */
    public void setEnd_Time(Date endTime){
        if(this.end==null){
            this.end=endTime;
        }else{
            this.end = DateUtil.setTime(this.end, endTime);
        }
    }


    

    @Override
    public String toString() {
            return this.title;
    }

    @Override
    public int hashCode() {
            return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
            if(obj == null) return false;
            if(obj instanceof TVListing){
                return this.getTitle().equals(((TVListing)obj).getTitle()) &&
                        this.getStart().compareTo(((TVListing)obj).getStart())==0;
            }
            return false;
    }
      // sort by date
    public int compareTo(TVListing another) {
        if (another == null) return 1;
        return another.getId().compareTo(this.getId());
    }

}
