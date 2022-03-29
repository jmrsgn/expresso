package com.example.expresso.models;

public class TopicQuizChoice {
    private String id;
    private String choice;
    private String isCorrect;
    private String questionID;

    public TopicQuizChoice(String id, String choice, String isCorrect, String questionID) {
        this.id = id;
        this.choice = choice;
        this.isCorrect = isCorrect;
        this.questionID = questionID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(String isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getQuestionID() {
        return questionID;
    }

    public void setQuestionID(String questionID) {
        this.questionID = questionID;
    }
}
