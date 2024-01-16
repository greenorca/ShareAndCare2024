package com.greenorca.shareandcare2024.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FirebaseShare implements Serializable {

    public static final String PROP_DONE = "done";
    public static final String PROP_TOPICS = "topics";
    public static final String PROP_NOTES = "notes";
    public static final String PROP_TAGS = "tags";


    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object s){
        return s.getClass().equals(getClass()) && ((FirebaseShare)s).uuid.equals(this.uuid);
    }

    private String uuid;
    private String title;
    private String url=null;
    private List<String> tags;
    private List<String> topics;

    public FirebaseShare(String uuid, String title, String url, List<String> tags, List<String> topics, String notes, String sender, Date date, boolean isOpen) {
        this.uuid = uuid;
        this.title = title;
        this.url = url;
        this.tags = tags;
        this.topics = topics;
        this.notes = notes;
        this.sender = sender;
        this.date = date;
        this.isOpen = isOpen;
    }

    private String notes;
    private String sender;
    private Date date;
    private boolean isOpen;

    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
    public String getUuid() { return uuid; }
    public String getTitle() {
        return title;
    }
    public List<String> getTopics(){ return topics; };
    public List<String> getTags(){ return tags; };
    public String getSender(){
        return sender;
    }
    public Date getDate(){
        return date;
    }
    public String getUrl() {
        return url;
    }
    public String getNotes() {
        return notes;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FirebaseShare(){
        //DO NOTHING CLUB
    }

    public FirebaseShare(String title, String sender){
        uuid = UUID.randomUUID().toString();
        this.title = title;
        this.sender = sender;
        this.tags = new ArrayList<>();
        this.topics = new ArrayList<>();
        this.date = new Date();
        isOpen = true;
    }

    public void setTopics(List<String> topics){
        this.topics = topics;
    }

    public void setTags(List<String> tags){
        this.tags = tags;
    }

    public void setNotes(String notes){
        this.notes = notes;
    }

    public void setSender(String sender){ this.sender = sender; }

    public void setId(String id) {
        this.uuid = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
