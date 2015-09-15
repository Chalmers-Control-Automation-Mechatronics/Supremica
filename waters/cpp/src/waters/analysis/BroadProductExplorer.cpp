//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   BroadProductExplorer
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include <jni.h>
#include <time.h>

#include "jni/cache/ClassCache.h"
#include "jni/cache/JavaString.h"
#include "jni/cache/PreAnalysisConfigurationException.h"
#include "jni/cache/PreEventNotFoundException.h"
#include "jni/cache/PreJavaException.h"
#include "jni/glue/AutomatonGlue.h"
#include "jni/glue/CollectionGlue.h"
#include "jni/glue/EventGlue.h"
#include "jni/glue/EventKindGlue.h"
#include "jni/glue/IteratorGlue.h"
#include "jni/glue/LinkedListGlue.h"
#include "jni/glue/NativeSafetyVerifierGlue.h"
#include "jni/glue/NativeVerificationResultGlue.h"
#include "jni/glue/SetGlue.h"
#include "jni/glue/StateGlue.h"
#include "jni/glue/TransitionGlue.h"
#include "jni/glue/TreeSetGlue.h"

#include "waters/analysis/BroadEventRecord.h"
#include "waters/analysis/BroadProductExplorer.h"
#include "waters/analysis/EventTree.h"
#include "waters/analysis/TarjanStateSpace.h"
#include "waters/analysis/TransitionRecord.h"
#include "waters/analysis/TransitionUpdateRecord.h"
#include "waters/base/HashTable.h"


namespace waters {

//############################################################################
//# class BroadExpandHandler
//############################################################################

//############################################################################
//# BroadExpandHandler: Constructors & Destructors

BroadExpandHandler::
BroadExpandHandler(BroadProductExplorer& explorer,
                  TransitionCallBack callBack) :
  mExplorer(explorer),
  mTransitionCallBack(callBack),
  mNumberOfWords(explorer.getAutomatonEncoding().getEncodingSize()),
  mNonDetBufferPacked(0)
{
}


//############################################################################
//# BroadExpandHandler: Callbacks

bool BroadExpandHandler::
handleEvent(uint32_t source,
            const uint32_t* sourceTuple,
            const uint32_t* sourcePacked,
            BroadEventRecord* event)
{
  uint32_t* bufferPacked = mExplorer.getStateSpace().prepare();
  if (event->isDeterministic()) {
    for (int w = 0; w < mNumberOfWords; w++) {
      TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w);
      if (update == 0) {
        bufferPacked[w] = sourcePacked[w];
      } else {
        uint32_t word = (sourcePacked[w] & update->getKeptMask()) |
                        update->getCommonTargets();
        for (TransitionRecord* trans = update->getTransitionRecords();
             trans != 0;
             trans = trans->getNextInUpdate()) {
          const AutomatonRecord* aut = trans->getAutomaton();
          const int a = aut->getAutomatonIndex();
          const uint32_t source = sourceTuple[a];
          word |= trans->getDeterministicSuccessorShifted(source);
        }
        bufferPacked[w] = word;
      }
    }
    return handleState(source, sourceTuple, sourcePacked, event);
  } else {
    NondeterministicTransitionIterator* iter =
      mExplorer.mNondeterministicTransitionIterators;
    int ndend = 0;
    for (int w = 0; w < mNumberOfWords; w++) {
      TransitionUpdateRecord* update = event->getTransitionUpdateRecord(w);
      if (update == 0) {
        bufferPacked[w] = sourcePacked[w];
      } else {
        uint32_t word = (sourcePacked[w] & update->getKeptMask()) |
                        update->getCommonTargets();
        for (TransitionRecord* trans = update->getTransitionRecords();
             trans != 0;
             trans = trans->getNextInUpdate()) {
          const AutomatonRecord* aut = trans->getAutomaton();
          const int a = aut->getAutomatonIndex();
          const uint32_t source = sourceTuple[a];
          uint32_t succ = trans->getDeterministicSuccessorShifted(source);
          if (succ == TransitionRecord::MULTIPLE_TRANSITIONS) {
            succ = iter[ndend++].setup(trans, source);
          }
          word |= succ;
        }
        bufferPacked[w] = word;
      }
    }
    if (ndend == 0) {
      return handleState(source, sourceTuple, sourcePacked, event);
    } else {
      mNonDetBufferPacked = bufferPacked;
      int ndindex;
      do {
        if (!handleState(source, sourceTuple, sourcePacked, event)) {
          mNonDetBufferPacked = 0;
          return false;
        }
        for (ndindex = 0; ndindex < ndend; ndindex++) {
          if (!iter[ndindex].advance(mNonDetBufferPacked)) {
            break;
          }
        }
      } while (ndindex < ndend);
      mNonDetBufferPacked = 0;
      return true;
    }
  }
}

