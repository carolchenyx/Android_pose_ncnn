#include "NanoPose.h"
#include <string>
bool NanoPose::hasGPU = true;
bool NanoPose::toUseGPU = true;
NanoPose *NanoPose::detector = nullptr;

inline float fast_exp(float x) {
    union {
        uint32_t i;
        float f;
    } v{};
    v.i = (1 << 23) * (1.4426950409 * x + 126.93490512f);
    return v.f;
}

inline float sigmoid(float x) {
    return 1.0f / (1.0f + fast_exp(-x));
}

template<typename _Tp>
int activation_function_softmax(const _Tp *src, _Tp *dst, int length) {
    const _Tp alpha = *std::max_element(src, src + length);
    _Tp denominator{0};

    for (int i = 0; i < length; ++i) {
        dst[i] = fast_exp(src[i] - alpha);
        denominator += dst[i];
    }

    for (int i = 0; i < length; ++i) {
        dst[i] /= denominator;
    }

    return 0;
}

NanoPose::NanoPose(AAssetManager *mgr, const char *param, const char *bin, bool useGPU) {
    hasGPU = ncnn::get_gpu_count() > 0;
    toUseGPU = hasGPU && useGPU;

    NanoDetNet = new ncnn::Net();
    // opt 需要在加载前设置
    NanoDetNet->opt.use_vulkan_compute = toUseGPU;  // gpu
    NanoDetNet->opt.use_fp16_arithmetic = true;  // fp16运算加速
    NanoDetNet->opt.use_fp16_packed = true;
    NanoDetNet->opt.use_fp16_storage = true;
    NanoDetNet->load_param(mgr, param);
    NanoDetNet->load_model(mgr, bin);

    SppeNet = new ncnn::Net();
    SppeNet->opt.use_vulkan_compute = toUseGPU;  // gpu
    //SppeNet->opt.use_fp16_arithmetic = true;  // fp16运算加速
    //SppeNet->opt.use_packing_layout = true;
    //SppeNet->opt.use_fp16_packed = true;

//    SppeNet->load_param(mgr, "mobilepose_nograd.param");
//    SppeNet->load_model(mgr, "mobilepose_nograd.bin");

//    SppeNet->load_param(mgr, "mobilepose_yoga.param");
//    SppeNet->load_model(mgr, "mobilepose_yoga.bin");

    SppeNet->load_param(mgr, "25.param");
    SppeNet->load_model(mgr, "25.bin");
}

NanoPose::~NanoPose() {
    NanoDetNet->clear();
    SppeNet->clear();
    delete NanoDetNet;
    delete SppeNet;
}


int NanoPose::runpose(cv::Mat &roi, int pose_size_w, int pose_size_h, std::vector<KeyPoint> &keypoints,
                      float x1, float y1) {
    int w = roi.cols;
    int h = roi.rows;
    ncnn::Mat in = ncnn::Mat::from_pixels_resize(roi.data, ncnn::Mat::PIXEL_BGR2RGB, \
                                                 roi.cols, roi.rows, pose_size_w, pose_size_h);
//    LOGD("in w:%d h:%d", roi.cols, roi.rows);
    //数据预处理
    const float mean_vals[3] = {0.485f * 255.f, 0.456f * 255.f, 0.406f * 255.f};
    const float norm_vals[3] = {1 / 0.229f / 255.f, 1 / 0.224f / 255.f, 1 / 0.225f / 255.f};
    in.substract_mean_normalize(mean_vals, norm_vals);

    auto ex = SppeNet->create_extractor();
    ex.set_light_mode(true);
    ex.set_num_threads(4);
    if (toUseGPU) {  // 消除提示
        ex.set_vulkan_compute(toUseGPU);
    }
//    ex.input("data", in);
//    ncnn::Mat out;
//    ex.extract("hybridsequential0_conv7_fwd", out);
    ex.input("input.1", in);
    ncnn::Mat out;
    ex.extract("497", out);
    keypoints.clear();
//    LOGD("pose out.c:%d", out.c);
    for (int p = 0; p < out.c; p++) {
        const ncnn::Mat m = out.channel(p);

        float max_prob = 0.f;
        int max_x = 0;
        int max_y = 0;
        for (int y = 0; y < out.h; y++) {
            const float *ptr = m.row(y);
            for (int x = 0; x < out.w; x++) {
                float prob = ptr[x];
                if (prob > max_prob) {
                    max_prob = prob;
                    max_x = x;
                    max_y = y;
                }
            }
        }

        KeyPoint keypoint;
        keypoint.p = cv::Point2f(max_x * w / (float) out.w + x1, max_y * h / (float) out.h + y1);
        keypoint.prob = max_prob;
        keypoints.push_back(keypoint);
    }
    return 0;
}

