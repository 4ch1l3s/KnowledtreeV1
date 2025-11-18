package com.example.knowledtree.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import androidx.camera.core.ImageProxy;

public class YuvToRgbConverter {

    private final RenderScript rs;
    private final ScriptIntrinsicYuvToRGB scriptYuvToRgb;

    public YuvToRgbConverter(Context context) {
        rs = RenderScript.create(context);
        scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
    }

    public Bitmap convert(ImageProxy image) {

        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        int width = image.getWidth();
        int height = image.getHeight();

        // Tạo buffer NV21 chuẩn
        byte[] yuvBytes = new byte[width * height * 3 / 2];
        ImageProxy.PlaneProxy yPlane = planes[0];
        ImageProxy.PlaneProxy uPlane = planes[1];
        ImageProxy.PlaneProxy vPlane = planes[2];

        int ySize = yPlane.getBuffer().remaining();
        yPlane.getBuffer().get(yuvBytes, 0, ySize);

        int chromaRowStride = uPlane.getRowStride();
        int chromaPixelStride = uPlane.getPixelStride();

        int offset = ySize;

        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {

                int uvOffset = row * chromaRowStride + col * chromaPixelStride;

                yuvBytes[offset++] = vPlane.getBuffer().get(uvOffset);
                yuvBytes[offset++] = uPlane.getBuffer().get(uvOffset);
            }
        }

        // Convert NV21 -> RGB
        Allocation in = Allocation.createSized(rs, Element.U8(rs), yuvBytes.length);
        Allocation out = Allocation.createFromBitmap(rs,
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888));

        in.copyFrom(yuvBytes);
        scriptYuvToRgb.setInput(in);
        scriptYuvToRgb.forEach(out);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bitmap);

        return bitmap;
    }
}
