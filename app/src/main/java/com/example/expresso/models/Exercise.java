package com.example.expresso.models;

public class Exercise {
    private String userExerciseID;
    private String id;
    private String title;
    private String difficulty;
    private String moduleID;
    private String description;
    private String slug;
    private String templateCode;
    private String hint;
    private String initialInput;
    private String solution;

    public Exercise(String userExerciseID, String id, String title, String difficulty, String moduleID, String description, String slug, String templateCode, String hint, String initialInput, String solution) {
        this.userExerciseID = userExerciseID;
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.moduleID = moduleID;
        this.description = description;
        this.slug = slug;
        this.templateCode = templateCode;
        this.hint = hint;
        this.initialInput = initialInput;
        this.solution = solution;
    }

    public String getUserExerciseID() {
        return userExerciseID;
    }

    public void setUserExerciseID(String userExerciseID) {
        this.userExerciseID = userExerciseID;
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

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getModuleID() {
        return moduleID;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getInitialInput() {
        return initialInput;
    }

    public void setInitialInput(String initialInput) {
        this.initialInput = initialInput;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}
