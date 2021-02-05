//getlargestbox(std::vector<BoxInfo> dets, int img_w, int img_h) returns a vector of int w/ 6 elements x1,y1,x2,y2,width,height of bbox
#ifndef NANOPOSE_H
#define NANOPOSE_H

#include "ncnn/net.h"
#include "YoloV5.h"
#include "SimplePose.h"

#include <opencv2/core/core.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include <stdio.h>
#include <vector>
#include <algorithm>
#include <opencv2/core/types.hpp>

typedef struct HeadInfo {
    std::string cls_layer;
    std::string dis_layer;
    int stride;
};

//struct KeyPoint {
//    cv::Point2f p;
//    float prob;
//};

//struct PoseResult {
//    std::vector<KeyPoint> keyPoints;
//    BoxInfo boxInfos;
//};

class NanoPose {
public:
    NanoPose(AAssetManager *mgr, const char *param, const char *bin, bool useGPU);

    ~NanoPose();

    std::vector<PoseResult> detect(JNIEnv *env, jobject image, float score_threshold, float nms_threshold);

    std::vector<std::string> labels{"person",};
private:
    int runpose(cv::Mat &roi, int pose_size_width, int pose_size_height,
                std::vector<KeyPoint> &keypoints,
                float x1, float y1);

    int pose_size_width = 256;
    int pose_size_height = 320;

    BoxInfo getlargestbox(std::vector<BoxInfo> dets, int img_w, int img_h);

    void preprocess(JNIEnv *env, jobject image, ncnn::Mat &in);

    void decode_infer(ncnn::Mat &cls_pred, ncnn::Mat &dis_pred, int stride, float threshold,
                      std::vector<std::vector<BoxInfo>> &results, float width_ratio, float height_ratio);

    BoxInfo disPred2Bbox(const float *&dfl_det, int label, float score, int x, int y, int stride, float width_ratio,
                         float height_ratio);

    static void nms(std::vector<BoxInfo> &result, float nms_threshold);

    ncnn::Net *NanoDetNet;
    ncnn::Net *SppeNet;
    int input_size = 416;
    int num_class = 1;
    int reg_max = 7;
    // Layer Name for
    std::vector<HeadInfo> heads_info{
            // cls_pred|dis_pred|stride
            {"792", "795", 8},
            {"814", "817", 16},
            {"836", "839", 32},
    };
//    std::vector<HeadInfo> heads_info{
//            // cls_pred|dis_pred|stride
//            {"379", "382", 8},
//            {"401", "404", 16},
//            {"423", "426", 32},
//    };
public:
    static NanoPose *detector;
    static bool hasGPU;
    static bool toUseGPU;
};


#endif //NANODET_H
