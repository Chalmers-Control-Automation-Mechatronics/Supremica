//############################################################################
//# Automaton baseclass for DES-to-C++-Generator, Revision 2.0
//# (C) Siemens AG, ZT SE 4, 1998. All Rights Reserved.
//# Authors: Dr. Markus Kaltenbach, Dr. Robi Malik
//############################################################################
//# New Version with event Queue 4/99
//# Martin Witte
//############################################################################

#ifndef _DESCODE_AUTOMATONTHREADED_H_
#define _DESCODE_AUTOMATONTHREADED_H_

#if _MSC_VER >= 1000
#pragma once
#endif

#ifdef __GNUG__
#pragma interface
#endif

#include <assert.h>
#include "DesCode_AutomatonThread.h"
#include "DesCode_AutomatonInterface.h"

#pragma warning(disable:4786)


//############################################################################
//# CLASS DesCode_AutomatonThreaded
//############################################################################
//# This is a default base class for all DES automata with queuing uncontrollables ---
//# Every generated automaton will be a subclass of it.
//############################################################################

typedef void DesCode_TraceFunction(const char* line);

class DesCode_SignalledEvent
{
public:
	DesCode_SignalledEvent(
		const unsigned short& evFamily,
		const unsigned short& parameter = 0)
		:	mEventFamily(evFamily),mParameter(parameter),mNext(0)
	{}
	~DesCode_SignalledEvent(){}

	//points to the existing event family definition
	const unsigned short mEventFamily;
	//points to the parameter
	const unsigned short mParameter;
	//for single linked list
	DesCode_SignalledEvent* mNext;
};

#ifdef DES_ONLY_ONE_EVENT
	bool operator==(const DesCode_SignalledEvent& lhs,
		const DesCode_SignalledEvent& rhs);
#endif

//single linked list of DesCode_SignalledEvent objects
class DesCode_EventQueue
{
public:
	DesCode_EventQueue();
	~DesCode_EventQueue();
	
	//list is empty or not?
	bool isEmpty(){return mFirst == 0;}
	//get first element
	DesCode_SignalledEvent* getFirst(){return mFirst;}
	//get last element
	DesCode_SignalledEvent* getLast(){return mLast;}
	//get the successor of the given event
	DesCode_SignalledEvent* getNext(DesCode_SignalledEvent* pred){
		assert(pred); return pred->mNext;
	}

		//add at the top of the list, use with lifo
	void push(DesCode_SignalledEvent* event);
	//add at end of the list, use with fifo
	void append(DesCode_SignalledEvent* event);
	
	//clear list, deletes the list members
	void clear();

	//get first element and remove it from list
	DesCode_SignalledEvent* pop();
	//remove event after "event" from the list; fast!
	bool removeAfter(DesCode_SignalledEvent* event);
	void lock(){EnterCriticalSection(&mProtect);}
	void unlock(){LeaveCriticalSection(&mProtect);}
private:
	DesCode_SignalledEvent* mFirst;
	DesCode_SignalledEvent* mLast;
	CRITICAL_SECTION mProtect;
};


class DesCode_AutomatonThreaded : public DesCode_AutomatonInterface {
public:
	/* General object handling */
	DesCode_AutomatonThreaded ();
	virtual ~DesCode_AutomatonThreaded ();
	
	//set controlling thread. Called by DesCode_AutomatonThread::setAutomaton()
	void setThread(DesCode_AutomatonThread* thread){mThread = thread;}
	//Initialize the queue (empty it). can be called anytime.
	virtual void init();
	//make one step. Implements the lokal scheduling policy.
	virtual bool step();
	//is automaton in marked state?
	virtual bool isMarkedState() const = 0;
	//unused old execute method
	virtual void execute(){};
#if defined(WITHTRACE) 
	//set the trace stream
	void setTraceFunction(DesCode_TraceFunction* f);
#endif
protected:
	//queue functions for uncontrollable events, creates a new event
	void signalEvent(DesCode_SignalledEvent* newEvent);
	//make an uncontrollable step if possible; returns true if uncontrollable was allowed
	virtual bool stepUncontrollable(const DesCode_SignalledEvent& event) = 0;
	//make the next controllable step; returns true if a transition was performed
	virtual bool stepControllables() = 0;

#if defined(WITHTRACE) || defined (DES_SIMULATOR)
	//get name of event
	virtual char* getEventName(const DesCode_SignalledEvent&) const = 0;
	virtual void debugAction(const DesCode_SignalledEvent&);
	virtual void debugSignal(const DesCode_SignalledEvent&);
	virtual void debugWarning(const DesCode_SignalledEvent&, const char* text);
#endif

private :
	//stuff for tracing
#if defined(WITHTRACE)
	DesCode_TraceFunction* mTraceFunc;
	void printTrace(const char* line);
#endif
	//the thread controlling this automaton
	DesCode_AutomatonThread* mThread;
	//the event queue for signalled uncontrollables
	DesCode_EventQueue mUncontrollableEventQueue;
};  /* class DesCode_AutomatonThreaded */

#endif  /* !_DESCODE_AUTOMATONTHREADED_H_ */
