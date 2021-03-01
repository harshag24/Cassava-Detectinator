package com.example.se_ui;

public class model {

String url, prediction;

     model(){

    }

    public model(String url, String prediction) {
        this.url = url;
        this.prediction = prediction;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }
}
