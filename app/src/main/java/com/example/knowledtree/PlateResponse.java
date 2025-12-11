package com.example.knowledtree; // Hoặc package chứa lớp PlateResponse của bạn

import com.google.gson.annotations.SerializedName;

public class PlateResponse {

    // Trường này ánh xạ plate_number từ JSON
    @SerializedName("plate_number")
    private String plateNumber;

    // Trường này ánh xạ "status" (chỉ xuất hiện trong response lỗi hoặc xe đã check-in)
    @SerializedName("status")
    private String status;


    public String getPlateNumber() {
        return plateNumber;
    }

    public String getStatus() {
        return status;
    }

}