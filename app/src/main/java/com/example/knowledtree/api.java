package com.example.knowledtree;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.knowledtree.PlateResponse;


public class api {

    private static final String BASE_URL = "http://192.168.1.232:8000/";
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
    // Interface chứa API endpoints
    // =======================================
    public interface ApiInterface {

        // 1) API gửi ảnh -> /detect
        @Multipart
        @POST("detect")
        Call<PlateResponse> uploadImage(
                @Part MultipartBody.Part image
        );

        // 2) API cập nhật trạng thái -> /update_status
        @Multipart
        @POST("update_status")
        Call<PlateResponse> updateStatus(
                @Part("bien_so") RequestBody bienSo,
                @Part("trang_thai") RequestBody trangThai
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