bool BroadExpandHandler::
handleState(uint32_t source,
            const uint32_t* sourceTuple,
            const uint32_t* sourcePacked,
            BroadEventRecord* event)
{
  mExplorer.incNumberOfTransitionsExplored();
  StateSpace& stateSpace = mExplorer.getStateSpace();
  uint32_t target = stateSpace.add();
  if (source == target) {
    return true;  // skip selfloop
  }
  mExplorer.incNumberOfTransitions();
  event->markTransitionsTakenFast(sourceTuple);
  if (target == mExplorer.getNumberOfStates()) {
    if (mNonDetBufferPacked == 0) {
      mExplorer.incNumberOfStates();
    } else {
      mNonDetBufferPacked = stateSpace.prepare(mExplorer.incNumberOfStates());
    }
  }
  return handleTransition(source, event, target);
}

bool BroadExpandHandler::
handleTransition(uint32_t source,
                 BroadEventRecord* event,
                 uint32_t target)
{
  // std::cerr << source << " -" << (const char*) event->getName() << "-> "
  //           << target << std::endl;
  if (mTransitionCallBack == 0) {
    return true;
  } else {
    return (mExplorer.*mTransitionCallBack)(source, event, target);
  }
}



//############################################################################
//# class BroadProductExplorer
//############################################################################

//############################################################################
//# BroadProductExplorer: Constructors & Destructors

BroadProductExplorer::
BroadProductExplorer(const jni::ProductDESProxyFactoryGlue& factory,
                     const jni::ProductDESGlue& des,
                     const jni::KindTranslatorGlue& translator,
                     const jni::EventGlue& premarking,
                     const jni::EventGlue& marking,
                     jni::ClassCache* cache)
  : ProductExplorer(factory, des, translator, premarking, marking, cache),
    mEventRecords(0),
    mReversedEventRecords(0),
    mMaxNondeterministicUpdates(0),
    mNondeterministicTransitionIterators(0),
    mDumpStates(0)
{
}

BroadProductExplorer::
~BroadProductExplorer()
{
  for (uint32_t i = 0; i < mReversedEventRecords.size(); i++) {
    delete mReversedEventRecords.get(i);
  }
  for (uint32_t i = 0; i < mEventRecords.size(); i++) {
    delete mEventRecords.get(i);
  }
  delete [] mNondeterministicTransitionIterators;
  delete [] mDumpStates;
}


//############################################################################
//# BroadProductExplorer: Overrides for ProductExplorer

void BroadProductExplorer::
addStatistics(const jni::NativeVerificationResultGlue& vresult)
  const
{
  ProductExplorer::addStatistics(vresult);
  vresult.setTotalNumberOfEvents(mEventRecords.size());
}


//############################################################################
//# BroadProductExplorer: Shared Auxiliary Methods

void BroadProductExplorer::
setup()
{
  ProductExplorer::setup();
  if (!isTrivial()) {
    switch (getCheckType()) {
    case CHECK_TYPE_SAFETY:
      setupSafety();
      break;
    case CHECK_TYPE_NONBLOCKING:
      setupNonblocking();
      break;
    default:
      break;
    }
  }
}

void BroadProductExplorer::
teardown()
{
  for (uint32_t i = 0; i < mReversedEventRecords.size(); i++) {
    delete mReversedEventRecords.get(i);
  }
  mReversedEventRecords.clear();
  // Keeping mEventRecords - for stats
  delete [] mNondeterministicTransitionIterators;
  mNondeterministicTransitionIterators = 0;
  delete [] mDumpStates;
  mDumpStates = 0;
  ProductExplorer::teardown();
}


//############################################################################
//# BroadProductExplorer: State Expansion Procedures

