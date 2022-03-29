package com.example.expresso.models;

public class Module {
    private String id;
    private String title;
    private String slug;
    private String photo;
    private String pathIndex;
    private String description;

    public Module(String id, String title, String slug, String photo, String pathIndex, String description) {
        this.id = id;
        this.title = title;
        this.slug = slug;
        this.photo = photo;
        this.pathIndex = pathIndex;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPathIndex() {
        return pathIndex;
    }

    public void setPathIndex(String pathIndex) {
        this.pathIndex = pathIndex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
