package com.example.muzfit;

import com.google.gson.annotations.SerializedName;

public class StoicQuote {
    @SerializedName("author")
    private String author;
    @SerializedName("quote")
    private String quote;

    public String getAuthor() { return author; }
    public String getQuote() { return quote; }
}