//----------------------------------------------------------------------------
// isLocalDumpState()

bool BroadProductExplorer::
isLocalDumpState(const uint32_t* tuple)
  const
{
  if (mDumpStates != 0) {
    uint32_t a = mDumpStates[0];
    int d = 1;
    do {
      if (tuple[a] == mDumpStates[d++]) {
        return true;
      }
      a = mDumpStates[d++];
    } while (a != UINT32_MAX);
  }
  return false;
}


//----------------------------------------------------------------------------
// setupReverseTransitionRelations()

void BroadProductExplorer::
setupReverseTransitionRelations()
{
  if (mReversedEventRecords.isEmpty()) {
    int maxUpdates = 0;
    for (uint32_t e = 0; e < mEventRecords.size(); e++) {
      BroadEventRecord* event = mEventRecords.get(e);
      event->removeTransitionsNotTaken();
      if (!event->isGloballyDisabled() && !event->isOnlySelfloops()) {
        BroadEventRecord* reversed = event->createReversedRecord();
        mReversedEventRecords.add(reversed);
        const int numUpdates = reversed->getNumberOfNondeterministicUpdates();
        if (numUpdates > maxUpdates) {
          maxUpdates = numUpdates;
        }
      }
    }
    mReversedEventRecords.sort(BroadEventRecord::compareForBackwardSearch);
    if (maxUpdates > mMaxNondeterministicUpdates) {
      mMaxNondeterministicUpdates = maxUpdates;
      delete [] mNondeterministicTransitionIterators;
      mNondeterministicTransitionIterators =
        new NondeterministicTransitionIterator[mMaxNondeterministicUpdates];
    }
  }
}


//----------------------------------------------------------------------------
// expandForward()

// The following code inlined instead of method call for +15% speed.

#define EXPAND(source, sourceTuple, sourcePacked, events, handler)      \
  {                                                                     \
    const uint32_t numEvents = events.size();                           \
    for (uint32_t e = 0; e < numEvents; e++) {                          \
      BroadEventRecord* event = events.get(e);                          \
      const AutomatonRecord* dis = 0;                                   \
      FIND_DISABLING_AUTOMATON(sourceTuple, event, dis);                \
      if (dis == 0 &&                                                   \
          !handler.handleEvent(source, sourceTuple, sourcePacked, event)) { \
        return false;                                                   \
      }                                                                 \
    }                                                                   \
    return true;                                                        \
  }

#define FIND_DISABLING_AUTOMATON(sourcetuple, event, dis)               \
  {                                                                     \
    for (TransitionRecord* trans = event->getTransitionRecord();        \
         trans != 0;                                                    \
         trans = trans->getNextInSearch()) {                            \
      const AutomatonRecord* aut = trans->getAutomaton();               \
      const int a = aut->getAutomatonIndex();                           \
      const uint32_t source = sourcetuple[a];                           \
      const uint32_t target = trans->getDeterministicSuccessorShifted(source); \
      if (target == TransitionRecord::NO_TRANSITION) {                  \
        dis = aut;                                                      \
        break;                                                          \
      }                                                                 \
    }                                                                   \
  }

bool BroadProductExplorer::
expandForward(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              BroadExpandHandler& handler)
{
  EXPAND(source, sourceTuple, sourcePacked, mEventRecords, handler);
}

bool BroadProductExplorer::
expandForward(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              TransitionCallBack callBack)
{
  BroadExpandHandler handler(*this, callBack);
  return expandForward(source, sourceTuple, sourcePacked, handler);
}


//----------------------------------------------------------------------------
// expandForwardSafety()

bool BroadProductExplorer::
expandForwardSafety(uint32_t source,
                    const uint32_t* sourceTuple,
                    const uint32_t* sourcePacked,
                    TransitionCallBack callBack)
{
  BroadExpandHandler handler(*this, callBack);
  const uint32_t numEvents = mEventRecords.size();
  for (uint32_t e = 0; e < numEvents; e++) {
    BroadEventRecord* event = mEventRecords.get(e);
    const AutomatonRecord* dis = 0;
    FIND_DISABLING_AUTOMATON(sourceTuple, event, dis);
    if (dis == 0) {
      if (!handler.handleEvent(source, sourceTuple, sourcePacked, event)) {
        return false;
      }
    } else if (!dis->isPlant() && !event->isControllable()) {
      setTraceEvent(event, dis);
      return false;
    }
  }
  return true;
}


