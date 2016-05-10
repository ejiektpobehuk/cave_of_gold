#include <jni.h>

const char * some_c_func(){
    return "9 from deep jni";
}

JNIEXPORT jstring JNICALL
Java_com_poly_ejiek_pitcher_MainActivity_getMsgFromJni(JNIEnv *env, jobject instance) {

   // TODO

   return (*env)->NewStringUTF(env, some_c_func());
}