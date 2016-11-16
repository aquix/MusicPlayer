#include <jni.h>
#include <algorithm>
#include <string>
#include <vector>
#include <ctime>
#include <android/log.h>

//extern "C"
//jstring
//Java_com_fisko_myapplication_MainActivity_stringFromJNI(
//        JNIEnv* env,
//        jobject /* this */) {
//    std::string hello = "Hello from C++";
//    return env->NewStringUTF(hello.c_str());
//}

static jclass java_util_ArrayList;
static jmethodID java_util_ArrayList_;
static jmethodID java_util_ArrayList_size;
static jmethodID java_util_ArrayList_get;
static jmethodID java_util_ArrayList_add;
static jclass java_lang_Integer;
static jmethodID java_lang_Integer_intValue;
static JNIEnv* env;

std::wstring jstr2cstr(jstring string);
jstring cstr2jstr(std::wstring cstr);
void init(JNIEnv* envIn);
std::vector<int> arrayListToVector(jobject arrayList);
jobject vectorToArrayList(std::vector<std::string> vector);


extern "C"
jlong
Java_com_vlad_player_ui_albums_AlbumsFragment_sum(
        JNIEnv* env,
        jobject /* this */,
        jobject arrayList) {
    init(env);
    std::vector<int> vector = arrayListToVector(arrayList);

    std::clock_t start_time = std::clock();
    long long sum = 0;
    for(int j = 0; j < 500; ++j) {
        for (int i = 0; i < vector.size(); ++i) {
            sum += vector[i];
        }
    }
    long running_time = (long) ((std::clock() - start_time) / (double)(CLOCKS_PER_SEC / 1000));
    __android_log_print(ANDROID_LOG_DEBUG, "JNI runnning time", "%ld", running_time);

    return sum;
}

void init(JNIEnv* envIn) {
    env = envIn;
    java_util_ArrayList      = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    java_util_ArrayList_     = env->GetMethodID(java_util_ArrayList, "<init>", "(I)V");
    java_util_ArrayList_size = env->GetMethodID (java_util_ArrayList, "size", "()I");
    java_util_ArrayList_get  = env->GetMethodID(java_util_ArrayList, "get", "(I)Ljava/lang/Object;");
    java_util_ArrayList_add  = env->GetMethodID(java_util_ArrayList, "add", "(Ljava/lang/Object;)Z");
    java_lang_Integer           = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/lang/Integer")));
    java_lang_Integer_intValue  = env->GetMethodID(java_lang_Integer, "intValue", "()I");
}

std::wstring jstr2cstr(jstring string)
{
    std::wstring value;

    const jchar *raw = env->GetStringChars(string, 0);
    jsize len = env->GetStringLength(string);

    value.assign(raw, raw + len);

    env->ReleaseStringChars(string, raw);

    return value;
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

jstring cstr2jstr(std::wstring cstr) {
    unsigned int len = (unsigned int) cstr.size();
    jchar* raw = new jchar[len];
    memcpy(raw, cstr.c_str(), len * sizeof(wchar_t));
    jstring result = env->NewString(raw, len);
    delete[] raw;
    return result;
}


jobject vectorToArrayList(std::vector<std::string> vector) {
    jobject result = env->NewObject(java_util_ArrayList, java_util_ArrayList_, vector.size());
    for (std::string s: vector) {
//        jstring str = cstr2jstr(s);
        jstring str = env->NewStringUTF(s.c_str());
        env->CallBooleanMethod(result, java_util_ArrayList_add, str);
        env->DeleteLocalRef(str);
    }
    return result;
}
