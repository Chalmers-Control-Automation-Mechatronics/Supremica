//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

#include <string.h>

#ifdef DEBUG
#include <math.h>
#include <iomanip>
#include <iostream>
#endif /* DEBUG */

#include "jni/cache/JavaString.h"

#include "waters/analysis/BroadEventRecord.h"
#include "waters/analysis/EventTree.h"


namespace waters {


//############################################################################
//# EventTree: Simple Access

void EventTree::
clear()
{
  mCode.clear();
  mEligibilityRecords.clear();
}


//############################################################################
//# EventTree: Code Generation

void EventTree::
appendCase(int aut)
{
  appendRaw(OPCODE_CASE_2 | aut);
}

void EventTree::
appendExecute(uint32_t event)
{
  appendRaw(OPCODE_EXEC_3 | event);
}

void EventTree::
appendIfDisabled(uint32_t trans, uint32_t task)
{
  appendRaw(OPCODE_IFNN_2 | trans);
  appendRaw(task);
}

void EventTree::
appendFail(uint32_t event)
{
  appendRaw(OPCODE_FAIL_3 | event);
}

void EventTree::
appendGoto(uint32_t task)
{
  appendRaw(OPCODE_GOTO_2 | task);
}

void EventTree::
appendRaw(uint32_t value)
{
  mCode.add(value);
}



//############################################################################
//# class EventTreeTaskHashAccessor
//############################################################################

//############################################################################
//# EventTreeTaskHashAccessor: Hash Methods

uint64_t EventTreeTaskHashAccessor::
hash(intptr_t key)
  const
{
  const EventTreeTask* task = (const EventTreeTask*) key;
  return task->hash();
}


bool EventTreeTaskHashAccessor::
equals(intptr_t key1, intptr_t key2)
  const
{
  const EventTreeTask* task1 = (const EventTreeTask*) key1;
  const EventTreeTask* task2 = (const EventTreeTask*) key2;
  return task1->equals(*task2);
}


//############################################################################
//# class EventTreeTask
//############################################################################

//############################################################################
//# EventTreeTask: Class Variables

const EventTreeTaskHashAccessor EventTreeTask::theHashAccessor;


//############################################################################
//# EventTreeTask: Constructors & Destructors

EventTreeTask::
EventTreeTask()
  : mTaskID(UINT32_MAX)
{
}



//############################################################################
//# class EventTreeTaskSplit
//############################################################################

//############################################################################
//# EventTreeTaskSplit: Class Variables

const uint64_t EventTreeTaskSplit::theHashCode = hashInt(TASK_TYPE_SPLIT);


//############################################################################
//# EventTreeTaskSplit: Constructors & Destructors

EventTreeTaskSplit::
EventTreeTaskSplit(EventTreeTask* next, EventTreeGenerator& generator)
  : mEventList(generator.getNumberOfEvents()),
    mAutomataList(generator.getNumberOfAutomata()),
    mAutomataSet(generator.getNumberOfAutomata(), true),
    mFanout(1),
    mSplittable(true),
    mGenerator(generator),
    mNextTask(next)
{
  for (uint32_t e = 0; e < generator.getNumberOfEvents(); e++) {
    mEventList.add(e);
  }
  for (uint32_t a = 0; a < generator.getNumberOfAutomata(); a++) {
    mAutomataList.add(a);
  }
}

EventTreeTaskSplit::
EventTreeTaskSplit(const ArrayList<uint32_t>& events,
                   const ArrayList<uint32_t>& automata,
                   uint32_t fanout,
                   bool splittable,
                   EventTreeTask* next,
                   EventTreeGenerator& generator)
  : mEventList(events.size()),
    mAutomataList(automata.size()),
    mAutomataSet(generator.getNumberOfAutomata()),
    mFanout(fanout),
    mSplittable(splittable),
    mGenerator(generator),
    mNextTask(next)
{
  BitSet disablingAutomata(generator.getNumberOfAutomata());
  for (uint32_t i = 0; i < events.size(); i++) {
    uint32_t e = events.get(i);
    mEventList.add(e);
    const BroadEventRecord* event = generator.getEvent(e);
    for (TransitionRecord* trans = event->getTransitionRecord();
         trans != 0;
         trans = trans->getNextInSearch()) {
      const AutomatonRecord* aut = trans->getAutomaton();
      const int a = aut->getAutomatonIndex();
      disablingAutomata.setBit(a);
    }
  }
  for (uint32_t i = 0; i < automata.size(); i++) {
    uint32_t a = automata.get(i);
    if (disablingAutomata.get(a)) {
      mAutomataList.add(a);
      mAutomataSet.setBit(a);
    }
  }
}


//############################################################################
//# EventTreeTaskSplit: Comparing & Hashing

bool EventTreeTaskSplit::
equals(const EventTreeTask& task)
  const
{
  if (task.getTaskType() == TASK_TYPE_SPLIT) {
    const EventTreeTaskSplit& splitTask = (const EventTreeTaskSplit&) task;
    return
      mEventList.equals(splitTask.mEventList) &&
      mAutomataSet.equals(splitTask.mAutomataSet) &&
      mNextTask == splitTask.mNextTask;
  } else {
    return false;
  }
}

uint64_t EventTreeTaskSplit::
hash()
  const
{
  return
    theHashCode +
    5 * mEventList.hash() +
    25 * mAutomataSet.hash() +
    125 * hashInt(mNextTask->getTaskID());
}


//############################################################################
//# EventTreeTaskSplit: Code Generation

void EventTreeTaskSplit::
execute()
{
  // 1. Don't try to split if we know it is not possible
  if (!mSplittable || mEventList.size() <= 1) {
    executeWithoutSplit();
    return;
  }

  // 2. Execute any always enabled events
  for (uint32_t i = 0; i < mEventList.size(); i++) {
    uint32_t e = mEventList.get(i);
    if (mGenerator.isAlwaysEnabled(e, mAutomataSet)) {
      mGenerator.appendExecute(e);
      EventTreeTask* task = addTaskWithoutEvent(e);
      mGenerator.appendGotoUnlessNext(task);
      return;
    }
  }

  // 3. Try to find an automaton to split upon
  mGenerator.resetTopCounts();
  for (uint32_t i = 0; i < mEventList.size(); i++) {
    uint32_t e = mEventList.get(i);
    mGenerator.registerTopCounts(e, mAutomataSet, mFanout);
  }
  uint32_t top = mGenerator.findTopAutomaton(mAutomataList);
  if (top == UINT32_MAX) {
    executeWithoutSplit();
    return;
  }
  uint32_t numEvents = mEventList.size();
  ArrayList<uint32_t> topEvents(numEvents);
  ArrayList<uint32_t> otherEvents(numEvents - 1);
  for (uint32_t i = 0; i < mEventList.size(); i++) {
    uint32_t e = mEventList.get(i);
    if (mGenerator.isTopAutomaton(top, e, mAutomataSet, mFanout)) {
      topEvents.add(e);
    } else {
      otherEvents.add(e);
    }
  }
  if (topEvents.size() <= 1) {
    executeWithoutSplit();
    return;
  }

  // 4. Split on top automaton using topEvents, then continue using otherEvents
  EventTreeTask* taskAfter = addTaskWithEvents(otherEvents);
  ArrayList<uint32_t> remainingAutomata(mAutomataList.size() - 1);
  for (uint32_t i = 0; i < mAutomataList.size(); i++) {
    uint32_t a = mAutomataList.get(i);
    if (a != top) {
      remainingAutomata.add(a);
    }
  }
  ArrayList<uint32_t> remainingEvents(topEvents.size());
  const AutomatonRecord* topAut = mGenerator.getAutomaton(top);
  const bool spec = mGenerator.isSafety() && !topAut->isPlant();
  const uint32_t numStates = topAut->getNumberOfStates();
  const uint32_t remainingFanout = mFanout * numStates;
  uint32_t* branches = new uint32_t[numStates];
  uint32_t s = numStates;
  do {
    s--;
    remainingEvents.clear();
    EventTreeTask* task = 0;
    for (uint32_t i = 0; i < topEvents.size(); i++) {
      uint32_t e = topEvents.get(i);
      if (mGenerator.isEnabled(e, top, s)) {
        remainingEvents.add(e);
      } else if (spec) {
        const BroadEventRecord* event = mGenerator.getEvent(e);
        if (!event->isControllable()) {
          task = mGenerator.addFailTask(e);
          break;
        }
      }
    }
    if (task == 0) {
      task = mGenerator.addSplitTask(remainingEvents, remainingAutomata,
                                     remainingFanout, true, taskAfter);
    }
    branches[s] = task->getTaskID();
  } while (s > 0);
  mGenerator.appendCase(top);
  for (uint32_t s = 0; s < numStates; s++) {
    mGenerator.appendRaw(branches[s]);
  }
  delete [] branches;
}

void EventTreeTaskSplit::
executeWithoutSplit()
{
  mSplittable = false;
  const uint32_t e = mEventList.get(0);
  const EventTreeTask* taskAfter = addTaskWithoutEvent(e);
  const BroadEventRecord* event = mGenerator.getEvent(e);
  const bool controllable = !mGenerator.isSafety() || event->isControllable();
  for (const TransitionRecord* trans = event->getTransitionRecord();
       trans != 0;
       trans = trans->getNextInSearch()) {
    const AutomatonRecord* aut = trans->getAutomaton();
    const int a = aut->getAutomatonIndex();
    if (mAutomataSet.get(a)) {
      const EventTreeTask* task =
        controllable || aut->isPlant() ? taskAfter : mGenerator.addFailTask(e);
      uint32_t taskID = task->getTaskID();
      mGenerator.appendIfDisabled(trans, e, taskID);
    }
  }
  mGenerator.appendExecute(e);
  mGenerator.appendGotoUnlessNext(taskAfter);
}


EventTreeTask* EventTreeTaskSplit::
addTaskWithoutEvent(uint32_t event)
  const
{
  if (mEventList.size() <= 1) {
    return mNextTask;
  } else {
    ArrayList<uint32_t> remainingEvents(mEventList.size() - 1);
    for (uint32_t i = 0; i < mEventList.size(); i++) {
      uint32_t e = mEventList.get(i);
      if (e != event) {
        remainingEvents.add(e);
      }
    }
    return addTaskWithEvents(remainingEvents);
  }
}

EventTreeTask* EventTreeTaskSplit::
addTaskWithEvents(const ArrayList<uint32_t>& events)
  const
{
  return mGenerator.addSplitTask(events, mAutomataList,
                                 mFanout, mSplittable, mNextTask);
}


uint32_t EventTreeTaskSplit::
findFailedEvent()
  const
{
  if (mSplittable && mGenerator.isSafety()) {
    for (uint32_t i = 0; i < mEventList.size(); i++) {
      const uint32_t e = mEventList.get(i);
      const BroadEventRecord* event = mGenerator.getEvent(e);
      if (!event->isControllable()) {
        for (const TransitionRecord* trans = event->getTransitionRecord();
             trans != 0;
             trans = trans->getNextInSearch()) {
          const AutomatonRecord* aut = trans->getAutomaton();
          if (aut->isPlant()) {
            const int a = aut->getAutomatonIndex();
            if (mAutomataSet.get(a)) {
              break;
            }
          } else if (trans->getProbability() == 0.0f) {
            return e;
          } else {
            break;
          }
        }
      }
    }
  }
  return UINT32_MAX;
}



//############################################################################
//# class EventTreeTaskFail
//############################################################################

//############################################################################
//# EventTreeTaskFail: Class Variables

const uint64_t EventTreeTaskFail::theHashCode = hashInt(TASK_TYPE_FAIL);


//############################################################################
//# EventTreeTaskFail: Constructors & Destructors

EventTreeTaskFail::
EventTreeTaskFail(uint32_t event, EventTreeGenerator& generator)
  : mEvent(event),
    mGenerator(generator)
{
}


//############################################################################
//# EventTreeTaskFail: Comparing & Hashing

bool EventTreeTaskFail::
equals(const EventTreeTask& task)
  const
{
  if (task.getTaskType() == TASK_TYPE_FAIL) {
    const EventTreeTaskFail& failTask = (const EventTreeTaskFail&) task;
    return mEvent == failTask.mEvent;
  } else {
    return false;
  }
}

uint64_t EventTreeTaskFail::
hash()
  const
{
  return theHashCode + hashInt(mEvent);
}


//############################################################################
//# EventTreeTaskFail: Code Generation

void EventTreeTaskFail::
execute()
{
  mGenerator.appendFail(mEvent);
}



//############################################################################
//# class EventTreeTaskEnd
//############################################################################

//############################################################################
//# EventTreeTaskEnd: Class Variables

const uint64_t EventTreeTaskEnd::theHashCode = hashInt(TASK_TYPE_END);



//###########################################################################
//# Class EventTreeGenerator
//###########################################################################

//############################################################################
//# EventTreeGenerator: Constructors & Destructors

EventTreeGenerator::
EventTreeGenerator(const AutomatonEncoding& encoding,
                   const ArrayList<BroadEventRecord*>& events,
                   EventTree& output,
                   bool safety)
  : mAutomatonEncoding(encoding),
    mEvents(events),
    mOutput(output),
    mSafety(safety),
    mHasFailedEvents(safety),
    mMaxFanout(events.size()),
    mEndTask(0),
    mAllTasks(EventTreeTask::getHashAccessor()),
    mEligibilityRecordTable(this),
    mTopCounts(0)
{
  for (int a = 0; a < encoding.getNumberOfAutomata(); a++) {
    const AutomatonRecord* aut = encoding.getRecord(a);
    uint32_t numStates = aut->getNumberOfStates();
    if (numStates > mMaxFanout) {
      mMaxFanout = numStates;
    }
  }
}

EventTreeGenerator::
~EventTreeGenerator()
{
  delete [] mTopCounts;
  HashTableIterator iter = mAllTasks.iterator();
  while (mAllTasks.hasNext(iter)) {
    EventTreeTask* task = mAllTasks.next(iter);
    delete task;
  }
}


//############################################################################
//# EventTreeGenerator: Invocation

void EventTreeGenerator::
execute()
{
  if (getNumberOfEvents() > 0) {
    EventTreeTaskEnd* endTask = new EventTreeTaskEnd();
    addTask(endTask);
    EventTreeTaskSplit* mainTask = new EventTreeTaskSplit(endTask, *this);
    addTask(mainTask);
    while (!mSplitTasks.isEmpty()) {
      EventTreeTaskSplit* task = mSplitTasks.removeLast();
      executeTask(task);
    }
    for (uint32_t i = 0; i < mFailTasks.size(); i++) {
      EventTreeTaskFail* task = mFailTasks.get(i);
      executeTask(task);
    }
    executeTask(mEndTask);
    renumber();
  }
}

void EventTreeGenerator::
executeTask(EventTreeTask* task)
{
  uint32_t id = task->getTaskID();
  uint32_t lineNumber = mOutput.getCodeSize();
  mLineNumbers.set(id, lineNumber);
  task->execute();
}


//############################################################################
//# EventTreeGenerator: Task Management

EventTreeTask* EventTreeGenerator::
peekNextTask()
  const
{
  if (!mSplitTasks.isEmpty()) {
    uint32_t end = mSplitTasks.size() - 1;
    return mSplitTasks.get(end);
  } else if (!mFailTasks.isEmpty()) {
    return mFailTasks.get(0);
  } else {
    return mEndTask;
  }
}

EventTreeTask* EventTreeGenerator::
addSplitTask(const ArrayList<uint32_t>& events,
             const ArrayList<uint32_t>& automata,
             uint32_t fanout,
             bool splittable,
             EventTreeTask* next)
{
  if (events.isEmpty()) {
    return next;
  }
  EventTreeTaskSplit* task = 
    new EventTreeTaskSplit(events, automata, fanout, splittable, next, *this);
  if (mHasFailedEvents) {
    uint32_t failed = task->findFailedEvent();
    if (failed != UINT32_MAX) {
      delete task;
      return addFailTask(failed);
    }
  }
  return addTask(task);
}

EventTreeTask* EventTreeGenerator::
addFailTask(uint32_t event)
{
  EventTreeTaskFail* task = new EventTreeTaskFail(event, *this);
  return addTask(task);
}

EventTreeTask* EventTreeGenerator::
addTask(EventTreeTask* task)
{
  EventTreeTask* added = mAllTasks.add(task);
  if (added != task) {
    delete task;
  } else {
    uint32_t id = mLineNumbers.size();
    task->setTaskID(id);
    mLineNumbers.add(UINT32_MAX);
    switch (task->getTaskType()) {
    case TASK_TYPE_END:
      mEndTask = (EventTreeTaskEnd*) task;
      break;
    case TASK_TYPE_FAIL:
      mFailTasks.add((EventTreeTaskFail*) task);
      break;
    case TASK_TYPE_SPLIT:
      mSplitTasks.add((EventTreeTaskSplit*) task);
      break;
    default:
      break;
    }
  }
  return added;
}


//############################################################################
//# EventTreeGenerator: Event Access

bool EventTreeGenerator::
isAlwaysEnabled(uint32_t e, const BitSet& automata)
  const
{
  const BroadEventRecord* event = getEvent(e);
  for (TransitionRecord* trans = event->getTransitionRecord();
       trans != 0;
       trans = trans->getNextInSearch()) {
    const AutomatonRecord* aut = trans->getAutomaton();
    const int a = aut->getAutomatonIndex();
    if (automata.get(a)) {
      return false;
    }
  }
  return true;
}

bool EventTreeGenerator::
isEnabled(uint32_t e, int a, uint32_t s)
  const
{
  const BroadEventRecord* event = getEvent(e);
  for (TransitionRecord* trans = event->getTransitionRecord();
       trans != 0;
       trans = trans->getNextInSearch()) {
    const AutomatonRecord* aut = trans->getAutomaton();
    if (aut->getAutomatonIndex() == a) {
      return trans->isEnabled(s);
    }
  }
  return true;
}


//############################################################################
//# EventTreeGenerator: Automata Access

void EventTreeGenerator::
resetTopCounts()
{
  uint32_t numAutomata = getNumberOfAutomata();
  if (mTopCounts == 0) {
    mTopCounts = new uint32_t[numAutomata];
  }
  memset(mTopCounts, 0, numAutomata * sizeof(uint32_t));
}


#define PROCESS_TOP_COUNTS(e, automata, fanout)                         \
  {                                                                     \
    const BroadEventRecord* event = getEvent(e);                        \
    const bool controllable = !mSafety || event->isControllable();      \
    const TransitionRecord* trans = event->getTransitionRecord();       \
    const AutomatonRecord* aut = 0;                                     \
    bool hasPlant = false;                                              \
    for (; trans != 0; trans = trans->getNextInSearch()) {              \
      aut = trans->getAutomaton();                                      \
      const int a = aut->getAutomatonIndex();                           \
      if (automata.get(a)) {                                            \
        if (!controllable) {                                            \
          if (aut->isPlant()) {                                         \
            hasPlant = true;                                            \
          } else if (hasPlant) {                                        \
            trans = 0;                                                  \
            break;                                                      \
          }                                                             \
        }                                                               \
        if ((uint64_t) fanout * aut->getNumberOfStates() <= mMaxFanout) { \
          break;                                                        \
        }                                                               \
      }                                                                 \
    }                                                                   \
    if (trans != 0) {                                                   \
      const TransitionRecord* top = trans;                              \
      const float topProbability = top->getProbability();               \
      const bool topPlant = aut->isPlant() || controllable;             \
      for (; trans != 0; trans = trans->getNextInSearch()) {            \
        aut = trans->getAutomaton();                                    \
        const int a = aut->getAutomatonIndex();                         \
        const bool plant = aut->isPlant() || controllable;              \
        const float probability = trans->getProbability();              \
        if (automata.get(a) &&                                          \
            probability == topProbability && plant == topPlant &&       \
            (uint64_t) fanout * aut->getNumberOfStates() <= mMaxFanout) { \
          VISIT_TOP_COUNT(a);                                           \
        } else if ((topPlant && !plant) || (probability < topProbability)) { \
          break;                                                        \
        }                                                               \
      }                                                                 \
    }                                                                   \
  }

void EventTreeGenerator::
registerTopCounts(uint32_t e, const BitSet& automata, uint32_t fanout)
{
# define VISIT_TOP_COUNT(a) mTopCounts[a]++
  PROCESS_TOP_COUNTS(e, automata, fanout);
# undef VISIT_TOP_COUNT
}


uint32_t EventTreeGenerator::
findTopAutomaton(const IntArrayList<uint32_t>& automata)
  const
{
  uint32_t top = UINT32_MAX;
  uint32_t topCount = 0;
  for (uint32_t i = 0; i < automata.size(); i++) {
    uint32_t a = automata.get(i);
    if (mTopCounts[a] > topCount) {
      top = a;
      topCount = mTopCounts[a];
    }
  }
  return top;
}


bool EventTreeGenerator::
isTopAutomaton(int aTop, uint32_t e, const BitSet& automata, uint32_t fanout)
  const
{
# define VISIT_TOP_COUNT(a) {if (a == aTop) return true;}
  PROCESS_TOP_COUNTS(e, automata, fanout);
# undef VISIT_TOP_COUNT
  return false;
}

#undef PROCESS_TOP_COUNTS


//############################################################################
//# EventTreeGenerator: Code Generation

void EventTreeGenerator::
appendCase(int aut)
{
  mOutput.appendCase(aut);
}

void EventTreeGenerator::
appendIfDisabled(const TransitionRecord* trans, uint32_t e, uint32_t task)
{
  uint32_t t = mOutput.prepareEligibilityRecord();
  FastEligibilityTestRecord& record = mOutput.getEligibilityRecord(t);
  record.setup(trans, e);
  t = mEligibilityRecordTable.add(t);
  mOutput.addEligibilityRecord(t);
  mOutput.appendIfDisabled(t, task);
}

void EventTreeGenerator::
appendExecute(uint32_t event)
{
  mOutput.appendExecute(event);
}

void EventTreeGenerator::
appendFail(uint32_t event)
{
  mOutput.appendFail(event);
}

void EventTreeGenerator::
appendGotoUnlessNext(const EventTreeTask* task)
{
  if (task != peekNextTask()) {
    appendGoto(task->getTaskID());
  }
}

void EventTreeGenerator::
appendGoto(uint32_t task)
{
  mOutput.appendGoto(task);
}

void EventTreeGenerator::
appendRaw(uint32_t value)
{
  mOutput.appendRaw(value);
}


void EventTreeGenerator::
renumber()
{
  const uint32_t lines = mOutput.getCodeSize();
  uint32_t pos = 0;
  while (pos < lines) {
    const int line = pos;
    const int code = mOutput.get(pos++);
    switch (code & EventTree::OPCODE_MASK_2) {
    case EventTree::OPCODE_CASE_2:
      {
        const uint32_t a = code & EventTree::OPERAND_MASK_2;
        const AutomatonRecord* aut = getAutomaton(a);
        for (uint32_t s = 0; s < aut->getNumberOfStates(); s++) {
          const uint32_t l0 = mOutput.get(pos);
          const uint32_t l1 = mLineNumbers.get(l0);
          mOutput.set(pos++, l1);
        }
        break;
      }
    case EventTree::OPCODE_IFNN_2:
      {
        const uint32_t l0 = mOutput.get(pos);
        const uint32_t l1 = mLineNumbers.get(l0);
        mOutput.set(pos++, l1);
        break;
      }
    case EventTree::OPCODE_EXEC_2:
      break;  // nothing to be replaced here
    case EventTree::OPCODE_GOTO_2:
      {
        const uint32_t l0 = code & EventTree::OPERAND_MASK_2;
        const uint32_t l1 = mLineNumbers.get(l0);
        mOutput.set(line, EventTree::OPCODE_GOTO_2 | l1);
        break;
      }
    default:
      break;
    }
  }
}



//############################################################################
//# EventTreeGenerator: Hash Methods

uint64_t EventTreeGenerator::
hash(int32_t key)
  const
{
  const FastEligibilityTestRecord& record = mOutput.getEligibilityRecord(key);
  return record.hash();
}

bool EventTreeGenerator::
equals(int32_t key1, int32_t key2)
  const
{
  const FastEligibilityTestRecord& record1 = mOutput.getEligibilityRecord(key1);
  const FastEligibilityTestRecord& record2 = mOutput.getEligibilityRecord(key2);
  return record1.equals(record2);
}



//############################################################################
//# EventTreeGenerator: Debugging

#ifdef DEBUG

void EventTreeGenerator::
dump()
  const
{
  const uint32_t lines = mOutput.getCodeSize();
  const int digits = (int) log10(lines) + 1;
  uint32_t pos = 0;
  while (pos < lines) {
    std::cerr << std::setw(digits) << pos << ": ";
    const uint32_t code = mOutput.get(pos++);
    switch (code & EventTree::OPCODE_MASK_2) {
    case EventTree::OPCODE_CASE_2:
      {
        const uint32_t a = code & EventTree::OPERAND_MASK_2;
        const AutomatonRecord* aut = getAutomaton(a);
        const jni::JavaString name = aut->getName();
        std::cerr << "CASE " << (const char*) name << ' ';
        bool first = true;
        for (uint32_t s = 0; s < aut->getNumberOfStates(); s++) {
          if (first) {
            first = false;
          } else {
            std::cerr << ',';
          }
          const uint32_t l = mOutput.get(pos++);
          std::cerr << l;
        }
        break;
      }
    case EventTree::OPCODE_IFNN_2:
      {
        const uint32_t t = code & EventTree::OPERAND_MASK_2;
        const FastEligibilityTestRecord& record =
          mOutput.getEligibilityRecord(t);
        const uint32_t a = record.getAutomatonIndex();
        const AutomatonRecord* aut = getAutomaton(a);
        const jni::JavaString autName = aut->getName();
        const uint32_t e = record.getEventIndex();
        const BroadEventRecord* event = getEvent(e);
        const jni::JavaString eventName = event->getName();
        const uint32_t l = mOutput.get(pos++);
        std::cerr << "IFNN " << (const char*) autName << ' '
                  << (const char*) eventName << " GOTO " << l;
        break;
      }
    case EventTree::OPCODE_EXEC_2:
      {
        if ((code & EventTree::OPCODE_MASK_3) == EventTree::OPCODE_EXEC_3) {
          std::cerr << "EXEC ";
        } else {
          std::cerr << "FAIL ";
        }
        const uint32_t e = code & EventTree::OPERAND_MASK_3;
        const BroadEventRecord* event = getEvent(e);
        const jni::JavaString eventName = event->getName();
        std::cerr << (const char*) eventName;
        break;
      }
    case EventTree::OPCODE_GOTO_2:
      {
        const uint32_t l = code & EventTree::OPERAND_MASK_2;
        std::cerr << "GOTO " << l;
        break;
      }
    default:
      break;
    }
    std::cerr << std::endl;
  }
}

#endif  /* DEBUG */


}  /* namespace waters */
