# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

aux_source_directory(src DIR_MAIN)

add_library(
        Secure

        SHARED

        ${DIR_MAIN}
)

find_library(
        log-lib

        log
)

target_link_libraries(
        Secure

        ${log-lib}
)