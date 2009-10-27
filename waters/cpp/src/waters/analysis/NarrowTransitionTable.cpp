//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   NarrowTransitionTable
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include "jni/glue/IteratorGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/NarrowEventRecord.h"
#include "waters/analysis/NarrowPreTransitionTable.h"
#include "waters/analysis/NarrowTransitionTable.h"

#ifdef DEBUG
#include "jni/cache/JavaString.h"
#endif /* DEBUG */


namespace waters {

//############################################################################
//# class NarrowTransitionTable
//############################################################################

//############################################################################
//# NarrowTransitionTable: Constructors & Destructors

NarrowTransitionTable::
NarrowTransitionTable(const NarrowPreTransitionTable* pre,
                      jni::ClassCache* cache,
                      const HashTable<const jni::EventGlue*,
                                      NarrowEventRecord*>& eventmap)
  : mNumTransitions(0),
    mStateTable(0),
    mBuffers(0)
{
  mAutomaton = pre->getAutomaton();
  mAutomatonIndex = mAutomaton->getAutomatonIndex();
  mIsPlant = mAutomaton->isPlant();
  mNumStates = mAutomaton->getNumberOfStates();
  mStateTable = new uint32[mNumStates];
  const uint32 transcount = pre->getTransitionCount();
  const uint32 ndcount = pre->getNDCount();
  mBuffers = new uint32[2 * transcount + mNumStates + ndcount];
  uint32 next = 0;
  for (uint32 code = 0; code < mNumStates; code++) {
    NarrowStateRecord* narrowstate = pre->getNarrowStateRecord(code);
    narrowstate->sort();
    // BUG: Must remove skippable stuff HERE!!!
    mStateTable[code] = next;
    uint32 pos = next;
    next += 2 * narrowstate->getNumberOfEnabledEvents() + 1;
    for (NarrowTransitionRecord* narrowtrans = narrowstate->getTransitions();
         narrowtrans != 0;
         narrowtrans = narrowtrans->getNext()) {
      mBuffers[pos++] = narrowtrans->getEventCode();
      if (narrowtrans->isDeterministic()) {
        narrowtrans->setBufferPos(pos++);
      } else {
        mBuffers[pos++] = next;
        next += narrowtrans->getNumberOfSuccessors();
      }
    }
    mBuffers[pos] = UNDEF_UINT32;
  }
  NarrowTransitionRecord* tmptrans = new NarrowTransitionRecord();
  const jni::SetGlue& uniqtrans = pre->getUniqueTransitions();
  const jni::IteratorGlue iter = uniqtrans.iteratorGlue(cache);
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::TransitionGlue trans(javaobject, cache);
    const jni::EventGlue& event = trans.getEventGlue(cache);
    const NarrowEventRecord* eventrecord = eventmap.get(&event);
    std::cerr << (const char*) eventrecord->getName() << std::endl;
    if (eventrecord->isSkippable() || pre->isLocallySelflooped(event)) {
      std::cerr << eventrecord->isSkippable()
                << " " << pre->isLocallySelflooped(event) << std::endl;
      continue;
    }
    const jni::StateGlue& source = trans.getSourceGlue(cache);
    const uint32 sourcecode = pre->getStateCode(source);
    tmptrans->init(sourcecode, eventrecord);
    NarrowTransitionRecord* ntrans = pre->getNarrowTransitionRecord(tmptrans);
    const jni::StateGlue& target = trans.getTargetGlue(cache);
    const uint32 targetcode = pre->getStateCode(target);
    ntrans->putSuccessor(mBuffers, targetcode, TAG_END_OF_LIST);
    mNumTransitions++;
  }
  delete tmptrans;
}


NarrowTransitionTable::
~NarrowTransitionTable()
{
  delete[] mStateTable;
  delete[] mBuffers;
}


//############################################################################
//# NarrowTransitionTable: Setup

