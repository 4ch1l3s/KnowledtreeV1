package com.example.knowledtree;

import com.google.gson.annotations.SerializedName;

public class ParkingHistoryItem {

    @SerializedName("id")
    private int id;

    @SerializedName("action")
    private String action; // "checkin" hoặc "checkout"

    @SerializedName("timestamp")
    private String timestamp;

    // ------------------- GETTERS -------------------
    public int getId() { return id; }
    public String getAction() { return action; }
    public String getTimestamp() { return timestamp; }
}