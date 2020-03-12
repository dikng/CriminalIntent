package com.bignerdranch.android.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }
    public String getPhotoFilename(){
        return "IMG_" + getId().toString() + ".jdp";
    }

    public String getSuspect() {
        return mSuspect;
    }

    public Crime(){
        this(UUID.randomUUID());
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public Date getDate() {
        return mDate;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
    }
}