void NarrowTransitionTable::
reverse(const NarrowEventRecord* const* events)
{
  NarrowStateRecord* narrowstates =
    (NarrowStateRecord*) new char[mNumStates * sizeof(NarrowStateRecord)];
  for (uint32 code = 0; code < mNumStates; code++) {
    new (&narrowstates[code]) NarrowStateRecord(code);
  }
  const HashAccessor* accessor = NarrowTransitionRecord::getHashAccessor();
  HashTable<NarrowTransitionRecord*,NarrowTransitionRecord*>
    narrowtransmap(accessor, mNumTransitions);
  int transcount = 0;
  int ndcount = 0;
  NarrowTransitionRecord* newtrans = 0;
  for (uint32 source = 0; source < mNumStates; source++) {
    for (uint32 iter = iterator(source); hasNext(iter); iter = next(iter)) {
      const uint32 e = getEvent(iter);
      const NarrowEventRecord* event = events[e];
      if (!event->isOnlySelfloops()) {
        uint32 raw = getRawSuccessors(iter);
        uint32 list = raw;
        if ((raw & TAG_END_OF_LIST) == 0) {
          raw = getRawNondetSuccessor(list);
        }
        bool done = false;
        do {
          uint32 target;
          if (raw & TAG_END_OF_LIST) {
            target = raw & ~TAG_END_OF_LIST;
            done = true;
          } else {
            target = raw;
            raw = getRawNondetSuccessor(++list);
          }
          if (newtrans == 0) {
            newtrans = new NarrowTransitionRecord(target, event);
          } else {
            newtrans->init(target, event);
          }
          NarrowTransitionRecord* oldtrans = narrowtransmap.add(newtrans);
          if (oldtrans == newtrans) {
            narrowstates[target].addTransition(newtrans);
            newtrans = 0;
            transcount++;
          } else if (oldtrans->isDeterministic()) {
            oldtrans->addSuccessor();
            ndcount += 2;
          } else {
            oldtrans->addSuccessor();
            ndcount++;
          }
        } while (!done);
      }
    }
  }
  uint32 *newstatetable = new uint32[mNumStates];
  uint32 *newbuffers = new uint32[2 * transcount + mNumStates + ndcount];
  uint32 nextpos = 0;
  for (uint32 code = 0; code < mNumStates; code++) {
    NarrowStateRecord& narrowstate = narrowstates[code];
    narrowstate.sort();
    newstatetable[code] = nextpos;
    uint32 pos = nextpos;
    nextpos += narrowstate.getNumberOfEnabledEvents() + 1;
    for (NarrowTransitionRecord* narrowtrans = narrowstate.getTransitions();
         narrowtrans != 0;
         narrowtrans = narrowtrans->getNext()) {
      newbuffers[pos++] = narrowtrans->getEventCode();
      if (narrowtrans->isDeterministic()) {
        narrowtrans->setBufferPos(pos++);
      } else {
        newbuffers[pos++] = nextpos;
        nextpos += narrowtrans->getNumberOfSuccessors();
      }
    }
    newbuffers[pos] = UNDEF_UINT32;
  }
  if (newtrans == 0) {
    newtrans = new NarrowTransitionRecord();
  }
  for (uint32 source = 0; source < mNumStates; source++) {
    for (uint32 iter = iterator(source); hasNext(iter); iter = next(iter)) {
      const uint32 e = getEvent(iter);
      const NarrowEventRecord* event = events[e];
      if (!event->isOnlySelfloops()) {
        uint32 raw = getRawSuccessors(iter);
        uint32 list = raw;
        if ((raw & TAG_END_OF_LIST) == 0) {
          raw = getRawNondetSuccessor(list);
        }
        bool done = false;
        do {
          uint32 target;
          if (raw & TAG_END_OF_LIST) {
            target = raw & ~TAG_END_OF_LIST;
            done = true;
          } else {
            target = raw;
            raw = getRawNondetSuccessor(++list);
          }
          if (newtrans == 0) {
            newtrans = new NarrowTransitionRecord(target, event);
          } else {
            newtrans->init(target, event);
          }
          NarrowTransitionRecord* oldtrans = narrowtransmap.get(newtrans);
          oldtrans->putSuccessor(newbuffers, source, TAG_END_OF_LIST);
        } while (!done);
      }
    }
  }
  delete [] mStateTable;
  mStateTable = newstatetable;
  delete [] mBuffers;
  mBuffers = newbuffers;
  mNumTransitions = transcount;
  for (uint32 code = 0; code < mNumStates; code++) {
    narrowstates[code].~NarrowStateRecord();
  }
  delete (const char*) narrowstates;
}


//############################################################################
//# NarrowTransitionTable: Debug Output

#ifdef DEBUG

void NarrowTransitionTable::
dump(const NarrowEventRecord* const* events)
  const
{
  std::cerr << (const char*) (mAutomaton->getName()) << " {" << std::endl
            << "STATE TABLE:" << std::endl;
  for (uint32 code = 0; code < mNumStates; code++) {
    uint32 data = mStateTable[code];
    std::cerr << "  " << code << ":" << (data & ~TAG_END_OF_LIST)
              << (data & TAG_END_OF_LIST ? "+" : "") << std::endl;
  }
  std::cerr << "BUFFERS:" << std::endl;
  for (uint32 code = 0; code < mNumStates; code++) {
    uint32 iter = iterator(code);
    while (hasNext(iter)) {
      uint32 data = getRawSuccessors(iter);
      uint32 ecode = getEvent(iter);
      std::cerr << "  " << iter << ":" << ecode
                << ":" << (data & ~TAG_END_OF_LIST)
                << (data & TAG_END_OF_LIST ? "+" : "")
                << " <" << (const char*) events[ecode]->getName() << ">"
                << std::endl;
      iter = next(iter);
    }
    std::cerr << "  " << iter << ":*" << std::endl;
  }
  std::cerr << "}" << std::endl;
}

#endif /* DEBUG */


}  /* namespace waters */
