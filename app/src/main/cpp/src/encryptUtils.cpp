#include <jni.h>
#include <string>
#include <sys/ptrace.h>
#include "aes/aes.h"

constexpr int TABLELEN = 62;
char alphaTable[TABLELEN];

static inline void init();

static inline void generateAlphaTable(char result[TABLELEN]);


JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *) {
    ptrace(PTRACE_TRACEME, 0, 0, 0);

    JNIEnv *env = nullptr;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }

    init();

    return JNI_VERSION_1_4;
}

static inline void init() {
    generateAlphaTable(alphaTable);
}

static inline void generateAlphaTable(char result[TABLELEN]) {
    char c = '0';
    int p = 0;
    for (; c <= '9'; c++, p++) {
        result[p] = c;
    }
    c = 'a';
    for (; c <= 'z'; c++, p++) {
        result[p] = c;
    }
    c = 'A';
    for (; c <= 'Z'; c++, p++) {
        result[p] = c;
    }
}

static inline void generateKey(const char *keyChars, int keyLen, char *result, int resultLen) {
    int offset = 0;

    for (int i = keyLen; i >= 0; i--) {
        auto p = (unsigned int) keyChars[i];
        result[i] = alphaTable[(offset + (p >> (sizeof(keyChars[i]) / 2))) % TABLELEN];
        result[resultLen - 1 - i] = alphaTable[(offset + (p << (sizeof(keyChars[i]) / 2))) % TABLELEN];
        offset = result[i] + result[resultLen - 1 - i];
    }

    result[resultLen - 1] = '\0';
}

char *encryptContent(const char *content, const char *key) {
    int keyLen = strlen(key);
    int resultLen = keyLen * 2 + 1;
    char generatedKey[resultLen];
    generateKey(key, keyLen, generatedKey, resultLen);
    return AES_128_ECB_PKCS5Padding_Encrypt(content, generatedKey);
}

char *decryptContent(const char *content, const char *key) {
    int keyLen = strlen(key);
    int resultLen = keyLen * 2 + 1;
    char generatedKey[resultLen];
    generateKey(key, keyLen, generatedKey, resultLen);
    return AES_128_ECB_PKCS5Padding_Decrypt(content, generatedKey);
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"
extern "C" JNIEXPORT jstring JNICALL Java_tool_xfy9326_naucourses_utils_secure_EncryptUtils_encryptText(JNIEnv *env, jobject, jstring content,
                                                                                                        jstring key) {
    const char *contentCharArr = env->GetStringUTFChars(content, nullptr);
    const char *keyCharArr = env->GetStringUTFChars(key, nullptr);

    const char *result = encryptContent(contentCharArr, keyCharArr);

    return env->NewStringUTF(result);
}

extern "C" JNIEXPORT jstring JNICALL Java_tool_xfy9326_naucourses_utils_secure_EncryptUtils_decryptText(JNIEnv *env, jobject, jstring content,
                                                                                                        jstring key) {
    const char *contentCharArr = env->GetStringUTFChars(content, nullptr);
    const char *keyCharArr = env->GetStringUTFChars(key, nullptr);

    const char *result = decryptContent(contentCharArr, keyCharArr);

    return env->NewStringUTF(result);
}
#pragma clang diagnostic pop