// DesCode_AutomatonThread.cpp: implementation of the DesCode_AutomatonThread class.
//
//////////////////////////////////////////////////////////////////////

#include "DesCode_AutomatonThread.h"
#include "DesCode_AutomatonInterface.h"
#include <assert.h>


//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

DesCode_AutomatonThread::DesCode_AutomatonThread(bool started)
:	mAutomaton(0),
	mEventArrived(false),
	mAutomatonDelete(0),
	m_started(started)
{
}

DesCode_AutomatonThread::~DesCode_AutomatonThread()
{
	m_started = true;
	mAutomatonDelete = mAutomaton;
	waitForThreadTerminated();
	delete mAutomaton;
}

void DesCode_AutomatonThread::setAutomaton(DesCode_AutomatonInterface* automaton)
{
	assert(mAutomaton == 0);
	mAutomaton = automaton;
	mAutomaton->init();
	mAutomaton->setThread(this);
}

	//schedule the automaton for deletion 
void DesCode_AutomatonThread::deleteAutomaton(DesCode_AutomatonInterface* automaton)
{
	assert(mAutomaton == automaton);
	mAutomatonDelete = automaton;
}


	//interface function to the automatons to resume the thread if necessary.
void DesCode_AutomatonThread::newSignalledEvent(DesCode_AutomatonInterface* /*automaton*/)
{
	mEventArrived = true;
	resume();
}

void DesCode_AutomatonThread::start()
{
	m_started = true;
	resume();
}

	//called by ThreadEntryFunc, implements the scheduling
DWORD WINAPI DesCode_AutomatonThread::ThreadFunc()
{
	callAtThreadEntry();
	while(!m_started)
		suspend();
	while(true){
		while(mAutomaton && !mAutomatonDelete && mAutomaton->step()){
			if(threadScheduledForTermination())
				return 0;
		}
		if(threadScheduledForTermination())
			return 0;
		if(mAutomatonDelete)
			deleteAutomaton();
		if(mEventArrived) 
			mEventArrived = false;
		else
			suspend();
	}
	callAtThreadExit();
	return 0;
}

	//really delete the automaton and set member to zero
void DesCode_AutomatonThread::deleteAutomaton()
{
	delete mAutomaton;
	mAutomaton = 0;
	mAutomatonDelete = 0;
}
