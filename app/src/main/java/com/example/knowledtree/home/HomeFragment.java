package com.example.knowledtree.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

// **********************************************
// IMPORT CẦN THIẾT (ĐÃ CHUẨN HÓA)
// **********************************************
import com.example.knowledtree.api; // Lớp Retrofit Client (chứa getApi() và ApiInterface)
import com.example.knowledtree.PlateResponse; // Lớp Response Model (Cần có getStatus/getPlateNumber)
import com.example.knowledtree.home.YuvToRgbConverter; // Lớp chuyển đổi ảnh (Giả định vị trí trong thư mục home)
import com.example.knowledtree.databinding.FragmentHomeBinding;
// **********************************************

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    // Khai báo kiểu dữ liệu api.ApiInterface (khớp với api.getApi())
    private api.ApiInterface apiService;

    private FragmentHomeBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private YuvToRgbConverter yuvConverter;

    // Contract xử lý kết quả yêu cầu quyền truy cập Camera
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(),
                            "Cần quyền Camera để sử dụng tính năng này!", Toast.LENGTH_LONG).show();
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();
        // Khởi tạo YuvToRgbConverter
        yuvConverter = new YuvToRgbConverter(requireContext());

        // KHỞI TẠO API SERVICE
        apiService = api.getApi();

        // Kiểm tra quyền Camera trước khi khởi tạo
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        binding.btnCapture.setOnClickListener(v -> capturePhoto());
    }


    // ========================
    // START CAMERA
    // ========================
    @SuppressLint("RestrictedApi")
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.cameraView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)


                        // Đây là định dạng bắt buộc cho YuvToRgbConverter hoạt động
                        .setBufferFormat(android.graphics.ImageFormat.YUV_420_888)

                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        (LifecycleOwner) getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                Log.e("CameraX", "Lỗi khởi tạo CameraX", e);
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // ========================
    // CAPTURE PHOTO
    // ========================
    private void capturePhoto() {

        if (imageCapture == null) {
            Toast.makeText(requireContext(), "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        imageCapture.takePicture(
                cameraExecutor,
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        try {
                            // 1. THÊM TOAST THÔNG BÁO CHỤP ẢNH THÀNH CÔNG
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "📸 Đã chụp ảnh, đang xử lý...", Toast.LENGTH_SHORT).show()
                            );

                            Bitmap bitmap = yuvConverter.convert(imageProxy);
                            imageProxy.close();

                            if (bitmap == null) {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "Convert ảnh lỗi!", Toast.LENGTH_SHORT).show()
                                );
                                return;
                            }

                            byte[] data = bitmapToBytes(bitmap);

                            requireActivity().runOnUiThread(() -> {
                                uploadToServer(data);
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Chụp ảnh lỗi", exception);
                    }
                }
        );
    }


    // ========================
    // bitmapToBytes
    // ========================
    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        return bos.toByteArray();
    }


    // ========================
    // GỌI API UPLOAD
    // ========================
    private void uploadToServer(byte[] imgBytes) {
        if (apiService == null) {
            Toast.makeText(requireContext(), "Lỗi: API Service chưa khởi tạo!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Toast thông báo đang gửi ảnh
        // (Đã có một Toast thông báo "Đang gửi ảnh check-in..." sẽ chạy sau Toast "Đã chụp ảnh")
        // Toast.makeText(requireContext(), "Đang gửi ảnh check-in...", Toast.LENGTH_SHORT).show();

        String fileName = "photo_" + System.currentTimeMillis() + ".jpeg";

        // Sử dụng hàm trợ giúp toImagePart
        MultipartBody.Part imagePart = api.toImagePart(fileName, imgBytes);

        // Gọi API Xe Vào (checkInVehicle)
        Call<PlateResponse> call = apiService.checkInVehicle(imagePart);

        call.enqueue(new Callback<PlateResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlateResponse> call, @NonNull Response<PlateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PlateResponse result = response.body();

                    String status = result.getStatus();
                    String bienSo = result.getPlateNumber();
                    String message;

                    if (bienSo == null || "unknown".equals(bienSo)) {
                        message = "⚠️ Lỗi nhận dạng: Không đọc được biển số!";
                    } else if ("xe_da_trong_bai".equals(status)) {
                        message = "❌ Xe " + bienSo + " đã check-in trước đó!";
                    } else {
                        message = "✅ CHECK-IN THÀNH CÔNG: Biển số " + bienSo;
                    }

                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                } else {
                    // Xử lý lỗi HTTP
                    Toast.makeText(requireContext(), "🛑 Lỗi Server. Code: " + response.code(), Toast.LENGTH_LONG).show();
                    Log.e("API_CALL", "Error response: " + response.code() + " " + response.errorBody());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlateResponse> call, @NonNull Throwable t) {
                // Xử lý lỗi kết nối
                Toast.makeText(requireContext(), "🚫 Lỗi kết nối API: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_CALL", "Failure: ", t);
            }
        });
    }


    // ========================
    // LIFECYCLE
    // ========================
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        if (binding != null) {
            binding = null;
        }
    }
}