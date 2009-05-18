//# This may look like C code, but it really is -*- C++ -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.base
//# CLASS:   LinkedRecordList
//###########################################################################
//# $Id$
//###########################################################################


#ifndef _LinkedRecordList_h_
#define _LinkedRecordList_h_

#ifdef __GNUG__
#pragma interface
#endif

#if _MSC_VER >= 1000
#pragma once
#endif


namespace waters {


//############################################################################
//# class UntypedLinkedRecordAccessor <typeless>
//############################################################################

class UntypedLinkedRecordAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  UntypedLinkedRecordAccessor() {}
  virtual ~UntypedLinkedRecordAccessor() {}

  //##########################################################################
  //# Override by Subclasses
  virtual void* getUntypedNext(const void* record) const = 0;
  virtual void setUntypedNext(void* record, void* next) const = 0;
  virtual int compareUntyped(const void* record1, const void* record2) const =
    0;
};


//############################################################################
//# class UntypedLinkedRecordList <typeless>
//############################################################################

class UntypedLinkedRecordList
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit UntypedLinkedRecordList
    (const UntypedLinkedRecordAccessor* accessor);
  explicit UntypedLinkedRecordList
    (const UntypedLinkedRecordAccessor* accessor, void* record);

  //##########################################################################
  //# List Access
  bool isEmpty() const {return mHead == 0;}
  void* getUntypedHead() const {return mHead;}
  void* getUntypedTail() const {return mTail;}
  void appendUntyped(void* record);
  void appendUntyped(const UntypedLinkedRecordList& list);

  //##########################################################################
  //# Sorting
  void qsort();

private:
  //##########################################################################
  //# Auxiliary Methods
  void seek();

  //##########################################################################
  //# Data Members
  const UntypedLinkedRecordAccessor* mAccessor;
  void* mHead;
  void* mTail;
};


//############################################################################
//# class LinkedRecordAccessor <typed>
//############################################################################

template <class Record>
class LinkedRecordAccessor : public UntypedLinkedRecordAccessor
{
public:
  //##########################################################################
  //# Constructors & Destructors
  LinkedRecordAccessor() {}

  //##########################################################################
  //# Override by Subclasses
  virtual Record* getNext(const Record* record) const = 0;
  virtual void setNext(Record* record, Record* next) const = 0;
  virtual int compare(const Record* record1, const Record* record2) const = 0;

  //##########################################################################
  //# Override for UntypedLinkedRecordAccessor
  virtual void* getUntypedNext(const void* record) const
    {return getNext((const Record*) record);}
  virtual void setUntypedNext(void* record, void* next) const
    {setNext((Record*) record, (Record*) next);}
  virtual int compareUntyped(const void* record1, const void* record2) const
    {return compare((const Record*) record1, (const Record*) record2);}
};


//############################################################################
//# template LinkedRecordList <typed>
//############################################################################

template <class Record>
class LinkedRecordList : public UntypedLinkedRecordList
{
public:
  //##########################################################################
  //# Constructors & Destructors
  explicit LinkedRecordList(const LinkedRecordAccessor<Record>* accessor) :
    UntypedLinkedRecordList(accessor)
  {
  }

  explicit LinkedRecordList(const LinkedRecordAccessor<Record>* accessor,
			    Record* record) :
    UntypedLinkedRecordList(accessor, record)
  {
  }

  //##########################################################################
  //# List Access
  Record* getHead() const
    {return (Record*) UntypedLinkedRecordList::getUntypedHead();}
  Record* getTail() const
    {return (Record*) UntypedLinkedRecordList::getUntypedTail();}
  void append(Record* record)
    {UntypedLinkedRecordList::appendUntyped(record);}
  void append(const LinkedRecordList<Record>& list)
    {UntypedLinkedRecordList::appendUntyped(list);}
};


}   /* namespace waters */

#endif  /* !_LinkedRecordList_h_ */
