package com.example.knowledtree.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.knowledtree.PlateResponse;
import com.example.knowledtree.api;
import com.example.knowledtree.databinding.FragmentHomeBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private YuvToRgbConverter yuvConverter;

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
        yuvConverter = new YuvToRgbConverter(requireContext());

        startCamera();

        binding.btnCapture.setOnClickListener(v -> capturePhoto());
    }


    // ========================
    // START CAMERA
    // ========================
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
                        .setTargetRotation(binding.cameraView.getDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }


    // ========================
    // CAPTURE PHOTO (NO CRASH)
    // ========================
    private void capturePhoto() {

        if (imageCapture == null) {
            Toast.makeText(requireContext(), "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        imageCapture.takePicture(
                cameraExecutor,   // Xử lý NỀN — KHÔNG crash
                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        try {
                            Bitmap bitmap = yuvConverter.convert(imageProxy);
                            imageProxy.close();

                            if (bitmap == null) {
                                requireActivity().runOnUiThread(() ->
                                        binding.txtResult.setText("Convert ảnh lỗi!"));
                                return;
                            }

                            byte[] data = bitmapToBytes(bitmap);

                            // Upload / OCR…
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


    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteBufferOutStream bos = new ByteBufferOutStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        return bos.toByteArray();
    }


    private void uploadToServer(byte[] imgBytes) {
        binding.txtResult.setText("Đã chụp ảnh, đang upload...");
        // TODO: bạn thay API ở đây
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
        binding = null;
    }
}
