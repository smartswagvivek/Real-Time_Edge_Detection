package com.example.edgedetectionviewer.gl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer renderer;

    // Used when inflating from XML
    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // OpenGL ES 2.0
        setEGLContextClientVersion(2);

        renderer = new MyGLRenderer();
        setRenderer(renderer);

        // Only render when we say so
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLRenderer getRenderer() {
        return renderer;
    }

    // Called from MainActivity when a new edge frame is ready
    public void updateFrame(Bitmap bmp) {
        renderer.updateBitmap(bmp);
        requestRender();
    }
}
