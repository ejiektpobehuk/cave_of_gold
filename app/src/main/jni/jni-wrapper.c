#include <jni.h>
#include "awesome.h"

JNIEXPORT jstring JNICALL
Java_com_poly_ejiek_pitcher_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance) {

   // TODO

   return (*env)->NewStringUTF(env, some_c_func());
}