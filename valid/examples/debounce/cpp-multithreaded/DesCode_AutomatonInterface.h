// DesCode_AutomatonInterface.h: interface for the DesCode_AutomatonInterface class.
//
// no DesCode_AutomatonInterface.cpp file necessary
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_DESCODE_AUTOMATONINTERFACE_H__321261C2_FEF4_11D2_9C2C_006008911536__INCLUDED_)
#define AFX_DESCODE_AUTOMATONINTERFACE_H__321261C2_FEF4_11D2_9C2C_006008911536__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

class DesCode_AutomatonThread;
//	Defines the interface between the thread controlling the Automaton and the
//	special automaton.
class DesCode_AutomatonInterface  
{
public:
	DesCode_AutomatonInterface(){};
	virtual ~DesCode_AutomatonInterface(){};
	//set thread which controlls this automaton
	virtual void setThread(DesCode_AutomatonThread* /*controllingThread*/) = 0;
	//initialize the automaton; reinitialization is possible
	virtual void init() = 0;
	//perform one transition step; returns true if one was performed
	virtual bool step() = 0;
	//execute the automaton infinitely; usage is discouraged 
	virtual void execute() = 0;
	//returns true iff automaton is in a marked state
	virtual bool isMarkedState() const = 0;
};


#endif // !defined(AFX_DESCODE_AUTOMATONINTERFACE_H__321261C2_FEF4_11D2_9C2C_006008911536__INCLUDED_)
