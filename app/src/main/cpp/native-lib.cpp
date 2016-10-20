#include <jni.h>
#include <algorithm>
#include <string>
#include <vector>

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
static JNIEnv* env;

std::wstring jstr2cstr(jstring string);
jstring cstr2jstr(std::wstring cstr);
void init(JNIEnv* envIn);
std::vector<std::string> arrayListToVector(jobject arrayList);
jobject vectorToArrayList(std::vector<std::string> vector);


extern "C"
jobject
Java_com_fisko_music_ui_albums_AlbumsActivity_uppercase(
        JNIEnv* env,
        jobject /* this */,
        jobject arrayList) {
    init(env);
    std::vector<std::string> vector = arrayListToVector(arrayList);
    for (std::string &str: vector) {
        std::transform(str.begin(), str.end(), str.begin(), ::toupper);
    }

    return vectorToArrayList(vector);
}

void init(JNIEnv* envIn) {
    env = envIn;
    java_util_ArrayList      = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));
    java_util_ArrayList_     = env->GetMethodID(java_util_ArrayList, "<init>", "(I)V");
    java_util_ArrayList_size = env->GetMethodID (java_util_ArrayList, "size", "()I");
    java_util_ArrayList_get  = env->GetMethodID(java_util_ArrayList, "get", "(I)Ljava/lang/Object;");
    java_util_ArrayList_add  = env->GetMethodID(java_util_ArrayList, "add", "(Ljava/lang/Object;)Z");
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

std::vector<std::string> arrayListToVector(jobject arrayList) {
    unsigned long len = (unsigned long) env->CallIntMethod(arrayList, java_util_ArrayList_size);
    std::vector<std::string> result;
    result.reserve(len);
    for (jint i = 0; i < len; i++) {
        jstring element = static_cast<jstring>(env->CallObjectMethod(arrayList, java_util_ArrayList_get, i));
        const char *nativeString = env->GetStringUTFChars(element, 0);
        result.push_back(nativeString);
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
