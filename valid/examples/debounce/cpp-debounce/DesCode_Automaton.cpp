/* 
  Automaton baseclass for DES-to-C++-Generator, Revision 2.0
  (C) Siemens AG, ZT SE 4, 1998. All Rights Reserved.
  Authors: Dr. Markus Kaltenbach, Dr. Robi Malik, 
  (c) Siemens AG, ZT SE 4, 1999 Martin Witte
*/

#ifdef __GNUG__
#pragma implementation
#endif

#include "DesCode_Automaton.h"

#include <assert.h>
#include <stdio.h>
#include <string.h>

#ifdef DES_SIMULATOR
extern void setEvent (const char* eventname);
#endif

//=============================DesCode_SignalledEvent==========================
#ifdef DES_ONLY_ONE_EVENT
bool operator==(const DesCode_SignalledEvent& lhs,
		const DesCode_SignalledEvent& rhs)
{
	return ((lhs.mEventFamily == rhs.mEventFamily)
		&& (lhs.mParameter == rhs.mParameter));
}
#endif

//=======================DesCode_SignalledEvent============================== 

//single linked list of DesCode_SignalledEvent

DesCode_EventQueue::DesCode_EventQueue()
:	mFirst(0),
	mLast(0)
{
}

DesCode_EventQueue::~DesCode_EventQueue()
{
	clear();
}

//add at the top of the list, use with lifo
void DesCode_EventQueue::push(DesCode_SignalledEvent* event)
{
	assert(event);
	assert(event->mNext == 0);
	if(mFirst) {	//queue not empty
		event->mNext = mFirst;
		mFirst = event;
	}
	else {	//queue empty
		assert(mLast == 0);
		mLast = mFirst = event;
	}
}

//add at end of the list, use with fifo
void DesCode_EventQueue::append(DesCode_SignalledEvent* event)
{
	assert(event);
	assert(event->mNext == 0);
	if(mFirst) {
		assert(mLast->mNext == 0);
		mLast->mNext = event;
		mLast = event;
	}
	else {
		assert(mLast == 0);
		mFirst = mLast = event;
	}
}

	//clear list
void DesCode_EventQueue::clear()
{
	while(!isEmpty()){
		delete pop();
	}
}

	//get first element and remove it from list; returns 0 if list empty
DesCode_SignalledEvent* DesCode_EventQueue::pop()
{
	if (mFirst){	//queue not empty
		DesCode_SignalledEvent* ret = mFirst;
		mFirst = mFirst->mNext;
		if(mFirst == 0){
			mLast = 0;
		}
		return ret;
	}
	return 0;
}


//remove event after the event given as parameter
//true on success
bool DesCode_EventQueue::removeAfter(DesCode_SignalledEvent* event)
{	//can not remove mFirst and empty queue
	assert(event);
	if(event->mNext) {	//there is a next element
		event->mNext = event->mNext->mNext;
		if(event->mNext == 0)	//removed last element
			mLast = event;
		return true;
	}
	return false;	//nothing done
}




//############################################################################
//# CLASS DesCode_Automaton
//############################################################################
//# This is a default base class for all DES automata ---
//# Every generated automaton will be a subclass of it.
//# This version has been rewritten for event-driven synchronization using
//# the SiPlace framework.
//############################################################################

//# DesCode_Automaton: General object handling ###################################


DesCode_Automaton::DesCode_Automaton ()	
#ifdef WITHTRACE
:	mTraceFunc(0)
#endif
{
}

DesCode_Automaton::~DesCode_Automaton ()
{
	mUncontrollableEventQueue.clear();
}

void DesCode_Automaton::init()
{
	mUncontrollableEventQueue.clear();
}




//# DesCode_Automaton: Standard methods ########################################

bool DesCode_Automaton::step()
{	//version which throws away the signalled uncontr. event, even if it is not allowed
	if(!mUncontrollableEventQueue.isEmpty()) {
		DesCode_SignalledEvent* event = mUncontrollableEventQueue.pop();
#ifdef WITHTRACE
		bool allowed = stepUncontrollable(*(event));
		if(!allowed)
			debugWarning(*event,"is not allowed!");
#else
		stepUncontrollable(*(event));
#endif
		delete event;
		return true;
	}
	return stepControllables();
}

void DesCode_Automaton::signalEvent(DesCode_SignalledEvent* newEvent)
{
	//===fifo
#ifdef DES_ONLY_ONE_EVENT
	DesCode_SignalledEvent* event = mUncontrollableEventQueue.getFirst();
	while (event && !(event == newEvent))
		event = mUncontrollableEventQueue.getNext(event);
	if(event) {
#ifdef WITHTRACE
		debugWarning(*event," already signalled!");
#endif
		delete newEvent;
	}
	else
		mUncontrollableEventQueue.append(newEvent);
#else
	mUncontrollableEventQueue.append(newEvent);
#endif
	//===lifo
	//mUncontrollableEventQueue.push(new DesCode_SignalledEvent(evFamily,param));
}


//# Default event methods ####################################################

#if defined(WITHTRACE) || defined(DES_SIMULATOR)

#if defined(WITHTRACE)
void DesCode_Automaton::setTraceFunction(DesCode_TraceFunction* f)
{
	mTraceFunc = f;
}
#endif

#if defined(WITHTRACE)
void DesCode_Automaton::printTrace(const char* line)
{
	(*mTraceFunc)(line);
}
#endif

void DesCode_Automaton::
debugAction (const DesCode_SignalledEvent& event)
{
	char* eventName = getEventName(event);
#ifdef WITHTRACE
	int len = strlen(eventName);
	char* line = new char[len+15];
	sprintf(line,"EXECUTING: %s\n",eventName);
	printTrace(line);
	delete[] line;
#endif
#ifdef DES_SIMULATOR
	setEvent (eventName);
#endif
  	delete[] eventName;
}


void DesCode_Automaton::
debugSignal (const DesCode_SignalledEvent& event)
{
#ifdef WITHTRACE
	char* eventName = getEventName(event);
	int len = strlen(eventName);
	char* line = new char[len+15];
	sprintf(line,"GOT EVENT: %s\n",eventName);
	printTrace(line);
	delete[] line;
	delete[] eventName;
#endif
}

void DesCode_Automaton::debugWarning (const DesCode_SignalledEvent& event,const char* text)
{
#ifdef WITHTRACE
	char* eventName = getEventName(event);
	int len = strlen(eventName) + strlen(text);
	char* line = new char[len+35];
	sprintf(line,"WARNING: uncontrollable %s %s\n",eventName,text);
	printTrace(line);
	delete[] line;
	delete[] eventName;
#endif
}

#endif
