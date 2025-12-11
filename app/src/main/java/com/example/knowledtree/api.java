package com.example.knowledtree;

import java.util.List; // Import cho List<ParkingRecord>
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET; // Import cho phương thức GET
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query; // Import cho tham số truy vấn Query
import retrofit2.converter.gson.GsonConverterFactory;


public class api {

    private static final String BASE_URL = "http://192.168.0.104:8000/"; // Phải thay đổi mỗi lần đổi IP
    private static Retrofit retrofit = null;
    private static ApiInterface apiInterface = null;

    public static ApiInterface getApi() {
        if (apiInterface == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiInterface = retrofit.create(ApiInterface.class);
        }
        return apiInterface;
    }

    // =======================================
    // Interface chứa API endpoints (ĐÃ CẬP NHẬT)
    // =======================================
    public interface ApiInterface {

        // ... (Các API cũ giữ nguyên) ...

        // 1) API gửi ảnh -> /detect (Gốc)
        @Multipart
        @POST("detect")
        Call<PlateResponse> uploadImage(
                @Part MultipartBody.Part image
        );

        // 2) API cập nhật trạng thái -> /update_status (Gốc)
        @Multipart
        @POST("update_status")
        Call<PlateResponse> updateStatus(
                @Part("bien_so") RequestBody bienSo,
                @Part("trang_thai") RequestBody trangThai
        );

        // 3) API Xe Vào (Check-in) -> /xe-vao (ĐÃ THÊM MỚI)
        @Multipart
        @POST("/xe-vao")
        Call<PlateResponse> checkInVehicle(
                @Part MultipartBody.Part image
        );

        // 4) API Xe Ra (Check-out) -> /xe-ra (ĐÃ THÊM MỚI)
        @Multipart
        @POST("/xe-ra")
        Call<PlateResponse> checkOutVehicle(
                @Part MultipartBody.Part image
        );

        // 🆕 5) API Lịch Sử -> /history (GET)
        @GET("history")
        Call<List<ParkingRecord>> getHistory( // SỬ DỤNG MODEL ParkingRecord
                                              @Query("status") String status,
                                              @Query("plate") String plate
        );
    }

    public static RequestBody toRequest(String text) {
        return RequestBody.create(MediaType.parse("text/plain"), text);
    }

    public static MultipartBody.Part toImagePart(String fileName, byte[] imageBytes) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        return MultipartBody.Part.createFormData("image", fileName, requestBody);
    }
}