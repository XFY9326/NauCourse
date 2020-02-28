//
// Created by 逸雪飞扬 on 2020/2/26.
//

#ifndef NAUCOURSE_RC4_H
#define NAUCOURSE_RC4_H

#include <iostream>
#include <string>
#include <sys/types.h>

class RC4 {
public:
    static char *encrypt(const char *content, const char *key, int contentLen, int keyLen, int &outputLen);

    static char *decrypt(const char *content, const char *key, int contentLen, int keyLen, int &outputLen);

private:
    static void getKey(const u_char *key, int keyLen, u_char *output);

    static void run(const u_char *content, int contentLen, const u_char *key, int keyLen, u_char *output, int *outputLen);

    static char *byteToHex(const u_char *content, const int contentLen);

    static u_char *hexToByte(const char *hexContent);

    static void swap(u_char &p1, u_char &p2);
};

#endif //NAUCOURSE_RC4_H
