// DesCode_AutomatonThread.h: interface for the DesCode_AutomatonThread class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_DESCODE_AUTOMATONTHREAD_H__321261C1_FEF4_11D2_9C2C_006008911536__INCLUDED_)
#define AFX_DESCODE_AUTOMATONTHREAD_H__321261C1_FEF4_11D2_9C2C_006008911536__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "DesThread.h"

class DesCode_AutomatonInterface;
//This class schedules one or (later) many automata.

class DesCode_AutomatonThread : public DesThread  
{
public:
	// Constuctor:
    // (Use started == true iff thread should start running immediately;
    //  otherwise start() has to be called first.
	DesCode_AutomatonThread(bool started = false);
	// Destructor, deletes all automata and waits until thread ends.
	virtual ~DesCode_AutomatonThread();
	// Set the automaton; the old one, if existent, must be deleted previously.
	void setAutomaton(DesCode_AutomatonInterface* automaton);
	// Schedule automaton for deletion.
	void deleteAutomaton(DesCode_AutomatonInterface* automaton);
	// Interface function to the automatons to resume the thread if necessary.
	void newSignalledEvent(DesCode_AutomatonInterface* automaton);
	// Start the thread initially.
	void start();
	// Function called at entry of thread, e.g. CoInitialize().
	virtual void callAtThreadEntry() {};
	// Function called at exit of thread, e.g. CoUninitialize().
	virtual void callAtThreadExit() {};
protected:
	// Called by ThreadEntryFunc, implements the scheduling.
	virtual DWORD WINAPI ThreadFunc();
private:
	// Delete the automaton and set member to zero.
	void deleteAutomaton();

	// We only schedule one automaton per thread in this version.
	DesCode_AutomatonInterface* mAutomaton;
	// An event has been signalled.
	bool mEventArrived;
	// Automaton scheduled for deletion.
	DesCode_AutomatonInterface* mAutomatonDelete;
	// Don't start as long as the user allows it.
    // (avoid construction problems when started with an uncontrollable)
	bool m_started;
};

#endif // !defined(AFX_DESCODE_AUTOMATONTHREAD_H__321261C1_FEF4_11D2_9C2C_006008911536__INCLUDED_)
