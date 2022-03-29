package com.example.expresso.models;

public class Quiz {
    private String id;
    private String moduleID;
    private String title;
    private String items;
    private String passing;

    public Quiz(String id, String moduleID, String title, String passing, String items) {
        this.id = id;
        this.moduleID = moduleID;
        this.title = title;
        this.passing = passing;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getPassing() {
        return passing;
    }

    public void setPassing(String passing) {
        this.passing = passing;
    }
}