//----------------------------------------------------------------------------
// expandForwardAgain()

class BroadVisitHandler : public BroadExpandHandler
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BroadVisitHandler(BroadProductExplorer& explorer,
			     TransitionCallBack callBack) :
    BroadExpandHandler(explorer, callBack)
  {}

  //##########################################################################
  //# Callbacks
  virtual bool handleState(uint32_t source,
			   const uint32_t* sourceTuple,
			   const uint32_t* sourcePacked,
			   BroadEventRecord* event)
  {
    getExplorer().incNumberOfTransitionsExplored();
    StateSpace& stateSpace = getExplorer().getStateSpace();
    uint32_t target = stateSpace.find();
    if (source != target) {
      return handleTransition(source, event, target);
    } else {
      return true;
    }
  }
};

bool BroadProductExplorer::
expandForwardAgain(uint32_t source,
                   const uint32_t* sourceTuple,
                   const uint32_t* sourcePacked,
                   TransitionCallBack callBack)
{
  BroadVisitHandler handler(*this, callBack);
  return expandForward(source, sourceTuple, sourcePacked, handler);
}


//----------------------------------------------------------------------------
// expandReverse()

bool BroadProductExplorer::
expandReverse(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              BroadExpandHandler& handler)
{
  EXPAND(source, sourceTuple, sourcePacked, mReversedEventRecords, handler);
}

bool BroadProductExplorer::
expandReverse(uint32_t source,
              const uint32_t* sourceTuple,
              const uint32_t* sourcePacked,
              TransitionCallBack callBack)
{
  BroadVisitHandler handler(*this, callBack);
  return expandReverse(source, sourceTuple, sourcePacked, handler);
}


//----------------------------------------------------------------------------
// expandTraceState()

class TraceExpandHandler : public BroadExpandHandler
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit TraceExpandHandler(BroadProductExplorer& explorer,
                              uint32_t prevLevelSize,
                              uint32_t prevLevelEnd) :
    BroadExpandHandler(explorer),
    mPrevLevelSize(prevLevelSize),
    mPrevLevelEnd(prevLevelEnd),
    mDeferred(0),
    mEvent(0),
    mSourceState(UINT32_MAX)
  {}

  //##########################################################################
  //# Simple Access
  inline const BroadEventRecord* getEvent() const {return mEvent;}
  inline uint32_t getSourceState() const {return mSourceState;}
  inline const ArrayList<const BroadEventRecord*>& getDeferred() const
    {return mDeferred;}

  //##########################################################################
  //# Callbacks
  virtual bool handleEvent(uint32_t source,
			   const uint32_t* sourceTuple,
			   const uint32_t* sourcePacked,
			   BroadEventRecord* event)
  {
    if (event->getFanout(sourceTuple) <= mPrevLevelSize) {
      return BroadExpandHandler::handleEvent(source, sourceTuple,
                                             sourcePacked, event);
    } else {
      mDeferred.add(event);
      return true;
    }
  }

  virtual bool handleState(uint32_t source,
			   const uint32_t* sourceTuple,
			   const uint32_t* sourcePacked,
			   BroadEventRecord* event)
  {
    getExplorer().incNumberOfTransitionsExplored();
    StateSpace& stateSpace = getExplorer().getStateSpace();
    uint32_t found = stateSpace.find();
    if (found < mPrevLevelEnd) {
      mEvent = event;
      mSourceState = found;
      return false;
    } else {
      return true;
    }
  }

private:
  //##########################################################################
  //# Data Members
  uint32_t mPrevLevelSize;
  uint32_t mPrevLevelEnd;
  ArrayList<const BroadEventRecord*> mDeferred;
  const BroadEventRecord* mEvent;
  uint32_t mSourceState;
};


