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
        JNIEnv *env, jobject thiz, jbyteArray frameData, jint width, jint height) {

    // 1. Get Y plane bytes
    jbyte *data = env->GetByteArrayElements(frameData, nullptr);

    cv::Mat yMat(height, width, CV_8UC1, (unsigned char *)data);

    // 2. Convert to gray (already is)
    cv::Mat gray = yMat;

    // 3. Canny Edge Detection
    cv::Mat edges;
    cv::Canny(gray, edges, 50, 150);

    cv::rotate(edges, edges, cv::ROTATE_90_CLOCKWISE);

    // 4. Convert edges (CV_8UC1) to byte[]
    int size = edges.rows * edges.cols;
    jbyteArray outArray = env->NewByteArray(size);
    env->SetByteArrayRegion(outArray, 0, size, (jbyte *)edges.data);

    // 5. Call Java callback
    jclass cls = env->GetObjectClass(thiz);
    jmethodID method = env->GetMethodID(
            cls, "onFrameProcessed", "([BII)V"
    );

    env->CallVoidMethod(thiz, method, outArray, edges.cols, edges.rows);

    // Cleanup
    env->ReleaseByteArrayElements(frameData, data, JNI_ABORT);
}
