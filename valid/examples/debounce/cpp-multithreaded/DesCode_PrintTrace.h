// DesCode_AutomatonThread.cpp: implementation of the DesCode_AutomatonThread class.
// (c) Martin Witte, Siemens AG 5/1999
//////////////////////////////////////////////////////////////////////

//example of a threadsafe printing function " void printTrace(const char* line) "
//for classes DesCode_Automaton or DesCode_AutomatonThreaded
//uses a class DesCode_Tracing to be able to Initialize and Delete the CRITICAL_SECTION
//automatically.

#ifndef _DESCODE_PRINTTRACE_
#define _DESCODE_PRINTTRACE_

# if  _MSC_VER >= 1000				// VC 5.0 or later ?
# pragma once
# endif  // _MSC_VER >= 1000

#ifdef _WIN32
	#ifdef _AFXDLL
		#include <afx.h>
	#else
		#include <windows.h>
	#endif
#else	//posix threads
	#include "win32api.h"	//includes certain win32 api constructs impl. on posix
#endif

extern void printTrace(const char* line);

class DesCode_Tracing
{
public:
	DesCode_Tracing();
	~DesCode_Tracing();
	void printTrace(const char* line);
	void lock();
	void unlock();
private:
	CRITICAL_SECTION mTraceLock;
};

#endif