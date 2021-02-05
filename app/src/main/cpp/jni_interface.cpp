#include <jni.h>
#include <string>
#include <ncnn/gpu.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "YoloV5.h"
#include "SimplePose.h"
#include "YoloV5CustomLayer.h"
#include "NanoPose.h"


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    ncnn::create_gpu_instance();
    if (ncnn::get_gpu_count() > 0) {
        YoloV5::hasGPU = true;
        SimplePose::hasGPU = true;
    }
//    LOGD("jni onload");
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    ncnn::destroy_gpu_instance();
    delete YoloV5::detector;
    delete SimplePose::detector;
//    LOGD("jni onunload");
}


/*********************************************************************************************
                                         Yolov5
 ********************************************************************************************/
extern "C" JNIEXPORT void JNICALL
Java_com_saillab_ncnn_YOLOv5_init(JNIEnv *env, jclass, jobject assetManager, jboolean useGPU) {
    if (YoloV5::detector != nullptr) {
        delete YoloV5::detector;
        YoloV5::detector = nullptr;
    }
    if (YoloV5::detector == nullptr) {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        YoloV5::detector = new YoloV5(mgr, "yolov5.param", "yolov5.bin", useGPU);
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_saillab_ncnn_YOLOv5_detect(JNIEnv *env, jclass, jobject image, jdouble threshold, jdouble nms_threshold) {
    auto result = YoloV5::detector->detect(env, image, threshold, nms_threshold);

    auto box_cls = env->FindClass("com/saillab/ncnn/Box");
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

// ***************************************[ Yolov5 Custom Layer ]****************************************
extern "C" JNIEXPORT void JNICALL
Java_com_saillab_ncnn_YOLOv5_initCustomLayer(JNIEnv *env, jclass, jobject assetManager, jboolean useGPU) {
    if (YoloV5CustomLayer::detector != nullptr) {
        delete YoloV5CustomLayer::detector;
        YoloV5CustomLayer::detector = nullptr;
    }
    if (YoloV5CustomLayer::detector == nullptr) {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        YoloV5CustomLayer::detector = new YoloV5CustomLayer(mgr, "yolov5s_customlayer.param", "yolov5s_customlayer.bin", useGPU);
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_saillab_ncnn_YOLOv5_detectCustomLayer(JNIEnv *env, jclass, jobject image, jdouble threshold, jdouble nms_threshold) {
    auto result = YoloV5CustomLayer::detector->detect(env, image, threshold, nms_threshold);

    auto box_cls = env->FindClass("com/saillab/ncnn/Box");
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

/*********************************************************************************************
                                         SimplePose
 ********************************************************************************************/

extern "C" JNIEXPORT void JNICALL
Java_com_saillab_ncnn_SimplePose_init(JNIEnv *env, jclass clazz, jobject assetManager, jboolean useGPU) {
    if (SimplePose::detector != nullptr) {
        delete SimplePose::detector;
        SimplePose::detector = nullptr;
    }
    if (SimplePose::detector == nullptr) {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        SimplePose::detector = new SimplePose(mgr, useGPU);
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_saillab_ncnn_SimplePose_detect(JNIEnv *env, jclass clazz, jobject image) {
    auto result = SimplePose::detector->detect(env, image);

    auto box_cls = env->FindClass("com/saillab/ncnn/KeyPoint");
    auto cid = env->GetMethodID(box_cls, "<init>", "([F[FFFFFF)V");
    jobjectArray ret = env->NewObjectArray(result.size(), box_cls, nullptr);
    int i = 0;
//    int KEY_NUM = 17;
    int KEY_NUM = 13;
    for (auto &keypoint : result) {
        env->PushLocalFrame(1);
        float x[KEY_NUM];
        float y[KEY_NUM];
        for (int j = 0; j < KEY_NUM; j++) {
            x[j] = keypoint.keyPoints[j].p.x;
            y[j] = keypoint.keyPoints[j].p.y;
        }
        jfloatArray xs = env->NewFloatArray(KEY_NUM);
        env->SetFloatArrayRegion(xs, 0, KEY_NUM, x);
        jfloatArray ys = env->NewFloatArray(KEY_NUM);
        env->SetFloatArrayRegion(ys, 0, KEY_NUM, y);

        jobject obj = env->NewObject(box_cls, cid, xs, ys,
                keypoint.boxInfos.x1, keypoint.boxInfos.y1, keypoint.boxInfos.x2, keypoint.boxInfos.y2,
                keypoint.boxInfos.score);
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }
    return ret;

}
///*********************************************************************************************
//                                         NanoPose
// ********************************************************************************************/
extern "C" JNIEXPORT void JNICALL
Java_com_saillab_ncnn_NanoPose_init(JNIEnv *env, jclass, jobject assetManager, jboolean useGPU) {
    if (NanoPose::detector != nullptr) {
        delete NanoPose::detector;
        NanoPose::detector = nullptr;
    }
    if (NanoPose::detector == nullptr) {
        AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);
        NanoPose::detector = new NanoPose(mgr, "coco-opt.param", "coco-opt.bin", useGPU);//detection
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_saillab_ncnn_NanoPose_detect(JNIEnv *env, jclass, jobject image, jdouble threshold, jdouble nms_threshold) {
    auto result = NanoPose::detector->detect(env, image, threshold, nms_threshold);

    auto box_cls = env->FindClass("com/saillab/ncnn/KeyPoint");
    auto cid = env->GetMethodID(box_cls, "<init>", "([F[FFFFFF)V");
    jobjectArray ret = env->NewObjectArray(result.size(), box_cls, nullptr);
    int i = 0;
//    int KEY_NUM = 17;
    int KEY_NUM = 13;
    for (auto &keypoint : result) {
        env->PushLocalFrame(1);
        float x[KEY_NUM];
        float y[KEY_NUM];
        for (int j = 0; j < KEY_NUM; j++) {
            x[j] = keypoint.keyPoints[j].p.x;
            y[j] = keypoint.keyPoints[j].p.y;
        }
        jfloatArray xs = env->NewFloatArray(KEY_NUM);
        env->SetFloatArrayRegion(xs, 0, KEY_NUM, x);
        jfloatArray ys = env->NewFloatArray(KEY_NUM);
        env->SetFloatArrayRegion(ys, 0, KEY_NUM, y);

        jobject obj = env->NewObject(box_cls, cid, xs, ys,
                                     keypoint.boxInfos.x1, keypoint.boxInfos.y1, keypoint.boxInfos.x2, keypoint.boxInfos.y2,
                                     keypoint.boxInfos.score);
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement(ret, i++, obj);
    }
    return ret;
}
