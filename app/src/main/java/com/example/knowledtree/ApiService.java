package com.example.knowledtree;

import java.util.List;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    // API Xe Vào (Check-in)
    @Multipart
    @POST("/xe-vao")
    Call<ParkingResponse> checkInVehicle(@Part MultipartBody.Part image);

    // API Xe Ra (Check-out)
    @Multipart
    @POST("/xe-ra")
    Call<ParkingResponse> checkOutVehicle(@Part MultipartBody.Part image);

    //API Lịch Sử (GET /history)
    @GET("history") // Endpoint history
    Call<List<ParkingRecord>> getHistory(
            // Tham số Query (dùng null để lấy tất cả)
            @Query("status") String status,
            @Query("plate") String plate
    );

}