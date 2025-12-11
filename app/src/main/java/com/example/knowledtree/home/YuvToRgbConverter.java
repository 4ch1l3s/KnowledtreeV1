package com.example.knowledtree.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;
import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class YuvToRgbConverter {

    // Không cần RenderScript. Constructor chỉ để HomeFragment khởi tạo.
    public YuvToRgbConverter(Context context) {
        // Khởi tạo trống
    }

    public Bitmap convert(ImageProxy image) {
        // Kiểm tra định dạng
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            Log.e("YUV_CONVERT", "Image format is not YUV_420_888");
            return null;
        }

        try {
            // 1. Lấy dữ liệu YUV từ ImageProxy
            ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
            ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
            ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            // Tổng kích thước buffer NV21 (Y + V + U)
            byte[] nv21 = new byte[ySize + uSize + vSize];

            // 2. Sao chép và Sắp xếp lại thành NV21 (Y + VU interleaving)
            yBuffer.get(nv21, 0, ySize);

            // Lấy dữ liệu U và V
            byte[] u = new byte[uSize];
            byte[] v = new byte[vSize];
            uBuffer.get(u, 0, uSize);
            vBuffer.get(v, 0, vSize);

            // Gán UV (NV21 yêu cầu V trước, U sau)
            int index = ySize;
            for (int i = 0; i < uSize; i++) {
                // Sắp xếp VUVU...
                nv21[index++] = v[i];
                nv21[index++] = u[i];
            }

            // 3. Sử dụng YuvImage để chuyển đổi sang JPEG/Bitmap
            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Nén JPEG từ YuvImage
            yuvImage.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, out);
            byte[] imageBytes = out.toByteArray();

            // 4. Giải nén JPEG thành Bitmap
            return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        } catch (Exception e) {
            // Bắt lỗi Java để xem vấn đề gì xảy ra
            Log.e("YUV_CONVERT", "Final conversion failed!", e);
            return null;
        }
    }
}