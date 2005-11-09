
#include <new>

#include <jni.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/CollectionsGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/ProductDESCopierGlue.h"
#include "jni/glue/ProductDESGlue.h"
#include "jni/glue/ProductDESProxyFactoryGlue.h"
#include "jni/glue/ProductDESResultGlue.h"
#include "jni/glue/SetGlue.h"

#include "waters/base/IntTypes.h"
#include "waters/des/GlobalAlphabet.h"
#include "waters/javah/Invocations.h"


namespace waters {


void initGlobalAlphabet(const jni::ProductDESGlue& des, jni::ClassCache* cache)
{
  uint32 numuncont = 0;
  uint32 numcont = 0;
  
  const jni::SetGlue events = des.getEventsGlue(cache);
  const int numevents = events.size();
  jni::EventGlue* eventarray =
    (jni::EventGlue*) new char[numevents * sizeof(jni::EventGlue)];
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  for (int i = 0; i < numevents; i++) {
    jobject javaobject = iter.next();
    jni::EventGlue* event = &eventarray[i];
    new (event) jni::EventGlue(javaobject, cache);
    switch (event->getKindGlue(cache)) {
    case jni::EventKind_UNCONTROLLABLE:
      numuncont++;
      break;
    case jni::EventKind_CONTROLLABLE:
      numcont++;
      break;
    default:
      break;
    }
  }

  GlobalAlphabet alphabet(numuncont, numcont);
  CodeIterator uncontiter = alphabet.uncontrollableIterator();
  CodeIterator contiter = alphabet.controllableIterator();
  for (int i = 0; i < numevents; i++) {
    const jni::EventGlue* event = &eventarray[i];
    uint32 code;
    switch (event->getKindGlue(cache)) {
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
  (JNIEnv* env, jobject jcopier)
{
  try {
    jni::ClassCache cache(env);
    try {
      jni::ProductDESCopierGlue copier(jcopier, &cache);
      jni::ProductDESGlue des = copier.getInputGlue(&cache);
      jstring name = des.getName();
      waters::initGlobalAlphabet(des, &cache);
      const jni::EventGlue prop = copier.getPropositionGlue(&cache);
      jni::ProductDESProxyFactoryGlue factory = copier.getFactoryGlue(&cache);
      jni::CollectionGlue empty = jni::CollectionsGlue::emptySetGlue(&cache);
      jni::ProductDESGlue copy =
	factory.createProductDESProxyGlue(name, &empty, &empty, &cache);
      jni::ProductDESResultGlue result(false, &copy, &cache);
      return result.returnJavaObject();
    } catch (const jni::PreJavaException& pre) {
      cache.throwJavaException(pre);
    }
  } catch (jthrowable exception) {
    return 0;
  }
}