class BroadEventFinder : public BroadExpandHandler
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit BroadEventFinder(BroadProductExplorer& explorer,
                            const uint32_t* targetPacked) :
    BroadExpandHandler(explorer),
    mTargetPacked(targetPacked),
    mEvent(0),
    mSourceState(UINT32_MAX)
  {}

  //##########################################################################
  //# Simple Access
  inline const BroadEventRecord* getEvent() const {return mEvent;}
  inline uint32_t getSourceState() const {return mSourceState;}

  //##########################################################################
  //# Callbacks
  virtual bool handleState(uint32_t source,
			   const uint32_t* sourceTuple,
			   const uint32_t* sourcePacked,
			   BroadEventRecord* event)
  {
    getExplorer().incNumberOfTransitionsExplored();
    const StateSpace& stateSpace = getExplorer().getStateSpace();
    const uint32_t numStates = stateSpace.size();
    const uint32_t* packed = stateSpace.get(numStates);
    if (stateSpace.equalTuples(packed, mTargetPacked)) {
      mEvent = event;
      mSourceState = source;
      return false;
    } else {
      return true;
    }
  }

private:
  //##########################################################################
  //# Data Members
  const uint32_t* mTargetPacked;
  const BroadEventRecord* mEvent;
  uint32_t mSourceState;
};


void BroadProductExplorer::
expandTraceState(const uint32_t target,
                 const uint32_t* targetTuple,
                 const uint32_t* targetPacked,
                 uint32_t level)
{
  const uint32_t prevLevelStart = getFirstState(level - 1);
  const uint32_t prevLevelEnd = getFirstState(level);
  const int prevLevelSize = prevLevelEnd - prevLevelStart;
  TraceExpandHandler handler(*this, prevLevelSize, prevLevelEnd);
  if (!expandReverse(target, targetTuple, targetPacked, handler)) {
    setTraceState(handler.getSourceState());
    setTraceEvent(handler.getEvent());
  } else {
    // Events with too much reverse fanout are not expanded by the above.
    // Instead we search the previous level in the forward direction,
    // if really necessary ...
    BroadEventFinder finder(*this, targetPacked);
    const ArrayList<const BroadEventRecord*>& deferred = handler.getDeferred();
    uint32_t numDeferred = deferred.size();
    uint32_t* sourceTuple = new uint32_t[getNumberOfAutomata()];
    for (uint32_t source = prevLevelStart; source < prevLevelEnd; source++) {
      uint32_t* sourcePacked = getStateSpace().get(source);
      getAutomatonEncoding().decode(sourcePacked, sourceTuple);
      for (uint32_t e = 0; e < numDeferred; e++) {
        BroadEventRecord* event = deferred.get(e)->getForwardRecord();
        const AutomatonRecord* dis = 0;
        FIND_DISABLING_AUTOMATON(sourceTuple, event, dis);
        if (dis == 0 &&
            !finder.handleEvent(source, sourceTuple, sourcePacked, event)) {
          setTraceState(source);
          setTraceEvent(event);
          goto finish;
        }
      }
    }
  finish:
    delete [] sourceTuple;
  }
}


//----------------------------------------------------------------------------
// findDisablingAutomaton()

const AutomatonRecord* BroadProductExplorer::
findDisablingAutomaton(const uint32_t* sourceTuple,
                       const BroadEventRecord* event)
  const
{
  const AutomatonRecord* dis = 0;
  FIND_DISABLING_AUTOMATON(sourceTuple, event, dis);
  return dis;
}


//----------------------------------------------------------------------------
// findEvent()

const EventRecord* BroadProductExplorer::
findEvent(uint32_t source,
          const uint32_t* sourcePacked,
          const uint32_t* sourceTuple,
          const uint32_t* targetPacked)
{
  BroadEventFinder handler(*this, targetPacked);
  expandForward(source, sourceTuple, sourcePacked, handler);
  return handler.getEvent();
}


//----------------------------------------------------------------------------
// storeNondeterministicTargets()

void BroadProductExplorer::
storeNondeterministicTargets(const uint32_t* sourcetuple,
                             const uint32_t* targettuple,
                             const jni::MapGlue& map)
{
  const BroadEventRecord* event = (const BroadEventRecord*) getTraceEvent();
  event->storeNondeterministicTargets(sourcetuple, targettuple, map);
}


//############################################################################
//# BroadProductExplorer: Private Auxiliary Methods

