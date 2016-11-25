#include <jni.h>
#include <algorithm>
#include <string>
#include <vector>
#include <ctime>
#include <android/log.h>

static jclass java_util_ArrayList;
static jmethodID java_util_ArrayList_;
static jmethodID java_util_ArrayList_size;
static jmethodID java_util_ArrayList_get;
static jmethodID java_util_ArrayList_add;
static jclass java_lang_Integer;
static jmethodID java_lang_Integer_intValue;
static JNIEnv* env;

void init(JNIEnv* envIn);
std::vector<int> arrayListToVector(jobject arrayList);


extern "C"
jlong
Java_com_vlad_player_ui_songs_SongsFragment_sum(
        JNIEnv* env,
        jobject /* this */,
        jobject arrayList) {
    init(env);
    std::vector<int> vector = arrayListToVector(arrayList);

    std::clock_t start_time = std::clock();
    long long sum = 0;
    for (int i = 0; i < vector.size(); ++i) {
        sum += vector[i];
    }
    long running_time = (long) ((std::clock() - start_time) / (double)(CLOCKS_PER_SEC / 1000));
    __android_log_print(ANDROID_LOG_DEBUG, "NDK runnning time", "%ld", running_time);

    return sum;
}

void init(JNIEnv* envIn) {
    env = envIn;
    java_util_ArrayList = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    java_util_ArrayList_ = env->GetMethodID(java_util_ArrayList, "<init>", "(I)V");
    java_util_ArrayList_size = env->GetMethodID (java_util_ArrayList, "size", "()I");
    java_util_ArrayList_get = env->GetMethodID(java_util_ArrayList, "get", "(I)Ljava/lang/Object;");
    java_util_ArrayList_add = env->GetMethodID(java_util_ArrayList, "add", "(Ljava/lang/Object;)Z");
    java_lang_Integer = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Integer")));
    java_lang_Integer_intValue = env->GetMethodID(java_lang_Integer, "intValue", "()I");
}

std::vector<int> arrayListToVector(jobject arrayList) {
    unsigned long len = (unsigned long) env->CallIntMethod(arrayList, java_util_ArrayList_size);
    std::vector<int> result;
    result.reserve(len);
    for (jint i = 0; i < len; i++) {
        jobject element = env->CallObjectMethod(arrayList, java_util_ArrayList_get, i);
        int value = env->CallIntMethod(element, java_lang_Integer_intValue);
        result.push_back(value);
    }
    return result;
}
