
#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/glue/CollectionGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/ModelAnalyserGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/ProductDESResultGlue.h"

#include "waters/base/IntTypes.h"
#include "waters/des/GlobalAlphabet.h"
#include "waters/javah/Invocations.h"


namespace waters {


void initGlobalAlphabet(const jni::ProductDESGlue& des, jni::ClassCache* cache)
{
  uint32 numprops = 0;
  uint32 numuncont = 0;
  uint32 numcont = 0;
  
  const jni::CollectionGlue events = des.getEventsGlue(cache);
  const int numevents = events.size();
  jni::EventGlue* eventarray =
    (jni::EventGlue*) new char[numevents * sizeof(jni::EventGlue)];
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  for (int i = 0; i < numevents; i++) {
    jobject javaobject = iter.next();
    jni::EventGlue* event = &eventarray[i];
    new (event) jni::EventGlue(javaobject, cache);
    switch (event->getKindGlue(cache)) {
    case jni::EventKind_PROPOSITION:
      numprops++;
      break;
    case jni::EventKind_UNCONTROLLABLE:
      numuncont++;
      break;
    case jni::EventKind_CONTROLLABLE:
      numcont++;
      break;
    }
  }

  GlobalAlphabet alphabet(numprops, numuncont, numcont);
  CodeIterator propiter = alphabet.propositionIterator();
  CodeIterator uncontiter = alphabet.uncontrollableIterator();
  CodeIterator contiter = alphabet.controllableIterator();
  for (int i = 0; i < numevents; i++) {
    const jni::EventGlue* event = &eventarray[i];
    uint32 code;
    switch (event->getKindGlue(cache)) {
    case jni::EventKind_PROPOSITION:
      code = propiter.next();
      break;
    case jni::EventKind_UNCONTROLLABLE:
      code = uncontiter.next();
      break;
    case jni::EventKind_CONTROLLABLE:
      code = contiter.next();
      break;
    default:
      continue;
    }
    jstring name = event->getName();
    alphabet.initEventName(code, name);
  }

  for (int i = 0; i < numevents; i++) {
    jni::EventGlue* event = &eventarray[i];
    event->~EventGlue();
  }
  delete [] (char*) eventarray;
}


}   /* namespace waters */


JNIEXPORT jobject JNICALL 
Java_net_sourceforge_waters_model_analysis_ProductDESCopier_callNativeMethod
  (JNIEnv* env, jobject copier)
{
  try {
    jni::ClassCache cache(env);
    jni::ModelAnalyserGlue analyser(copier, &cache);
    jni::ProductDESGlue des = analyser.getInputGlue(&cache);
    jstring name = des.getName();
    waters::initGlobalAlphabet(des, &cache);
    jni::ProductDESGlue copy(name, &cache);
    jni::ProductDESResultGlue result(false, &copy, &cache);
    return result.returnJavaObject();
  } catch (jthrowable exception) {
    return 0;
  }
}
