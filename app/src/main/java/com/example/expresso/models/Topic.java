package com.example.expresso.models;

public class Topic {
    private String id;
    private String title;
    private String moduleID;
    private String slug;
    private String pathIndex;
    private String content;
    private String description;

    public Topic(String id, String title, String moduleID, String slug, String pathIndex, String content, String description) {
        this.id = id;
        this.title = title;
        this.moduleID = moduleID;
        this.slug = slug;
        this.pathIndex = pathIndex;
        this.content = content;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(String pathIndex) {
        this.pathIndex = pathIndex;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


