package com.example.knowledtree;

import com.google.gson.annotations.SerializedName;

public class ParkingResponse {

    @SerializedName("plate_number")
    private String plateNumber;

    @SerializedName("status")
    private String status;

    // Các trường API /xe-ra
    @SerializedName("checkin_time")
    private String checkinTime;

    @SerializedName("checkout_time")
    private String checkoutTime;

    @SerializedName("tong_tien")
    private int totalPrice;

    @SerializedName("so_gio_tinh_tien")
    private int calculatedHours;



    // Getters and Setters
    public String getPlateNumber() {
        return plateNumber;
    }

    public String getStatus() {
        return status;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    // ... (Thêm các getters khác nếu cần)
}