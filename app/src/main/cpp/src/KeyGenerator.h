//
// Created by 逸雪飞扬 on 2020/2/26.
//

#ifndef NAUCOURSE_KEY_GENERATOR_H
#define NAUCOURSE_KEY_GENERATOR_H

#include <sys/types.h>

class KeyGenerator {
public:
    static void initAlphaTable();

    static int getKeySpace(int paramLen);

    static void getKey(const char *param, int paramLen, char *result);
};

#endif //NAUCOURSE_KEY_GENERATOR_H
