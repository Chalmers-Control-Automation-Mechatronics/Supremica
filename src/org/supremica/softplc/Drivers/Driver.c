#include <jni.h>
#include <stdio.h>
#include "Driver.h"
#include "dask.h"

JNIEXPORT jshort JNICALL
Java_Driver_RegisterCard(JNIEnv *env, jobject obj, jshort cardid, jshort cardnr)
{
  jshort card;
  if ((card=Register_Card(cardid, cardnr)) < 0) {
      //fprintf(stderr,"Can't open device file\n");
      return -1;
  }
  return card;
}

JNIEXPORT void JNICALL
Java_Driver_ReleaseCard(JNIEnv *env, jobject obj, jshort card)
{
  if (card >= 0) {
    Release_Card(card);
  }
}

JNIEXPORT void JNICALL
Java_Driver_WritePort(JNIEnv *env, jobject obj, jshort card, jshort channel, jlong value)
{
  DO_WritePort(card, channel, value);
}

JNIEXPORT jlong JNICALL
Java_Driver_ReadPort(JNIEnv *env, jobject obj, jshort card, jshort channel)
{
  long input;
  DI_ReadPort(card,  channel, &input);
  return