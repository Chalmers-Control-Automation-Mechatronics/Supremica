//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   PreEventNotFoundException
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreEventNotFoundException.h"
#include "jni/glue/EventNotFoundExceptionGlue.h"
#include "jni/glue/Glue.h"


namespace jni {


//###########################################################################
//# Class PreEventNotFoundException
//###########################################################################

//###########################################################################
//# PreEventNotFoundException: Constructors, Destructors & Co.

PreEventNotFoundException::
PreEventNotFoundException(const ProductDESGlue& model, jstring name)
  : PreJavaException(CLASS_EventNotFoundException),
    mModel(model),
    mName(name)
{
}

PreEventNotFoundException::
PreEventNotFoundException(const PreEventNotFoundException& partner)
  : PreJavaException(partner),
    mModel(partner.mModel),
    mName(partner.mName)
{
}

PreEventNotFoundException& PreEventNotFoundException::
operator=(const PreEventNotFoundException& partner)
{
  if (this != &partner) {
    PreJavaException::operator=(partner);
    mModel = partner.mModel;
    mName = partner.mName;
  }
  return *this;
}


//###########################################################################
//# PreEventNotFoundException: Throwing Exceptions

jint PreEventNotFoundException::
throwJavaException(ClassCache& cache)
  const
{
  EventNotFoundExceptionGlue glue(&mModel, mName, &cache);
  return cache.throwJavaException(glue);
}


}  /* namespace jni */
