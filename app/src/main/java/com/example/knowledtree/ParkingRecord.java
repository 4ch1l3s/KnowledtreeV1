package com.example.knowledtree;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ParkingRecord {

    @SerializedName("id")
    private int id;

    @SerializedName("plate_number")
    private String plateNumber;

    @SerializedName("checkin_time")
    private String checkinTime;

    @SerializedName("checkout_time")
    private String checkoutTime; // Nullable String

    @SerializedName("tong_tien")
    private Integer tongTien; // Nullable Integer

    @SerializedName("so_gio_tinh_tien")
    private Integer calculatedHours;

    @SerializedName("checkin_image_path")
    private String checkinImagePath;

    @SerializedName("checkout_image_path")
    private String checkoutImagePath;

    // Khai báo trường history sử dụng Model phụ
    @SerializedName("history")
    private List<ParkingHistoryItem> history;

    // ------------------- GETTERS -------------------
    public int getId() { return id; }
    public String getPlateNumber() { return plateNumber; }
    public String getCheckinTime() { return checkinTime; }
    public String getCheckoutTime() { return checkoutTime; }
    public Integer getTongTien() { return tongTien; }
    public List<ParkingHistoryItem> getHistory() { return history; }

    // ------------------- Helper Methods -------------------
    public String getStatusText() {
        return checkoutTime == null ? "ĐANG TRONG BÃI" : "ĐÃ CHECK-OUT";
    }

    public String getTongTienText() {
        if (tongTien != null) {
            return String.format("%,d VND", tongTien);
        }
        return "N/A";
    }
}