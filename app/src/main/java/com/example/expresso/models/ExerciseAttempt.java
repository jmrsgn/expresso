package com.example.expresso.models;

public class ExerciseAttempt {
    private String date;
    private String remarks;
    private String exerciseID;
    private String code;

    public ExerciseAttempt(String date, String remarks, String exerciseID, String code) {
        this.date = date;
        this.remarks = remarks;
        this.exerciseID = exerciseID;
        this.code = code;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getExerciseID() {
        return exerciseID;
    }

    public void setExerciseID(String exerciseID) {
        this.exerciseID = exerciseID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
