// DesCode_AutomatonThread.cpp: implementation of the DesCode_AutomatonThread class.
// (c) Martin Witte, Siemens AG 5/1999
//////////////////////////////////////////////////////////////////////

//example of a threadsafe printing function " void printTrace(const char* line) "
//for classes DesCode_Automaton or DesCode_AutomatonThreaded
//uses a class DesCode_Tracing to be able to Initialize and Delete the CRITICAL_SECTION
//automatically.

#include "DesCode_PrintTrace.h"
#include <iostream>

DesCode_Tracing::DesCode_Tracing()
{
	InitializeCriticalSection(&mTraceLock);
}

DesCode_Tracing::~DesCode_Tracing()
{
	DeleteCriticalSection(&mTraceLock);
}


void DesCode_Tracing::lock()
{
	EnterCriticalSection(&mTraceLock);
}

void DesCode_Tracing::unlock()
{
	LeaveCriticalSection(&mTraceLock);
}

void DesCode_Tracing::printTrace(const char* line)
{
	lock();
	std::cerr << line << std::flush;
	unlock();
}

void printTrace(const char* line)
{
	static DesCode_Tracing tracer;	
		//calls constructor at first call of "printTrace" and inits CRITICAL_SECTION
	tracer.printTrace(line);
}
