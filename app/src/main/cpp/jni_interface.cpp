//
// Created by ningy on 2021/05/03.
//

#include <jni.h>
#include <string>
#include <ncnn/gpu.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "YoloV5.h"
#include "YoloV5CustomLayer.h"

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    ncnn::create_gpu_instance();
    if (ncnn::get_gpu_count() > 0) {
        YoloV5::hasGPU = true;
    }
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
{
    ncnn::destroy_gpu_instance();
    delete YoloV5::detector;
}


extern "C" JNIEXPORT void JNICALL
Java_com_example_yolov5_1ncnn_1android_YOLOv5_init(JNIEnv *env, jclass clazz, jobject assetManager) {
    // TODO: implement init()
    if (YoloV5::detector != nullptr) {
        delete YoloV5::detector;
        YoloV5::detector = nullptr;
    }
    if (YoloV5::detector == nullptr) {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        YoloV5::detector = new YoloV5(mgr, "yolov5.param", "yolov5.bin");
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_yolov5_1ncnn_1android_YOLOv5_detect(JNIEnv *env, jclass clazz, jobject image, jdouble threshold, jdouble nms_threshold) {
    // TODO: implement detect()
    auto result = YoloV5::detector->detect(env, image, threshold, nms_threshold);

    auto box_cls = env->FindClass("com/example/yolov5_ncnn_android/Box");
    auto cid = env->GetMethodID(box_cls, "<init>", "(FFFFIF)V");
    jobjectArray ret = env->NewObjectArray(result.size(), box_cls, nullptr);
    int i = 0;
    for (auto &box:result) {
        env->PushLocalFrame(1);
        jobject obj = env->NewObject(box_cls, cid, box.x1, box.y1, box.x2, box.y2, box.label, box.score);
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }
    return ret;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_yolov5_1ncnn_1android_YOLOv5_initCustomLayer(JNIEnv *env, jclass clazz,jobject assetManager) {
    // TODO: implement initCustomLayer()
    if (YoloV5CustomLayer::detector != nullptr) {
        delete YoloV5CustomLayer::detector;
        YoloV5CustomLayer::detector = nullptr;
    }
    if (YoloV5CustomLayer::detector == nullptr) {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        YoloV5CustomLayer::detector = new YoloV5CustomLayer(mgr, "yolov5-c.param", "yolov5-c.bin");
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_example_yolov5_1ncnn_1android_YOLOv5_detectCustomLayer(JNIEnv *env, jclass clazz, jobject image, jdouble threshold, jdouble nms_threshold) {
    // TODO: implement detectCustomLayer()
    auto result = YoloV5CustomLayer::detector->detect(env, image, threshold, nms_threshold);

    auto box_cls = env->FindClass("com/example/yolov5_ncnn_android/Box");
    auto cid = env->GetMethodID(box_cls, "<init>", "(FFFFIF)V");
    jobjectArray ret = env->NewObjectArray(result.size(), box_cls, nullptr);
    int i = 0;
    for (auto &box:result) {
        env->PushLocalFrame(1);
        jobject obj = env->NewObject(box_cls, cid, box.x1, box.y1, box.x2, box.y2, box.label, box.score);
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }
    return ret;
}