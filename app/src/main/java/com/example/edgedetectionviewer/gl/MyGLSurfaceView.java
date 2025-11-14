package com.example.edgedetectionviewer.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer renderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Use OpenGL ES 2.0
        setEGLContextClientVersion(2);

        renderer = new MyGLRenderer();
        setRenderer(renderer);

        // Render on-demand; MainActivity will call requestRender()
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public MyGLRenderer getRenderer() {
        return renderer;
    }
}
