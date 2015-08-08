package com.timmo.notes;

class Note {

    //private variables
    private int _id;
    private String _title;
    private String _content;
    private String _metadata;

    // Empty constructor
    public Note() {

    }

    // constructor
    public Note(int id, String title, String content, String metadata) {
        this._id = id;
        this._title = title;
        this._content = content;
        this._metadata = metadata;
    }

    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public String getTitle() {
        return this._title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public String getContent() {
        return this._content;
    }

    public void setContent(String content) {
        this._content = content;
    }

    public String getMetadata() {
        return this._metadata;
    }

    public void setMetadata(String metadata) {
        this._metadata = metadata;
    }

}