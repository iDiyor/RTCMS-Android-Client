package com.vodiytechnologies.rtcmsclient;

/**
 * Created by Diyor on 8/10/2015.
 */
public class Message {
    private long id;
    private boolean mIsSelf;
    private String mContent;
    private int mUserId;
    private String mTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getSelf() {
        return mIsSelf;
    }

    public void setSelf(boolean mIsSelf) {
        this.mIsSelf = mIsSelf;
    }


    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }


    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int mUserId) {
        this.mUserId = mUserId;
    }


    public String getTime() {
        return mTime;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }
}
