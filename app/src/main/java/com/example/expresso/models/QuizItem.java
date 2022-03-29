package com.example.expresso.models;

public class QuizItem {
    private String id;
    private String question;
    private String photo;
    private String score;
    private String quizID;

    public QuizItem(String id, String question, String photo, String score, String quizID) {
        this.id = id;
        this.question = question;
        this.photo = photo;
        this.score = score;
        this.quizID = quizID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getQuizID() {
        return quizID;
    }

    public void setQuizID(String quizID) {
        this.quizID = quizID;
    }
}