void BroadProductExplorer::
setupSafety()
{
  // Establish initial event map ...
  jni::ClassCache* cache = getCache();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const int numevents = events.size();
  const EventRecordHashAccessor* eventaccessor =
    BroadEventRecord::getHashAccessor();
  PtrHashTable<const jni::EventGlue*,BroadEventRecord*>
    eventmap(eventaccessor, numevents);
  setupEventMap(eventmap);

  // Collect transitions ...
  const int numaut = getNumberOfAutomata();
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    const jni::AutomatonGlue& autglue = aut->getJavaAutomaton();
    if (aut->getNumberOfInitialStates() == 0 && !isTrivial()) {
      setTrivial();
      if (!aut->isPlant() && isInitialUncontrollable()) {
        setTraceEvent(0, aut);
      }
    }
    setupTransitions(aut, autglue, eventmap);
    const jni::SetGlue& events = autglue.getEventsGlue(cache);
    const jni::IteratorGlue& eventiter = events.iteratorGlue(cache);
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, cache);
      jni::EventKind kind =
        getKindTranslator().getEventKindGlue(&event, cache);
      if (kind == jni::EventKind_UNCONTROLLABLE ||
          kind == jni::EventKind_CONTROLLABLE) {
        BroadEventRecord* eventrecord = eventmap.get(&event);
        eventrecord->normalize(aut);
      }
    }
  }

  // Establish compact event list ...
  setupCompactEventList(eventmap);
}


void BroadProductExplorer::
setupNonblocking()
{
  // Establish initial event map ...
  jni::ClassCache* cache = getCache();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const int numevents = events.size();
  const EventRecordHashAccessor* eventaccessor =
    BroadEventRecord::getHashAccessor();
  PtrHashTable<const jni::EventGlue*,BroadEventRecord*>
    eventmap(eventaccessor, numevents);
  setupEventMap(eventmap);

  // Collect transitions ...
  const int numaut = getNumberOfAutomata();
  uint32_t numdump = 0;
  for (int a = 0; a < numaut; a++) {
    AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
    const jni::AutomatonGlue& autglue = aut->getJavaAutomaton();
    if (aut->getNumberOfInitialStates() == 0) {
      setTrivial();
      return;
    }
    setupTransitions(aut, autglue, eventmap);
    const jni::SetGlue& events = autglue.getEventsGlue(cache);
    const jni::IteratorGlue& eventiter = events.iteratorGlue(cache);
    while (eventiter.hasNext()) {
      jobject javaobject = eventiter.next();
      jni::EventGlue event(javaobject, cache);
      switch (getKindTranslator().getEventKindGlue(&event, cache)) {
      case jni::EventKind_UNCONTROLLABLE:
      case jni::EventKind_CONTROLLABLE:
        {
          BroadEventRecord* eventrecord = eventmap.get(&event);
          eventrecord->normalize(aut);
          break;
        }
      default:
        break;
      }
    }
    numdump += aut->getNumberOfDumpStates();
  }

  // Make dump states list ...
  if (numdump > 0) {
    mDumpStates = new uint32_t[2 * numdump + 1];
    int d = 0;
    for (int a = 0; a < numaut; a++) {
      const AutomatonRecord* aut = getAutomatonEncoding().getRecord(a);
      for (int i = 0; i < aut->getNumberOfDumpStates(); i++) {
        mDumpStates[d++] = a;
        mDumpStates[d++] = aut->getDumpState(i);
      }
    }
    mDumpStates[d] = UINT32_MAX;
  }

  // Establish compact event list ...
  setupCompactEventList(eventmap);
}


void BroadProductExplorer::
setupEventMap(PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  jni::ClassCache* cache = getCache();
  const int numwords = getAutomatonEncoding().getEncodingSize();
  const jni::SetGlue events = getModel().getEventsGlue(cache);
  const jni::IteratorGlue iter = events.iteratorGlue(cache);
  while (iter.hasNext()) {
    jobject javaobject = iter.next();
    jni::EventGlue event(javaobject, cache);
    bool controllable;
    switch (getKindTranslator().getEventKindGlue(&event, cache)) {
    case jni::EventKind_UNCONTROLLABLE:
      controllable = false;
      break;
    case jni::EventKind_CONTROLLABLE:
      controllable = true;
      break;
    default:
      continue;
    }
    BroadEventRecord* record =
      new BroadEventRecord(event, controllable, numwords);
    eventmap.add(record);
  }
}


