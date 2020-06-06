FROM ubuntu:20.04

LABEL maintainer="XFY9326@xfy9326.top"

# Environment
ENV DEBIAN_FRONTEND noninteractive

RUN sed -i s@/archive.ubuntu.com/@/mirrors.tuna.tsinghua.edu.cn/@g /etc/apt/sources.list
RUN apt-get clean
RUN apt-get update

RUN apt-get install --assume-yes apt-utils
RUN apt-get upgrade -y --fix-missing
RUN apt-get install git wget curl gcc cmake unzip openjdk-8-jdk -y --fix-missing

# JAVA
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV JRE_HOME ${JAVA_HOME}/jre
ENV CLASSPATH .:${JAVA_HOME}/lib:${JRE_HOME}/lib:${JAVA_HOME}/lib/dt.jar:${JAVA_HOME}/lib/tools.jar
ENV PATH ${JAVA_HOME}/bin:$PATH

# Android SDK
RUN mkdir -p /Android/sdk

ENV ANDROID_SDK /Android/sdk
ENV ANDROID_HOME $ANDROID_SDK
ENV PATH ${ANDROID_SDK}/tools:${ANDROID_SDK}/tools/bin:${ANDROID_SDK}/platform-tools:$PATH

ENV ANDROID_PLATFORM_VERSION 29
ENV ANDROID_BUILD_TOOLS_VERSION 29.0.3
ENV ANDROID_NDK_VERSION 21.2.6472646

WORKDIR /Android

RUN mkdir ~/.android
RUN touch ~/.android/repositories.cfg

RUN wget --no-check-certificate -O ${ANDROID_SDK}/tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-6514223_latest.zip"
RUN unzip -d ${ANDROID_SDK} ${ANDROID_SDK}/tools.zip
RUN rm -rf ${ANDROID_SDK}/tools.zip

RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "tools" 
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "platform-tools"
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "platforms;android-${ANDROID_PLATFORM_VERSION}"
RUN yes | sdkmanager --sdk_root=${ANDROID_HOME} "ndk;${ANDROID_NDK_VERSION}"

