package com.example.knowledtree;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // API Xe Vào (Check-in)
    @Multipart
    @POST("/xe-vao")
    Call<ParkingResponse> checkInVehicle(@Part MultipartBody.Part image);

    // API Xe Ra (Check-out)
    // Cấu trúc Response của xe-ra phức tạp hơn (có thêm tiền, giờ),
    // nên cần cùng một lớp ParkingResponse để chứa tất cả các trường
    @Multipart
    @POST("/xe-ra")
    Call<ParkingResponse> checkOutVehicle(@Part MultipartBody.Part image);
}