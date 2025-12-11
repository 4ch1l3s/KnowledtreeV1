package com.example.knowledtree; // Hoặc package chứa lớp PlateResponse của bạn

import com.google.gson.annotations.SerializedName;

public class PlateResponse {

    // Trường này ánh xạ "plate_number" từ JSON
    @SerializedName("plate_number")
    private String plateNumber;

    // Trường này ánh xạ "status" (chỉ xuất hiện trong response lỗi hoặc xe đã check-in)
    @SerializedName("status")
    private String status;

    // Thêm các trường khác nếu bạn muốn dùng (ví dụ: tong_tien, checkin_time, id...)
    // @SerializedName("tong_tien")
    // private int totalPrice;

    // ===================================
    // GETTERS (Bắt buộc để giải quyết lỗi)
    // ===================================

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getStatus() {
        // Trả về status. Nếu không có (response thành công), nó sẽ là null.
        return status;
    }

    // Setter (Thường không cần thiết cho lớp response)
    // public void setPlateNumber(String plateNumber) {
    //     this.plateNumber = plateNumber;
    // }
}