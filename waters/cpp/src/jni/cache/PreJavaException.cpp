//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: jni.cache
//# CLASS:   PreJavaException
//###########################################################################
//# $Id: PreJavaException.cpp,v 1.3 2006-11-15 01:26:40 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <string.h>

#include "jni/cache/PreJavaException.h"


namespace jni {


//###########################################################################
//# Class PreJavaException
//###########################################################################

//###########################################################################
//# PreJavaException: Constructors, Destructors & Co.

PreJavaException::
PreJavaException(waters::uint32 classcode)
  : mClassCode(classcode)
{
  initMessage(0,true);
}

PreJavaException::
PreJavaException(waters::uint32 classcode,
                 const char* msg,
                 bool staticString)
  : mClassCode(classcode)
{
  initMessage(msg, staticString);
}

PreJavaException::
PreJavaException(const PreJavaException& partner)
  : mClassCode(partner.mClassCode)
{
  initMessage(partner.mMessage, partner.mStaticString);
}

PreJavaException::
~PreJavaException()
{
  if (!mStaticString) {
    delete [] mMessage;
  }
}

PreJavaException& PreJavaException::
operator=(const PreJavaException& partner)
{
  if (this != &partner) {
    if (!mStaticString) {
      delete [] mMessage;
    }
    mClassCode = partner.mClassCode;
    initMessage(partner.mMessage, partner.mStaticString);
  }
  return *this;
}


//###########################################################################
//# PreJavaException: Auxiliary Methods

void PreJavaException::
initMessage(const char* msg, bool staticString)
{
  if ( (mStaticString = staticString) ) {
    mMessage = (char*) msg;
  } else {
    int len = strlen(msg);
    mMessage = new char[len + 1];
    strcpy(mMessage, msg);
  }
}


}  /* namespace jni */
