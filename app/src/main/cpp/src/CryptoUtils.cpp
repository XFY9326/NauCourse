#include <jni.h>
#include <sys/ptrace.h>
#include "KeyGenerator.h"
#include "RC4.h"

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    ptrace(PTRACE_TRACEME, 0, 0, 0);

    JNIEnv *env = nullptr;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return result;
    }

    KeyGenerator::initAlphaTable();

    return JNI_VERSION_1_6;
}

char *encryptContent(const char *content, const char *param, int contentLen, int paramLen, int &outputLen) {
    int keySpace = KeyGenerator::getKeySpace(paramLen);
    char key[keySpace];
    KeyGenerator::getKey(param, paramLen, key);

    return RC4::encrypt(content, key, contentLen, keySpace - 1, outputLen);
}

char *decryptContent(const char *content, const char *param, int contentLen, int paramLen, int &outputLen) {
    int keySpace = KeyGenerator::getKeySpace(paramLen);
    char key[keySpace];
    KeyGenerator::getKey(param, paramLen, key);

    return RC4::decrypt(content, key, contentLen, keySpace - 1, outputLen);
}

extern "C" JNIEXPORT jstring JNICALL
Java_tool_xfy9326_naucourses_utils_secure_CryptoUtils_encryptText(JNIEnv *env, jobject, jstring content, jstring key) {
    const char *contentBuf = env->GetStringUTFChars(content, nullptr);
    const char *keyBuf = env->GetStringUTFChars(key, nullptr);
    int contentLen = env->GetStringUTFLength(content);
    int keyLen = env->GetStringUTFLength(key);

    int outputLen;
    char *result = encryptContent(contentBuf, keyBuf, contentLen, keyLen, outputLen);

    env->ReleaseStringUTFChars(content, contentBuf);
    env->ReleaseStringUTFChars(key, keyBuf);

    jstring output = env->NewStringUTF(result);
    delete[] result;
    return output;
}

extern "C" JNIEXPORT jstring JNICALL
Java_tool_xfy9326_naucourses_utils_secure_CryptoUtils_decryptText(JNIEnv *env, jobject, jstring content, jstring key) {
    const char *contentBuf = env->GetStringUTFChars(content, nullptr);
    const char *keyBuf = env->GetStringUTFChars(key, nullptr);
    int contentLen = env->GetStringUTFLength(content);
    int keyLen = env->GetStringUTFLength(key);

    int outputLen;
    char *result = decryptContent(contentBuf, keyBuf, contentLen, keyLen, outputLen);

    env->ReleaseStringUTFChars(content, contentBuf);
    env->ReleaseStringUTFChars(key, keyBuf);

    jstring output = env->NewStringUTF(result);
    delete[] result;
    return output;
}