void BroadProductExplorer::
setupTransitions
  (AutomatonRecord* aut,
   const jni::AutomatonGlue& autglue,
   const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  jni::ClassCache* cache = getCache();
  Int32PtrHashTable<const jni::StateGlue*,uint32_t>* statemap =
    aut->createStateMap();
  const jni::CollectionGlue transitions = autglue.getTransitionsGlue(cache);
  const jni::TreeSetGlue uniqtrans(&transitions, cache);
  bool* dumpstatus = 0;
  if (isDumpStateAware() && !aut->isAllMarked()) {
    uint32_t numstates = aut->getNumberOfStates();
    dumpstatus = new bool[numstates];
    for (uint32_t s = 0; s < numstates; s++) {
      dumpstatus[s] = !aut->isMarkedState(s);
    }
  }
  int maxpass = 1;
  for (int pass = 1; pass <= maxpass; pass++) {
    const jni::IteratorGlue transiter = uniqtrans.iteratorGlue(cache);
    while (transiter.hasNext()) {
      jobject javaobject = transiter.next();
      jni::TransitionGlue trans(javaobject, cache);
      const jni::EventGlue& eventglue = trans.getEventGlue(cache);
      BroadEventRecord* eventrecord = eventmap.get(&eventglue);
      if (eventrecord == 0) {
        throw jni::PreEventNotFoundException(getModel(), eventglue.getName());
      }
      const jni::StateGlue& sourceglue = trans.getSourceGlue(cache);
      const uint32_t sourcecode = statemap->get(&sourceglue);
      const jni::StateGlue& targetglue = trans.getTargetGlue(cache);
      const uint32_t targetcode = statemap->get(&targetglue);
      if (pass == 1) {
        const bool det =
          eventrecord->addDeterministicTransition(aut, sourcecode, targetcode);
        if (!det) {
          maxpass = 2;
        }
        if (dumpstatus != 0) {
          dumpstatus[sourcecode] = false;
        }
      } else {
        eventrecord->addNondeterministicTransition
          (aut, sourcecode, targetcode);
      }
    }
  }
  if (dumpstatus != 0) {
    aut->setupDumpStates(dumpstatus);
    delete [] dumpstatus;
  }
  aut->deleteStateMap(statemap);
}


void BroadProductExplorer::
setupCompactEventList
  (const PtrHashTable<const jni::EventGlue*,BroadEventRecord*>& eventmap)
{
  mMaxNondeterministicUpdates = 0;
  uint32_t numEvents = eventmap.size();
  CheckType checkType = getCheckType();
  HashTableIterator hiter1 = eventmap.iterator();
  while (eventmap.hasNext(hiter1)) {
    const BroadEventRecord* event = eventmap.next(hiter1);
    if (event->isSkippable(checkType)) {
      numEvents--;
    }
    const int numupdates = event->getNumberOfNondeterministicUpdates();
    if (numupdates > mMaxNondeterministicUpdates) {
      mMaxNondeterministicUpdates = numupdates;
    }
  }
  mEventRecords.clear(numEvents);
  HashTableIterator hiter2 = eventmap.iterator();
  bool trivial = checkType == CHECK_TYPE_SAFETY;
  bool removing = checkType == CHECK_TYPE_NONBLOCKING &&
                  getConflictCheckMode() ==
                    jni::ConflictCheckMode_COMPUTED_BACKWARDS_TRANSITIONS;
  while (eventmap.hasNext(hiter2)) {
    BroadEventRecord* event = eventmap.next(hiter2);
    if (event->isSkippable(checkType)) {
      delete event;
    } else {
      event->optimizeTransitionRecordsForSearch(checkType);
      if (removing) {
        event->setupNotTakenSearchRecords();
      }
      mEventRecords.add(event);
      trivial &= (event->isControllable() | !event->isDisabledInSpec());
    }
  }
  if (trivial) {
    setTrivial();
    return;
  }
  mEventRecords.sort(BroadEventRecord::compareForForwardSearch);
  if (mMaxNondeterministicUpdates > 0) {
    mNondeterministicTransitionIterators =
      new NondeterministicTransitionIterator[mMaxNondeterministicUpdates];
  }
}


}  /* namespace waters */