BoxInfo NanoPose::getlargestbox(std::vector<BoxInfo> dets, int img_w, int img_h){
    BoxInfo largestbox;
    int id = 0;
    int max_area_id = 0;
    float max_area = 0;
    for (BoxInfo bi: dets) {
        float x1, y1, x2, y2;
        float pw, ph;
        float area;
        x1 = bi.x1;
        y1 = bi.y1;
        x2 = bi.x2;
        y2 = bi.y2;
        if(x2>x1){
            LOGD("bbox@1: Normal");
        }
        else{
            LOGD("bbox@1: SHIT");
        }
        pw = x2 - x1;
        ph = y2 - y1;
        area = pw*ph;
        if(area>max_area)
        {
            max_area = area;
            max_area_id = id;
        }
        id++;
    }

    if(max_area > 10)
    {
        BoxInfo lb = dets[max_area_id];
        float x1, y1, x2, y2, score, label;
        float pw, ph, cx, cy;

        x1 = lb.x1;
        y1 = lb.y1;
        x2 = lb.x2;
        y2 = lb.y2;

        pw = x2 - x1;
        ph = y2 - y1;
        cx = x1 + 0.5 * pw;
        cy = y1 + 0.5 * ph;

        x1 = cx - 0.8 * pw;
        y1 = cy - 0.7 * ph;
        x2 = cx + 0.8 * pw;
        y2 = cy + 0.7 * ph;

        score = lb.score;
        label = lb.label;

        //处理坐标越界问题
        if (x1 < 0) x1 = 0;
        if (y1 < 0) y1 = 0;
        if (x2 < 0) x2 = 0;
        if (y2 < 0) y2 = 0;

        if (x1 > img_w) x1 = img_w;
        if (y1 > img_h) y1 = img_h;
        if (x2 > img_w) x2 = img_w;
        if (y2 > img_h) y2 = img_h;

        largestbox = BoxInfo{x1,y1,x2,y2,score,(int)label};

    }
    else{
        largestbox = BoxInfo{0,0,0,0,0,-1};
    }

    return largestbox;

}

void NanoPose::preprocess(JNIEnv *env, jobject image, ncnn::Mat &in) {

    in = ncnn::Mat::from_android_bitmap_resize(env, image, ncnn::Mat::PIXEL_RGBA2BGR, input_size, input_size);
//    in = ncnn::Mat::from_pixels(image.data, ncnn::Mat::PIXEL_BGR, img_w, img_h);
    //in = ncnn::Mat::from_pixels_resize(image.data, ncnn::Mat::PIXEL_BGR, img_w, img_h, this->input_width, this->input_height);

    const float mean_vals[3] = {103.53f, 116.28f, 123.675f};
    const float norm_vals[3] = {0.017429f, 0.017507f, 0.01712475};
    in.substract_mean_normalize(mean_vals, norm_vals);
}


std::vector<PoseResult> NanoPose::detect(JNIEnv *env, jobject image, float score_threshold, float nms_threshold) {
    AndroidBitmapInfo img_size;
    AndroidBitmap_getInfo(env, image, &img_size);
    int img_w = img_size.width;
    int img_h = img_size.height;

    LOGD("img_w : %d", img_w);
    LOGD("img_h : %d", img_h);

    ncnn::Mat src_img = ncnn::Mat::from_android_bitmap_resize(env, image, ncnn::Mat::PIXEL_RGBA2RGB,
                                                              img_size.width, img_size.height);
    cv::Mat bgr(src_img.h, src_img.w, CV_8UC3);
    src_img.to_pixels(bgr.data, ncnn::Mat::PIXEL_RGB2BGR);

    float width_ratio = (float) img_size.width / (float) input_size;
    float height_ratio = (float) img_size.height / (float) input_size;

    ncnn::Mat input;
    preprocess(env, image, input);

    auto ex = NanoDetNet->create_extractor();
    ex.set_light_mode(true);
    ex.set_num_threads(3);
    if (toUseGPU) {  // 消除提示
        ex.set_vulkan_compute(toUseGPU);
    }
    ex.input("input.1", input);
    std::vector<std::vector<BoxInfo>> results;
    results.resize(this->num_class);

    for (const auto &head_info : this->heads_info) {
        ncnn::Mat dis_pred;
        ncnn::Mat cls_pred;
        ex.extract(head_info.dis_layer.c_str(), dis_pred);
        ex.extract(head_info.cls_layer.c_str(), cls_pred);

        decode_infer(cls_pred, dis_pred, head_info.stride, score_threshold, results, width_ratio, height_ratio);
    }

    std::vector<BoxInfo> dets;
    for (int i = 0; i < (int) results.size(); i++) {
        nms(results[i], nms_threshold);

        for (auto box : results[i]) {
            dets.push_back(box);
        }
    }

    std::vector<PoseResult> poseResults;

    BoxInfo lb = getlargestbox(dets, img_w, img_h);
    if(dets.size() > 0){
        cv::Mat roi;
        std::vector<KeyPoint> keypoints;
        if(lb.label != -1){
            roi = bgr(cv::Rect(lb.x1, lb.y1, lb.x2 - lb.x1, lb.y2 - lb.y1)).clone();
            runpose(roi, pose_size_width, pose_size_height, keypoints, lb.x1, lb.y1);
        }

        PoseResult poseResult;
        poseResult.keyPoints = keypoints;
        poseResult.boxInfos = lb;
        poseResults.push_back(poseResult);
    }

    return poseResults;
}


