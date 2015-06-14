package me.skipr.skipr.model;

import android.graphics.drawable.Drawable;

import com.andtinder.model.CardModel;

/**
 * Created by Navjot on 6/13/2015.
 */
public class SongCard extends CardModel {
    private String mUniqueId;
    public void setUniqueId(String uniqueId){
        mUniqueId = uniqueId;
    }

    public String getUniqueId(){
        return mUniqueId;
    }

    public SongCard(String uniqueId, String title, String desc, Drawable image){
        super(title, desc, image);
        setUniqueId(uniqueId);
    }
}
