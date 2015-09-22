//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

#ifndef _EventTree_h_
#define _EventTree_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif

#include <stdint.h>

#include "waters/base/ArrayList.h"
#include "waters/base/BitSet.h"
#include "waters/base/HashTable.h"
#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/TransitionRecord.h"


namespace waters {

class BroadEventRecord;
class EventTreeGenerator;


//###########################################################################
//# Class EventTree
//###########################################################################

class EventTree
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTree() {};
  virtual ~EventTree() {};

  //#########################################################################
  //# Simple Access
  inline bool isEmpty() const {return mCode.isEmpty();}
  inline uint32_t getCodeSize() const {return mCode.size();}
  inline uint32_t get(uint32_t index) const {return mCode.get(index);}
  inline void set(uint32_t index, uint32_t code) {mCode.set(index, code);}

  inline FastEligibilityTestRecord& getEligibilityRecord(uint32_t index)
    {return mEligibilityRecords.getref(index);}
  inline const FastEligibilityTestRecord&
    getEligibilityRecord(uint32_t index) const
    {return mEligibilityRecords.getref(index);}
  inline uint32_t prepareEligibilityRecord()
    {return mEligibilityRecords.prepare();}
  inline void addEligibilityRecord(uint32_t t)
    {if (mEligibilityRecords.size() == t) mEligibilityRecords.add();}

  //#########################################################################
  //# Code Generation
  void appendCase(int aut);
  void appendIfDisabled(uint32_t trans, uint32_t task);
  void appendExecute(uint32_t event);
  void appendFail(uint32_t event);
  void appendGoto(uint32_t task);
  void appendRaw(uint32_t value);

private:
  //#########################################################################
  //# Data Members
  ArrayList<uint32_t> mCode;
  ArrayList<FastEligibilityTestRecord> mEligibilityRecords;

public:
  //#########################################################################
  //# Class Constants
  static const uint32_t OPCODE_SHIFT_2 = 30;
  static const uint32_t OPERAND_MASK_2 = (1 << OPCODE_SHIFT_2) - 1;
  static const uint32_t OPCODE_MASK_2 = ~OPERAND_MASK_2;

  static const uint32_t OPCODE_SHIFT_3 = 29;
  static const uint32_t OPERAND_MASK_3 = (1 << OPCODE_SHIFT_3) - 1;
  static const uint32_t OPCODE_MASK_3 = ~OPERAND_MASK_3;

  static const uint32_t OPCODE_CASE_2 = 0x0 << OPCODE_SHIFT_2;  // 00*
  static const uint32_t OPCODE_IFNN_2 = 0x1 << OPCODE_SHIFT_2;  // 01*
  static const uint32_t OPCODE_EXEC_2 = 0x2 << OPCODE_SHIFT_2;  // 100
  static const uint32_t OPCODE_EXEC_3 = OPCODE_EXEC_2;
  static const uint32_t OPCODE_FAIL_2 = OPCODE_EXEC_2;          // 101
  static const uint32_t OPCODE_FAIL_3 = OPCODE_FAIL_2 | (1 << OPCODE_SHIFT_3);
  static const uint32_t OPCODE_GOTO_2 = 0x3 << OPCODE_SHIFT_2;  // 11*
};


//###########################################################################
//# Class EventTreeTaskHashAccessor
//###########################################################################

class EventTreeTaskHashAccessor : public PtrHashAccessor
{
private:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTreeTaskHashAccessor() {};
  friend class EventTreeTask;

public:
  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(intptr_t key) const;
  virtual bool equals(intptr_t key1, intptr_t key2) const;
};


//############################################################################
//# enumeration EventTreeTaskType
//############################################################################

enum EventTreeTaskType {
  TASK_TYPE_END = 0,
  TASK_TYPE_FAIL = 1,
  TASK_TYPE_SPLIT = 2
};


//############################################################################
//# class EventTreeTask
//############################################################################

class EventTreeTask
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTreeTask();
  virtual ~EventTreeTask() {};

  //##########################################################################
  //# Simple Access
  inline uint32_t getTaskID() const {return mTaskID;}
  inline void setTaskID(uint32_t id) {mTaskID = id;}
  virtual EventTreeTaskType getTaskType() const = 0;

  //##########################################################################
  //# Comparing & Hashing
  virtual bool equals(const EventTreeTask& task) const = 0;
  virtual uint64_t hash() const = 0;
  static const EventTreeTaskHashAccessor* getHashAccessor()
    {return &theHashAccessor;}

  //##########################################################################
  //# Code Generation
  virtual void execute() = 0;

private:
  //##########################################################################
  //# Data Members
  uint32_t mTaskID;

  //##########################################################################
  //# Class Variables
  static const EventTreeTaskHashAccessor theHashAccessor;
};


//############################################################################
//# class EventTreeTaskSplit
//############################################################################

class EventTreeTaskSplit : public EventTreeTask
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTreeTaskSplit(EventTreeTask* next,
			      EventTreeGenerator& generator);
  explicit EventTreeTaskSplit(const ArrayList<uint32_t>& events,
			      const ArrayList<uint32_t>& automata,
			      uint32_t fanout,
			      bool splittable,
			      EventTreeTask* next,
			      EventTreeGenerator& generator);

  //##########################################################################
  //# Simple Access
  virtual EventTreeTaskType getTaskType() const {return TASK_TYPE_SPLIT;}

  //##########################################################################
  //# Comparing & Hashing
  virtual bool equals(const EventTreeTask& task) const;
  virtual uint64_t hash() const;

  //##########################################################################
  //# Code Generation
  virtual void execute();
  virtual void executeWithoutSplit();
  EventTreeTask* addTaskWithoutEvent(uint32_t event) const;
  EventTreeTask* addTaskWithEvents(const ArrayList<uint32_t>& events) const;
  uint32_t findFailedEvent() const;

