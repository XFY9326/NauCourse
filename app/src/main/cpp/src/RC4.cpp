#include "RC4.h"

#define RC4_MAX_LEN 256

void RC4::getKey(const u_char *key, int keyLen, u_char *output) {
    int i;
    for (i = 0; i < RC4_MAX_LEN; i++) {
        output[i] = static_cast<u_char>(i);
    }
    i = 0;
    for (int j = 0; i < RC4_MAX_LEN; i++) {
        j = (key[i % keyLen] + output[i] + j) % RC4_MAX_LEN;
        swap(output[i], output[j]);
    }
}

void RC4::run(const u_char *content, int contentLen, const u_char *key, int keyLen, u_char *output, int *outputLen) {
    auto *realKey = new u_char[RC4_MAX_LEN];
    getKey(key, keyLen, realKey);

    for (int i = 0, x = 0, y = 0; i < contentLen; i++) {
        x = (x + 1) % RC4_MAX_LEN;
        y = (realKey[x] + y) % RC4_MAX_LEN;
        swap(realKey[x], realKey[y]);
        output[i] = content[i] ^ realKey[(realKey[x] + realKey[y]) % RC4_MAX_LEN];
    }
    *outputLen = contentLen;
    delete[] realKey;
}

char *RC4::encrypt(const char *content, const char *key, int contentLen, int keyLen, int &outputLen) {
    auto *outputBuffer = new u_char[contentLen];
    int outputBufferLen = 0;
    run((u_char *) content, contentLen, (u_char *) key, keyLen, outputBuffer, &outputBufferLen);
    char *output = byteToHex(outputBuffer, outputBufferLen);
    outputLen = ++outputBufferLen;
    delete[] outputBuffer;
    return output;
}

char *RC4::decrypt(const char *content, const char *key, int contentLen, int keyLen, int &outputLen) {
    u_char *byteBuffer = hexToByte(content);
    auto *output = new u_char[contentLen / 2 + 1];
    outputLen = 0;
    memset(output, 0, contentLen / 2 + 1);
    run(byteBuffer, contentLen / 2, (u_char *) key, keyLen, output, &outputLen);
    output[outputLen++] = '\0';
    delete[] byteBuffer;
    return (char *) output;
}

void RC4::swap(u_char &p1, u_char &p2) {
    u_char temp = p1;
    p1 = p2;
    p2 = temp;
}

char *RC4::byteToHex(const u_char *content, const int contentLen) {
    if (!content) {
        return nullptr;
    }
    char *temp = new char[contentLen * 2 + 1];
    int temp2;
    for (int i = 0; i < contentLen; i++) {
        temp2 = (int) (content[i]) / 16;
        temp[i * 2] = (char) (temp2 + ((temp2 > 9) ? 'A' - 10 : '0'));
        temp2 = (int) (content[i]) % 16;
        temp[i * 2 + 1] = (char) (temp2 + ((temp2 > 9) ? 'A' - 10 : '0'));
    }
    temp[contentLen * 2] = '\0';
    return temp;
}

u_char *RC4::hexToByte(const char *hexContent) {
    if (!hexContent) {
        return nullptr;
    }
    int hexLen = static_cast<int>(strlen(hexContent));
    if (hexLen <= 0 || 0 != hexLen % 2) {
        return nullptr;
    }
    auto *buffer = new u_char[hexLen / 2];
    int temp1, temp2;
    for (int i = 0; i < hexLen / 2; i++) {
        temp1 = (int) hexContent[i * 2] - (((int) hexContent[i * 2] >= 'A') ? 'A' - 10 : '0');
        if (temp1 >= 16) {
            return nullptr;
        }
        temp2 = (int) hexContent[i * 2 + 1] - (((int) hexContent[i * 2 + 1] >= 'A') ? 'A' - 10 : '0');
        if (temp2 >= 16) {
            return nullptr;
        }
        buffer[i] = static_cast<u_char>(temp1 * 16 + temp2);
    }
    return buffer;
}