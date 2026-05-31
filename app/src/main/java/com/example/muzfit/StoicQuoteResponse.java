package com.example.muzfit;

import com.google.gson.annotations.SerializedName;

public class StoicQuoteResponse {
    @SerializedName("data")
    private StoicQuote data;

    public StoicQuote getData() { return data; }
}
