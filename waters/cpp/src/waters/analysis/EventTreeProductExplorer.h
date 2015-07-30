//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   EventTreeProductExplorer
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _EventTreeProductExplorer_h_
#define _EventTreeProductExplorer_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/EventTree.h"


namespace waters {


//############################################################################
//# class EventTreeProductExplorer
//############################################################################

class EventTreeProductExplorer : public BroadProductExplorer
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTreeProductExplorer
    (const jni::ProductDESProxyFactoryGlue& factory,
     const jni::ProductDESGlue& des,
     const jni::KindTranslatorGlue& translator,
     const jni::EventGlue& premarking,
     const jni::EventGlue& marking,
     jni::ClassCache* cache);
  virtual ~EventTreeProductExplorer() {};

protected:
  //##########################################################################
  //# Overrides for ProductExplorer
  virtual void setup();
  virtual void setupReverseTransitionRelations();
  virtual bool expandForward
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, BroadExpandHandler& handler);
  virtual bool expandForwardSafety
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, TransitionCallBack callBack = 0);
  virtual bool expandReverse
    (uint32_t source, const uint32_t* sourceTuple,
     const uint32_t* sourcePacked, BroadExpandHandler& handler);

private:
  //##########################################################################
  //# Data Members
  EventTree mForwardEventTree;
  EventTree mBackwardEventTree;
};

}   /* namespace waters */

#endif  /* !_EventTreeProductExplorer_h_ */
