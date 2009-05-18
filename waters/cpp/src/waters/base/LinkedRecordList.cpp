//# -*- indent-tabs-mode: nil -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   LinkedRecordList
//###########################################################################
//# $Id$
//###########################################################################

#ifdef __GNUG__
#pragma implementation
#endif

#include <iostream>
#include <new>

#include "waters/base/LinkedRecordList.h"


namespace waters {


//###########################################################################
//# Class UntypedLinkedRecordList
//###########################################################################

//###########################################################################
//# UntypedLinkedRecordList: Constructors & Destructors

UntypedLinkedRecordList::
UntypedLinkedRecordList(const UntypedLinkedRecordAccessor* accessor) :
  mAccessor(accessor),
  mHead(0),
  mTail(0)
{
}

UntypedLinkedRecordList::
UntypedLinkedRecordList(const UntypedLinkedRecordAccessor* accessor,
                        void* record) :
  mAccessor(accessor),
  mHead(record),
  mTail(record)
{
  seek();
}


//###########################################################################
//# UntypedLinkedRecordList: List Access

void UntypedLinkedRecordList::
appendUntyped(void* record)
{
  if (mHead == 0) {
    mHead = mTail = record;
  } else {
    mAccessor->setUntypedNext(mTail, record);
    mTail = record;
  }
  seek();
}

void UntypedLinkedRecordList::
appendUntyped(const UntypedLinkedRecordList& list)
{
  void* head = list.mHead;
  if (mHead == 0) {
    mHead = head;
  } else {
    mAccessor->setUntypedNext(mTail, head);
  }
  if (void* tail = list.mTail) {
    mTail = tail;
  }
}

//###########################################################################
//# UntypedLinkedRecordList: Sorting

void UntypedLinkedRecordList::
qsort()
{
  void* pivot = mHead;
  if (pivot != 0 && mAccessor->getUntypedNext(pivot) != 0) {
    mHead = mTail = 0;
    UntypedLinkedRecordList after(mAccessor);
    void* next = mAccessor->getUntypedNext(pivot);
    mAccessor->setUntypedNext(pivot, 0);
    do {
      void* current = next;
      next = mAccessor->getUntypedNext(current);
      mAccessor->setUntypedNext(current, 0);
      if (mAccessor->compareUntyped(pivot, current) > 0) {
        appendUntyped(current);
      } else {
        after.appendUntyped(current);
      }
    } while (next);
    qsort();
    appendUntyped(pivot);
    after.qsort();
    appendUntyped(after);
  }
}


//############################################################################
//# TransitionRecordList: Auxiliary Methods
  
void UntypedLinkedRecordList::
seek()
{
  if (mTail != 0) {
    while (void* next = mAccessor->getUntypedNext(mTail)) {
      mTail = next;
    }
  }
}


}  /* namespace waters */
