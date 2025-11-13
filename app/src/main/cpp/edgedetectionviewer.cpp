#include <jni.h>
#include <android/log.h>

#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "NativeFrame"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_example_edgedetectionviewer_MainActivity_processFrame(
        JNIEnv *env,
        jobject thiz,
        jbyteArray frameData,
        jint width,
        jint height) {

    // Convert Java byte[] to C++ pointer
    jbyte *data = env->GetByteArrayElements(frameData, nullptr);

    // Y-plane from YUV camera frame -> grayscale Mat
    unsigned char* yPlane = reinterpret_cast<unsigned char*>(data);
    cv::Mat gray(height, width, CV_8UC1, yPlane);

    LOGI("Gray Mat: %dx%d channels=%d", gray.cols, gray.rows, gray.channels());

    // --- STEP 2: Canny Edge Detection ---
    cv::Mat edges;
    cv::Canny(gray, edges, 80, 160);

    LOGI("Edges Mat created: %dx%d", edges.cols, edges.rows);

    // --- STEP 3: Send processed edges back to Java ---
    int size = width * height;     // 1 byte per pixel (grayscale)
    jbyteArray edgeArray = env->NewByteArray(size);

    env->SetByteArrayRegion(
            edgeArray, 0, size,
            reinterpret_cast<jbyte*>(edges.data)
    );

    // Get Java class
    jclass cls = env->GetObjectClass(thiz);

    // Find callback method
    jmethodID methodID = env->GetMethodID(
            cls,
            "onFrameProcessed",
            "([BII)V"
    );

    // Call Java method
    env->CallVoidMethod(thiz, methodID, edgeArray, width, height);

    // Cleanup
    env->ReleaseByteArrayElements(frameData, data, JNI_ABORT);
}
