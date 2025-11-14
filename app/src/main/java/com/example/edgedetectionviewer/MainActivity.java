package com.example.edgedetectionviewer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.edgedetectionviewer.gl.MyGLSurfaceView;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("edgedetectionviewer");
    }

    // JNI bridge
    public native void processFrame(byte[] frameData, int width, int height);

    private TextureView textureView;          // Camera preview (background - optional)
    private MyGLSurfaceView glSurfaceView;    // OpenGL edge output (foreground)

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        glSurfaceView = findViewById(R.id.glSurfaceView);

        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    //   CALLBACK FROM C++ (JNI)
    public void onFrameProcessed(byte[] edgeData, int width, int height) {

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(edgeData));

        // JNI callback is not guaranteed to be on UI thread → hop to UI:
        runOnUiThread(() -> glSurfaceView.updateFrame(bmp));
    }

    //        CAMERA SETUP
    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    openCamera();
                }

                @Override public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture s, int w, int h) {}
                @Override public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture s) { return false; }
                @Override public void onSurfaceTextureUpdated(@NonNull SurfaceTexture s) {}
            };

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, 100);
                return;
            }

            manager.openCamera(cameraId, stateCallback, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startCameraPreview();
                }

                @Override public void onDisconnected(@NonNull CameraDevice camera) {}
                @Override public void onError(@NonNull CameraDevice camera, int error) {}
            };

    private void startCameraPreview() {

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(1920, 1080);
        Surface previewSurface = new Surface(surfaceTexture);

        imageReader = ImageReader.newInstance(
                1920,
                1080,
                ImageFormat.YUV_420_888,
                2
        );

        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireNextImage();

            if (image != null) {
                byte[] frameBytes = convertYUVToByteArray(image);
                processFrame(frameBytes, image.getWidth(), image.getHeight());
                image.close();
            }

        }, null);

        Surface imageSurface = imageReader.getSurface();

        try {
            CaptureRequest.Builder builder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            builder.addTarget(previewSurface);  // camera shown (if not covered)
            builder.addTarget(imageSurface);    // frames to C++

            cameraDevice.createCaptureSession(
                    Arrays.asList(previewSurface, imageSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSession = session;
                            try {
                                session.setRepeatingRequest(builder.build(), null, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
                    },
                    null
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //   CORRECT Y PLANE EXTRACTION
    private byte[] convertYUVToByteArray(Image image) {

        Image.Plane yPlane = image.getPlanes()[0];
        ByteBuffer yBuffer = yPlane.getBuffer();

        int width = image.getWidth();
        int height = image.getHeight();

        int rowStride = yPlane.getRowStride();
        int pixelStride = yPlane.getPixelStride();

        byte[] yBytes = new byte[width * height];
        int pos = 0;

        for (int row = 0; row < height; row++) {
            int rowStart = row * rowStride;

            for (int col = 0; col < width; col++) {
                yBytes[pos++] = yBuffer.get(rowStart + col * pixelStride);
            }
        }

        return yBytes;
    }
}
