#ifndef _AES_H_
#define _AES_H_

#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <jni.h>
#include "base64.h"

static const unsigned char HEX[16] = {0x10, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};

#ifdef __cplusplus
extern "C" {
#endif

char *AES_128_ECB_PKCS5Padding_Encrypt(const char *in, char *key);

char *AES_128_ECB_PKCS5Padding_Decrypt(const char *in, char *key);

int findPaddingIndex(uint8_t *out, int len);

#ifdef __cplusplus
}
#endif

#endif
