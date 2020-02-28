//
// Created by 逸雪飞扬 on 2020/2/26.
//

#include "KeyGenerator.h"

const int TABLE_LEN = 62;

u_char alphaTable[TABLE_LEN];

void KeyGenerator::initAlphaTable() {
    int p = 0;
    u_char c;
    for (c = 'A'; c <= 'Z'; c++, p++) {
        alphaTable[p] = c;
    }
    for (c = 'a'; c <= 'z'; c++, p++) {
        alphaTable[p] = c;
    }
    for (c = '0'; c <= '9'; c++, p++) {
        alphaTable[p] = c;
    }
}

int KeyGenerator::getKeySpace(int paramLen) {
    return paramLen * 2 + 1;
}

void KeyGenerator::getKey(const char *param, int paramLen, char *result) {
    int ivLen = getKeySpace(paramLen);
    int offset = 0;

    for (int i = paramLen - 1; i >= 0; i--) {
        int p = param[i];
        result[i] = alphaTable[(offset + (p >> 4)) % TABLE_LEN];
        result[ivLen - 2 - i] = alphaTable[(offset + (p << 4)) % TABLE_LEN];
        offset = result[i] + result[ivLen - 2 - i];
    }

    result[ivLen - 1] = '\0';
}