void NanoPose::decode_infer(ncnn::Mat &cls_pred, ncnn::Mat &dis_pred, int stride, float threshold,
                           std::vector<std::vector<BoxInfo>> &results, float width_ratio, float height_ratio) {
    int feature_h = input_size / stride;
    int feature_w = input_size / stride;

    //cv::Mat debug_heatmap = cv::Mat(feature_h, feature_w, CV_8UC3);
    for (int idx = 0; idx < feature_h * feature_w; idx++) {
        const float *scores = cls_pred.row(idx);
        int row = idx / feature_w;
        int col = idx % feature_w;
        float score = 0;
        int cur_label = 0;
        for (int label = 0; label < num_class; label++) {
            if (scores[label] > score) {
                score = scores[label];
                cur_label = label;
            }
        }
        if (score > threshold) {
            //std::cout << "label:" << cur_label << " score:" << score << std::endl;
            const float *bbox_pred = dis_pred.row(idx);
            results[cur_label].push_back(
                    this->disPred2Bbox(bbox_pred, cur_label, score, col, row, stride, width_ratio, height_ratio));
            //debug_heatmap.at<cv::Vec3b>(row, col)[0] = 255;
            //cv::imshow("debug", debug_heatmap);
        }

    }
}

BoxInfo NanoPose::disPred2Bbox(const float *&dfl_det, int label, float score, int x, int y, int stride, float width_ratio,
                              float height_ratio) {
    float ct_x = (x + 0.5) * stride;
    float ct_y = (y + 0.5) * stride;
    std::vector<float> dis_pred;
    dis_pred.resize(4);
    for (int i = 0; i < 4; i++) {
        float dis = 0;
        float *dis_after_sm = new float[reg_max + 1];
        activation_function_softmax(dfl_det + i * (reg_max + 1), dis_after_sm, reg_max + 1);
        for (int j = 0; j < reg_max + 1; j++) {
            dis += j * dis_after_sm[j];
        }
        dis *= stride;
        //std::cout << "dis:" << dis << std::endl;
        dis_pred[i] = dis;
        delete[] dis_after_sm;
    }
    float xmin = (std::max)(ct_x - dis_pred[0], .0f) * width_ratio;
    float ymin = (std::max)(ct_y - dis_pred[1], .0f) * height_ratio;
    float xmax = (std::min)(ct_x + dis_pred[2], (float) input_size) * width_ratio;
    float ymax = (std::min)(ct_y + dis_pred[3], (float) input_size) * height_ratio;

    //std::cout << xmin << "," << ymin << "," << xmax << "," << xmax << "," << std::endl;
    return BoxInfo{xmin, ymin, xmax, ymax, score, label};
}

void NanoPose::nms(std::vector<BoxInfo> &input_boxes, float NMS_THRESH) {
    std::sort(input_boxes.begin(), input_boxes.end(), [](BoxInfo a, BoxInfo b) { return a.score > b.score; });
    std::vector<float> vArea(input_boxes.size());
    for (int i = 0; i < int(input_boxes.size()); ++i) {
        vArea[i] = (input_boxes.at(i).x2 - input_boxes.at(i).x1 + 1)
                   * (input_boxes.at(i).y2 - input_boxes.at(i).y1 + 1);
    }
    for (int i = 0; i < int(input_boxes.size()); ++i) {
        for (int j = i + 1; j < int(input_boxes.size());) {
            float xx1 = (std::max)(input_boxes[i].x1, input_boxes[j].x1);
            float yy1 = (std::max)(input_boxes[i].y1, input_boxes[j].y1);
            float xx2 = (std::min)(input_boxes[i].x2, input_boxes[j].x2);
            float yy2 = (std::min)(input_boxes[i].y2, input_boxes[j].y2);
            float w = (std::max)(float(0), xx2 - xx1 + 1);
            float h = (std::max)(float(0), yy2 - yy1 + 1);
            float inter = w * h;
            float ovr = inter / (vArea[i] + vArea[j] - inter);
            if (ovr >= NMS_THRESH) {
                input_boxes.erase(input_boxes.begin() + j);
                vArea.erase(vArea.begin() + j);
            } else {
                j++;
            }
        }
    }
}
