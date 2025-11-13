cmake_minimum_required(VERSION 3.22.1)
project("edgedetectionviewer")

# OpenCV include path
include_directories(${CMAKE_CURRENT_LIST_DIR}/include)



# Import OpenCV shared library
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(opencv_java4 PROPERTIES
        IMPORTED_LOCATION ${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java4.so
)

# Your C++ source file
add_library(${CMAKE_PROJECT_NAME} SHARED
        edgedetectionviewer.cpp
)

# NDK libraries
find_library(log-lib log)
find_library(android-lib android)

# Link OpenCV + Android native libs
target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        opencv_java4
        ${log-lib}
        ${android-lib}
)
