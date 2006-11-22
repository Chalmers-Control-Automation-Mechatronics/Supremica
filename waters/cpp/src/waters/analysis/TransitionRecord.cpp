//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionRecord
//###########################################################################
//# $Id: TransitionRecord.cpp,v 1.3 2006-11-22 21:27:57 robi Exp $
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <new>

#include "waters/analysis/AutomatonEncoding.h"
#include "waters/analysis/StateRecord.h"
#include "waters/analysis/TransitionRecord.h"


namespace waters {

//############################################################################
//# class TransitionRecord
//############################################################################

//############################################################################
//# TransitionRecord: Constructors & Destructors

TransitionRecord::
TransitionRecord(const AutomatonRecord* aut, TransitionRecord* next)
  : mAutomaton(aut),
    mWeight(0),
    mIsOnlySelfloops(true),
    mNextInSearch(next),
    mNextInUpdate(0)
{
  const uint32 numstates = (uint32) aut->getNumberOfStates();
  mShiftedSuccessors = new uint32[numstates];
  for (uint32 code = 0; code < numstates; code++) {
    mShiftedSuccessors[code] = UNDEF_UINT32;
  }
}

TransitionRecord::
~TransitionRecord()
{
  delete mNextInSearch;
}


//############################################################################
//# TransitionRecord: Comparing

int TransitionRecord::
compareToForSearch(const TransitionRecord* partner)
  const
{
  const AutomatonRecord* aut1 = mAutomaton;
  const bool isplant1 = aut1->isPlant();
  const AutomatonRecord* aut2 = partner->mAutomaton;
  const bool isplant2 = aut2->isPlant();
  if (isplant1 && !isplant2) {
    return -1;
  } else if (!isplant1 && isplant2) {
    return 1;
  }
  const int weight1 = mWeight;
  const int weight2 = partner->mWeight;
  if (weight1 != weight2) {
    return weight1 - weight2;
  } else {
    return aut1->compareTo(aut2);
  }
}

int TransitionRecord::
compareToForTrace(const TransitionRecord* partner)
  const
{
  const AutomatonRecord* aut1 = mAutomaton;
  const AutomatonRecord* aut2 = partner->mAutomaton;
  const int weight1 = mWeight / aut1->getNumberOfStates();
  const int weight2 = partner->mWeight / aut2->getNumberOfStates();
  if (weight1 != weight2) {
    return weight1 - weight2;
  } else {
    return aut1->compareTo(aut2);
  }
}

int TransitionRecord::
compareForSearch(const TransitionRecord* trans1,
                 const TransitionRecord* trans2)
{
  return trans1->compareToForSearch(trans2);
}

int TransitionRecord::
compareForTrace(const TransitionRecord* trans1,
                const TransitionRecord* trans2)
{
  return trans1->compareToForTrace(trans2);
}


//############################################################################
//# TransitionRecord: Set up

bool TransitionRecord::
addTransition(const StateRecord* source, const StateRecord* target)
{
  const uint32 sourcecode = source->getStateCode();
  const uint32 targetcode = target->getStateCode();
  if (mShiftedSuccessors[sourcecode] == UNDEF_UINT32) {
    const int shift = mAutomaton->getShift();
    mShiftedSuccessors[sourcecode] = targetcode << shift;
    mWeight++;
    if (sourcecode != targetcode) {
      mIsOnlySelfloops = false;
    }
    return true;
  } else {
    return false;
  }
}

void TransitionRecord::
normalize()
{
  const int numstates = mAutomaton->getNumberOfStates();
  if (mWeight == numstates) {
    mWeight = PROBABILITY_1;
  } else {
    mWeight *= PROBABILITY_1 / numstates;
  }
}

uint32 TransitionRecord::
getCommonTarget()
  const
{
  const uint32 numstates = (uint32) mAutomaton->getNumberOfStates();
  uint32 result = UNDEF_UINT32;
  for (uint32 code = 0; code < numstates; code++) {
    const uint32 succ = mShiftedSuccessors[code];
    if (succ != UNDEF_UINT32) {
      if (result == UNDEF_UINT32) {
        result = succ;
      } else {
        return UNDEF_UINT32;
      }
    }      
  }
  return result;
}


//############################################################################
//# class TransitionRecordList
//############################################################################

//############################################################################
//# TransitionRecordList: Constructors & Destructors
  
TransitionRecordList::
TransitionRecordList() :
  mHead(0),
  mTail(0)
{
}

TransitionRecordList::
TransitionRecordList(TransitionRecord* record) :
  mHead(record),
  mTail(record)
{
  seek();
}


//############################################################################
//# TransitionRecordList: Access
  
void TransitionRecordList::
append(TransitionRecord* record)
{
  if (mHead == 0) {
    mHead = mTail = record;
  } else {
    mTail->setNextInSearch(record);
    mTail = record;
  }
  seek();
}

void TransitionRecordList::
append(const TransitionRecordList& list)
{
  TransitionRecord* head = list.mHead;
  if (mHead == 0) {
    mHead = head;
  } else {
    mTail->setNextInSearch(head);
  }
  if (TransitionRecord* tail = list.mTail) {
    mTail = tail;
  }
}


//############################################################################
//# TransitionRecordList: Sorting

void TransitionRecordList::
qsort(TransitionRecordComparator comparator)
{
  TransitionRecord* pivot = mHead;
  if (pivot != 0 && pivot->getNextInSearch() != 0) {
    mHead = mTail = 0;
    TransitionRecordList after;
    TransitionRecord* next = pivot->getNextInSearch();
    pivot->setNextInSearch(0);
    do {
      TransitionRecord* current = next;
      next = current->getNextInSearch();
      current->setNextInSearch(0);
      if (comparator(pivot, current) > 0) {
        append(current);
      } else {
        after.append(current);
      }
    } while (next);
    qsort(comparator);
    append(pivot);
    after.qsort(comparator);
    append(after);
  }
}


//############################################################################
//# TransitionRecordList: Auxiliary Methods
  
void TransitionRecordList::
seek()
{
  if (mTail != 0) {
    while (TransitionRecord* next = mTail->getNextInSearch()) {
      mTail = next;
    }
  }
}


}  /* namespace waters */
