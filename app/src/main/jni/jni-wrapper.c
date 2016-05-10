#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_poly_ejiek_pitcher_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance) {

   // TODO

   return (*env)->NewStringUTF(env, "9 from jni");
}