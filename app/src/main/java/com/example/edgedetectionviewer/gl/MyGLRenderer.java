package com.example.edgedetectionviewer.gl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private int[] textureId = new int[1];
    private Bitmap pendingBitmap = null;

    // Full-screen quad (NDC coordinates)
    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;

    private int program;
    private int positionHandle;
    private int texCoordHandle;
    private int textureHandle;

    private static final float[] QUAD_VERTICES = {
            -1f, -1f,
            1f, -1f,
            -1f,  1f,
            1f,  1f
    };

    private static final float[] QUAD_UVS = {
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        vertexBuffer = makeFloatBuffer(QUAD_VERTICES);
        uvBuffer = makeFloatBuffer(QUAD_UVS);

        String vertexShaderCode =
                "attribute vec2 aPos;" +
                        "attribute vec2 aUV;" +
                        "varying vec2 vUV;" +
                        "void main() {" +
                        "  gl_Position = vec4(aPos, 0.0, 1.0);" +
                        "  vUV = aUV;" +
                        "}";

        // We use alpha channel from the texture and show it as grayscale
        String fragmentShaderCode =
                "precision mediump float;" +
                        "varying vec2 vUV;" +
                        "uniform sampler2D uTex;" +
                        "void main() {" +
                        "  float a = texture2D(uTex, vUV).a;" +
                        "  gl_FragColor = vec4(a, a, a, 1.0);" +
                        "}";

        program = createProgram(vertexShaderCode, fragmentShaderCode);
        positionHandle = GLES20.glGetAttribLocation(program, "aPos");
        texCoordHandle = GLES20.glGetAttribLocation(program, "aUV");
        textureHandle = GLES20.glGetUniformLocation(program, "uTex");

        // Create texture
        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if (pendingBitmap != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, pendingBitmap, 0);
            // optional: pendingBitmap.recycle();
            pendingBitmap = null;
        }

        if (textureId[0] == 0) return;

        GLES20.glUseProgram(program);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, uvBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
        GLES20.glUniform1i(textureHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void updateBitmap(Bitmap bmp) {
        // Called from GL surface view
        this.pendingBitmap = bmp;
    }

    // ==================== helpers ====================

    private FloatBuffer makeFloatBuffer(float[] data) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(data.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buffer.put(data).position(0);
        return buffer;
    }

    private int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private int createProgram(String vertexCode, String fragmentCode) {
        int vs = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode);
        int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode);
        int prog = GLES20.glCreateProgram();
        GLES20.glAttachShader(prog, vs);
        GLES20.glAttachShader(prog, fs);
        GLES20.glLinkProgram(prog);
        return prog;
    }
}