private:
  //##########################################################################
  //# Data Members
  IntArrayList<uint32_t> mEventList;
  IntArrayList<uint32_t> mAutomataList;
  BitSet mAutomataSet;
  uint32_t mFanout;
  bool mSplittable;
  EventTreeGenerator& mGenerator;
  EventTreeTask* mNextTask;

  //##########################################################################
  //# Class Variables
  static const uint64_t theHashCode;
};


//############################################################################
//# class EventTreeTaskFail
//############################################################################

class EventTreeTaskFail : public EventTreeTask
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTreeTaskFail(uint32_t event, EventTreeGenerator& generator);

  //##########################################################################
  //# Simple Access
  virtual EventTreeTaskType getTaskType() const {return TASK_TYPE_FAIL;}

  //##########################################################################
  //# Comparing & Hashing
  virtual bool equals(const EventTreeTask& task) const;
  virtual uint64_t hash() const;

  //##########################################################################
  //# Code Generation
  virtual void execute();

private:
  //##########################################################################
  //# Data Members
  const uint32_t mEvent;
  EventTreeGenerator& mGenerator;

  //##########################################################################
  //# Class Variables
  static const uint64_t theHashCode;
};


//############################################################################
//# class EventTreeTaskEnd
//############################################################################

class EventTreeTaskEnd : public EventTreeTask
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTreeTaskEnd() {};

  //##########################################################################
  //# Simple Access
  virtual EventTreeTaskType getTaskType() const {return TASK_TYPE_END;}

  //##########################################################################
  //# Comparing & Hashing
  virtual bool equals(const EventTreeTask& task) const
    {return task.getTaskType() == TASK_TYPE_END;}
  virtual uint64_t hash() const {return theHashCode;}

  //##########################################################################
  //# Code Generation
  virtual void execute() {}

private:
  //##########################################################################
  //# Class Variables
  static const uint64_t theHashCode;
};


//###########################################################################
//# Class EventTreeGenerator
//###########################################################################

class EventTreeGenerator : public Int32HashAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit EventTreeGenerator(const AutomatonEncoding& encoding,
			      const ArrayList<BroadEventRecord*>& events,
			      EventTree& output,
			      bool safety = false);
  virtual ~EventTreeGenerator();

  //#########################################################################
  //# Invocation
  void execute();
  void executeTask(EventTreeTask* task);

  //#########################################################################
  //# Task Management
  EventTreeTask* peekNextTask() const;
  EventTreeTask* addSplitTask(const ArrayList<uint32_t>& events,
			      const ArrayList<uint32_t>& automata,
			      uint32_t fanout,
			      bool splittable,
			      EventTreeTask* next);
  EventTreeTask* addFailTask(uint32_t event);
  EventTreeTask* addTask(EventTreeTask* task);

  //#########################################################################
  //# Simple Access
  inline bool isSafety() const {return mSafety;}

  //#########################################################################
  //# Event Access
  inline uint32_t getNumberOfEvents() const {return mEvents.size();}
  inline const BroadEventRecord* getEvent(uint32_t event) const
    {return mEvents.get(event);}
  bool isAlwaysEnabled(uint32_t event, const BitSet& automata) const;
  bool isEnabled(uint32_t event, int aut, uint32_t state) const;

  //#########################################################################
  //# Automata Access
  inline uint32_t getNumberOfAutomata() const
    {return mAutomatonEncoding.getNumberOfAutomata();}
  inline AutomatonRecord* getAutomaton(int a) const
    {return mAutomatonEncoding.getRecord(a);}
  void resetTopCounts();
  void registerTopCounts
    (uint32_t event, const BitSet& automata, uint32_t fanout);
  uint32_t findTopAutomaton(const IntArrayList<uint32_t>& automata) const;
  bool isTopAutomaton(int aut, uint32_t event,
		      const BitSet& automata, uint32_t fanout) const;

  //#########################################################################
  //# Code Generation
  void appendCase(int aut);
  void appendIfDisabled(const TransitionRecord* trans,
			uint32_t event, uint32_t task);
  void appendExecute(uint32_t event);
  void appendFail(uint32_t event);
  void appendGotoUnlessNext(const EventTreeTask* task);
  void appendGoto(uint32_t task);
  void appendRaw(uint32_t value);
  void renumber();

  //##########################################################################
  //# Hash Methods
  virtual uint64_t hash(int32_t key) const;
  bool equals(int32_t key1, int32_t key2) const;

  //#########################################################################
  //# Debugging
#ifdef DEBUG
  void dump() const;
#endif /* DEBUG */

private:
  //#########################################################################
  //# Data Members
  const AutomatonEncoding& mAutomatonEncoding;
  const ArrayList<BroadEventRecord*>& mEvents;
  EventTree& mOutput;
  bool mSafety;
  bool mHasFailedEvents;
  uint32_t mMaxFanout;

  ArrayList<EventTreeTaskSplit*> mSplitTasks;
  ArrayList<EventTreeTaskFail*> mFailTasks;
  EventTreeTaskEnd* mEndTask;
  PtrHashTable<EventTreeTask*,EventTreeTask*> mAllTasks;
  Int32HashTable<uint32_t,uint32_t> mEligibilityRecordTable;
  ArrayList<uint32_t> mLineNumbers;

  uint32_t* mTopCounts;
};

}   /* namespace waters */

#endif  /* !_EventTree_h_ */








