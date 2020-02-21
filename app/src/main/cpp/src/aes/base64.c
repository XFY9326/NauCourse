#pragma clang diagnostic push
#pragma ide diagnostic ignored "readability-magic-numbers"
#pragma ide diagnostic ignored "cppcoreguidelines-avoid-magic-numbers"
#pragma ide diagnostic ignored "hicpp-signed-bitwise"

#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include "base64.h"

char *b64_encode(const unsigned char *src, size_t len) {
    int i = 0;
    int j = 0;
    char *enc = NULL;
    size_t size = 0;
    unsigned char buf[4];
    unsigned char tmp[3];

    enc = (char *) malloc(0);
    if (NULL == enc) { return NULL; }

    while (len--) {
        tmp[i++] = *(src++);

        if (3 == i) {
            buf[0] = (unsigned char) ((tmp[0] & 0xfc) >> 2);
            buf[1] = (unsigned char) (((tmp[0] & 0x03) << 4) + ((tmp[1] & 0xf0) >> 4));
            buf[2] = (unsigned char) (((tmp[1] & 0x0f) << 2) + ((tmp[2] & 0xc0) >> 6));
            buf[3] = (unsigned char) (tmp[2] & 0x3f);

            enc = (char *) realloc(enc, size + 4);
            for (i = 0; i < 4; ++i) {
                enc[size++] = b64_table[buf[i]];
            }

            i = 0;
        }
    }

    if (i > 0) {
        for (j = i; j < 3; ++j) {
            tmp[j] = '\0';
        }

        buf[0] = (unsigned char) ((tmp[0] & 0xfc) >> 2);
        buf[1] = (unsigned char) (((tmp[0] & 0x03) << 4) + ((tmp[1] & 0xf0) >> 4));
        buf[2] = (unsigned char) (((tmp[1] & 0x0f) << 2) + ((tmp[2] & 0xc0) >> 6));
        buf[3] = (unsigned char) (tmp[2] & 0x3f);

        for (j = 0; (j < i + 1); ++j) {
            enc = (char *) realloc(enc, size + 1);
            enc[size++] = b64_table[buf[j]];
        }

        while ((i++ < 3)) {
            enc = (char *) realloc(enc, size + 1);
            enc[size++] = '=';
        }
    }

    enc = (char *) realloc(enc, size + 1);
    enc[size] = '\0';

    return enc;
}


unsigned char *b64_decode_ex(const char *src, size_t len, size_t *decsize) {
    int i = 0;
    int j = 0;
    int l = 0;
    size_t size = 0;
    unsigned char *dec = NULL;
    unsigned char buf[3];
    unsigned char tmp[4];

    dec = (unsigned char *) malloc(0);
    if (NULL == dec) { return NULL; }

    while (len--) {
        if ('=' == src[j]) { break; }
        if (!(isalnum(src[j]) || '+' == src[j] || '/' == src[j])) { break; }

        tmp[i++] = (unsigned char) src[j++];

        if (4 == i) {
            for (i = 0; i < 4; ++i) {
                for (l = 0; l < 64; ++l) {
                    if (tmp[i] == b64_table[l]) {
                        tmp[i] = (unsigned char) l;
                        break;
                    }
                }
            }

            buf[0] = (unsigned char) ((tmp[0] << 2) + ((tmp[1] & 0x30) >> 4));
            buf[1] = (unsigned char) (((tmp[1] & 0xf) << 4) + ((tmp[2] & 0x3c) >> 2));
            buf[2] = (unsigned char) (((tmp[2] & 0x3) << 6) + tmp[3]);

            dec = (unsigned char *) realloc(dec, size + 3);
            for (i = 0; i < 3; ++i) {
                dec[size++] = buf[i];
            }

            i = 0;
        }
    }

    if (i > 0) {
        for (j = i; j < 4; ++j) {
            tmp[j] = '\0';
        }

        for (j = 0; j < 4; ++j) {
            for (l = 0; l < 64; ++l) {
                if (tmp[j] == b64_table[l]) {
                    tmp[j] = (unsigned char) l;
                    break;
                }
            }
        }

        buf[0] = (unsigned char) ((tmp[0] << 2) + ((tmp[1] & 0x30) >> 4));
        buf[1] = (unsigned char) (((tmp[1] & 0xf) << 4) + ((tmp[2] & 0x3c) >> 2));
        buf[2] = (unsigned char) (((tmp[2] & 0x3) << 6) + tmp[3]);

        dec = (unsigned char *) realloc(dec, size + (i - 1));
        for (j = 0; (j < i - 1); ++j) {
            dec[size++] = buf[j];
        }
    }

    dec = (unsigned char *) realloc(dec, size + 1);
    dec[size] = '\0';

    if (decsize != NULL) *decsize = size;

    return dec;
}

#pragma clang diagnostic pop