//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   ProductDES
//###########################################################################
//# $Id: ProductDES.h,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################


#ifndef _ProductDES_h_
#define _ProductDES_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <jni.h>

#include "waters/des/GlobalAlphabet.h"


namespace waters {

//############################################################################
//# class ProductDES
//############################################################################

class ProductDES
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit ProductDES(uint32 numprops, uint32 numuncont, uint32 numcont);
  ~ProductDES();

  //##########################################################################
  //# Initialisation
  void initEventName(uint32 code, jstring name);

  //##########################################################################
  //# Simple Access

private:
  //##########################################################################
  //# Data Members
  jstring mName;
  GlobalAlphabet mAlphabet;

};

}   /* namespace waters */

#endif  /* !_ProductDES_h_ */
