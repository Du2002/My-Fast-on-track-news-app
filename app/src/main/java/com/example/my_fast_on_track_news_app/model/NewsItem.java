package com.example.my_fast_on_track_news_app.model;

public class NewsItem {
    public String imageUrl, title, description, date;

    public NewsItem() {} // Firebase needs empty constructor

    public NewsItem(String imageUrl, String title, String description, String date) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.description = description;
        this.date = date;
    }
}
