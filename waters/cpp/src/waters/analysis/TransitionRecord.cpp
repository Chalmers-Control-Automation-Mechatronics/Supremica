//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.analysis
//# CLASS:   TransitionRecord
//###########################################################################
//# $Id: TransitionRecord.cpp,v 1.2 2006-09-04 11:04:41 robi Exp $
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
    mNext(next)
{
  const uint32 numstates = (uint32) aut->getNumberOfStates();
  mSuccessorStates = new uint32[numstates];
  for (uint32 code = 0; code < numstates; code++) {
    mSuccessorStates[code] = UNDEF_UINT32;
  }
}

TransitionRecord::
~TransitionRecord()
{
  delete mNext;
}


//############################################################################
//# TransitionRecord: Comparing

int TransitionRecord::
compareTo(const TransitionRecord* partner)
  const
{
  const int weight1 = mWeight;
  const int weight2 = partner->mWeight;
  const AutomatonRecord* aut1 = mAutomaton;
  const AutomatonRecord* aut2 = partner->mAutomaton;
  if (weight1 == PROBABILITY_1) {
    return weight2 == PROBABILITY_1 ? aut1->compareTo(aut2) : 1;
  } else if (weight2 == PROBABILITY_1) {
    return -1;
  } else if (aut1->isPlant() && !aut2->isPlant()) {
    return -1;
  } else if (!aut1->isPlant() && aut2->isPlant()) {
    return 1;
  } else if (weight1 != weight2) {
    return weight1 - weight2;
  } else {
    return aut1->compareTo(aut2);
  }
}

int TransitionRecord::
compare(const void* elem1, const void* elem2)
{
  const TransitionRecord* val1 = *((const TransitionRecord**) elem1);
  const TransitionRecord* val2 = *((const TransitionRecord**) elem2);
  return val1->compareTo(val2);
}


//############################################################################
//# TransitionRecord: Set up

bool TransitionRecord::
addTransition(const StateRecord* source, const StateRecord* target)
{
  const uint32 sourcecode = source->getStateCode();
  const uint32 targetcode = target->getStateCode();
  if (mSuccessorStates[sourcecode] == UNDEF_UINT32) {
    mSuccessorStates[sourcecode] = targetcode;
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
    mTail->setNext(record);
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
    mTail->setNext(head);
  }
  if (TransitionRecord* tail = list.mTail) {
    mTail = tail;
  }
}


//############################################################################
//# TransitionRecordList: Sorting

void TransitionRecordList::
qsort()
{
  TransitionRecord* pivot = mHead;
  if (pivot != 0 && pivot->getNext() != 0) {
    mHead = mTail = 0;
    TransitionRecordList after;
    TransitionRecord* next = pivot->getNext();
    pivot->setNext(0);
    do {
      TransitionRecord* current = next;
      next = current->getNext();
      current->setNext(0);
      if (pivot->compareTo(current) > 0) {
        append(current);
      } else {
        after.append(current);
      }
    } while (next);
    qsort();
    append(pivot);
    after.qsort();
    append(after);
  }
}


//############################################################################
//# TransitionRecordList: Auxiliary Methods
  
void TransitionRecordList::
seek()
{
  while (TransitionRecord* next = mTail->getNext()) {
    mTail = next;
  }
}


}  /* namespace waters */
