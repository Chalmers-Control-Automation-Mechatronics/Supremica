//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#ifdef DEBUG
#include <iostream>
#include "jni/cache/JavaString.h"
#endif /* DEBUG */

#include "waters/analysis/BroadEventRecord.h"
#include "waters/analysis/EventTreeProductExplorer.h"


namespace waters {

//############################################################################
//# class EventTreeProductExplorer
//############################################################################

//############################################################################
//# EventTreeProductExplorer: Constructors & Destructors

EventTreeProductExplorer::
EventTreeProductExplorer(const jni::ProductDESProxyFactoryGlue& factory,
                         const jni::ProductDESGlue& des,
                         const jni::KindTranslatorGlue& translator,
                         const jni::EventGlue& premarking,
                         const jni::EventGlue& marking,
                         jni::ClassCache* cache)
  : BroadProductExplorer(factory, des, translator, premarking, marking, cache)
{
}


//############################################################################
//# EventTreeProductExplorer:
//# Overrides for ProductExplorer and BroadProductExplorer

//----------------------------------------------------------------------------
// setup()

void EventTreeProductExplorer::
setup()
{
  BroadProductExplorer::setup();
  if (!isTrivial()) {
    EventTreeGenerator generator(getAutomatonEncoding(),
                                 getForwardEventRecords(),
                                 mForwardEventTree,
                                 getCheckType() == CHECK_TYPE_SAFETY);
    generator.execute();
    //generator.dump();
  }
}


//----------------------------------------------------------------------------
// setupReverseTransitionRelations()

void EventTreeProductExplorer::
setupReverseTransitionRelations()
{
  if (mBackwardEventTree.isEmpty()) {
    BroadProductExplorer::setupReverseTransitionRelations();
    EventTreeGenerator generator(getAutomatonEncoding(),
                                 getBackwardEventRecords(),
                                 mBackwardEventTree);
    generator.execute();
    //generator.dump();
  }
}


//----------------------------------------------------------------------------
// removeUncontrollableEvents()

void EventTreeProductExplorer::
removeUncontrollableEvents()
{
  mForwardEventTree.clear();
  BroadProductExplorer::removeUncontrollableEvents();
  EventTreeGenerator generator(getAutomatonEncoding(),
                               getForwardEventRecords(),
                               mForwardEventTree,
                               getCheckType() == CHECK_TYPE_SAFETY);
  generator.execute();
  //generator.dump();
}


//----------------------------------------------------------------------------
// expandForward()

#define EXPAND(source, sourceTuple, sourcePacked,                       \
               handler, tree, events, safety)                           \
  {                                                                     \
    const uint32_t lines = tree.getCodeSize();                          \
    uint32_t pos = 0;                                                   \
    while (pos < lines) {                                               \
      const uint32_t code = tree.get(pos++);                            \
      switch (code & EventTree::OPCODE_MASK_2) {                        \
      case EventTree::OPCODE_CASE_2:                                    \
        {                                                               \
          const uint32_t a = code & EventTree::OPERAND_MASK_2;          \
          const uint32_t s = sourceTuple[a];                            \
          pos = tree.get(pos + s);                                      \
          break;                                                        \
        }                                                               \
      case EventTree::OPCODE_IFNN_2:                                    \
        {                                                               \
          const uint32_t t = code & EventTree::OPERAND_MASK_2;          \
          const FastEligibilityTestRecord& record =                     \
            tree.getEligibilityRecord(t);                               \
          const uint32_t a = record.getAutomatonIndex();                \
          const uint32_t s = sourceTuple[a];                            \
          if (record.isEnabled(s)) {                                    \
            pos++;                                                      \
          } else {                                                      \
            pos = tree.get(pos);                                        \
          }                                                             \
          break;                                                        \
        }                                                               \
      case EventTree::OPCODE_EXEC_2:                                    \
        {                                                               \
          const uint32_t e = code & EventTree::OPERAND_MASK_3;          \
          BroadEventRecord* event = events.get(e);                \
          if (safety && ((code & EventTree::OPCODE_MASK_3) ==           \
                         EventTree::OPCODE_FAIL_3)) {                   \
            const AutomatonRecord* dis =                                \
              findDisablingAutomaton(sourceTuple, event);               \
            setTraceEvent(event, dis);                                  \
            return false;                                               \
          } else if (!handler.handleEvent(source, sourceTuple,          \
                                          sourcePacked, event)) {       \
            return false;                                               \
          }                                                             \
          break;                                                        \
        }                                                               \
      case EventTree::OPCODE_GOTO_2:                                    \
        {                                                               \
          pos = code & EventTree::OPERAND_MASK_2;                       \
          break;                                                        \
        }                                                               \
      default:                                                          \
        break;                                                          \
      }                                                                 \
    }                                                                   \
    return true;                                                        \
  }


bool EventTreeProductExplorer::
expandForward(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              BroadExpandHandler& handler)
{
  EXPAND(source, sourceTuple, sourcePacked, handler,
         mForwardEventTree, getForwardEventRecords(), false);
}


//----------------------------------------------------------------------------
// expandForwardSafety()

bool EventTreeProductExplorer::
expandForwardSafety(uint32_t source,
                    const uint32_t* sourceTuple,
                    const uint32_t* sourcePacked,
                    TransitionCallBack callBack)
{
  BroadExpandHandler handler(*this, callBack);
  EXPAND(source, sourceTuple, sourcePacked, handler,
         mForwardEventTree, getForwardEventRecords(), true);
}


//----------------------------------------------------------------------------
// expandReverse()

bool EventTreeProductExplorer::
expandReverse(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              BroadExpandHandler& handler)
{
  EXPAND(source, sourceTuple, sourcePacked, handler,
         mBackwardEventTree, getBackwardEventRecords(), false);
}


}  /* namespace waters */
