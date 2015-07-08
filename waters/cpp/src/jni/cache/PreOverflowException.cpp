//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   PreOverflowException
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include "jni/cache/ClassCache.h"
#include "jni/cache/PreOverflowException.h"
#include "jni/glue/Glue.h"
#include "jni/glue/OverflowExceptionGlue.h"


namespace jni {


//###########################################################################
//# Class PreOverflowException
//###########################################################################

//###########################################################################
//# PreOverflowException: Constructors, Destructors & Co.

PreOverflowException::
PreOverflowException(OverflowKind kind, int limit)
  : PreJavaException(CLASS_OverflowException),
    mKind(kind),
    mLimit(limit)
{
}

PreOverflowException::
PreOverflowException(const PreOverflowException& partner)
  : PreJavaException(partner),
    mKind(partner.mKind),
    mLimit(partner.mLimit)
{
}

PreOverflowException& PreOverflowException::
operator=(const PreOverflowException& partner)
{
  if (this != &partner) {
    PreJavaException::operator=(partner);
    mKind = partner.mKind;
    mLimit = partner.mLimit;
  }
  return *this;
}


//###########################################################################
//# PreOverflowException: Throwing Exceptions

jint PreOverflowException::
throwJavaException(ClassCache& cache)
  const
{
  OverflowExceptionGlue glue(mKind, mLimit, &cache);
  return cache.throwJavaException(glue);
}


}  /* namespace jni */
