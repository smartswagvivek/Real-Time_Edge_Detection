#include <jni.h>
#include <android/log.h>

// OpenCV core headers
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
    jsize length = env->GetArrayLength(frameData);

    LOGI("Received frame | bytes = %d | width = %d | height = %d",
         length, width, height);


    // STEP 1: Convert Y-plane to OpenCV grayscale Mat
    // Frame buffer is YUV_420_888; we use only the Y-plane (grayscale)
    unsigned char* yPlane = reinterpret_cast<unsigned char*>(data);

    // Create grayscale Mat from Y-plane
    cv::Mat gray(height, width, CV_8UC1, yPlane);

    // Log matrix details
    LOGI("Gray Mat created: %dx%d | channels = %d",
         gray.rows, gray.cols, gray.channels());

    // ------------------------------------------
    // END OF COMMIT 1
    // Only grayscale conversion, no edge detection yet.
    // ------------------------------------------

    // Release Java byte array
    env->ReleaseByteArrayElements(frameData, data, JNI_ABORT);
}
