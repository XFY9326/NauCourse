# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

add_library(
        Secure

        SHARED

        src/aes/aes.c
        src/aes/aes.h
        src/aes/base64.c
        src/aes/base64.h
        src/encryptUtils.cpp
)

find_library(
        log-lib

        log
)

target_link_libraries(
        Secure

        ${log-lib}
)