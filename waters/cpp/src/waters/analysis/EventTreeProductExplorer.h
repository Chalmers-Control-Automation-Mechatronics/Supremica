//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
  virtual void removeUncontrollableEvents();
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
