//Thread class, designed by Kirchner, added by witte
//(c)Siemens AG 1999
//thread termination inspired by JAVA threads
//instrumented for Doc++ Documentation (like JavaDoc)

#ifndef _THREAD_H_
#define _THREAD_H_

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
	
//=================================class DesThread======================================

class DesThread {
	//definition of certain internal Events and Priorities
public:
#ifdef _WIN32

	enum PRIORITY_CLASS {
		IDLE = IDLE_PRIORITY_CLASS,
		NORMAL = NORMAL_PRIORITY_CLASS,
		HIGH = HIGH_PRIORITY_CLASS,
		REALTIME = REALTIME_PRIORITY_CLASS
	};
	enum REL_PRIORITY {
		REL_LOWEST = THREAD_PRIORITY_LOWEST,
		REL_BELOW_NORMAL = THREAD_PRIORITY_BELOW_NORMAL,	
		REL_NORMAL = THREAD_PRIORITY_NORMAL,	
		REL_ABOVE_NORMAL = THREAD_PRIORITY_ABOVE_NORMAL,	
		REL_HIGHEST = THREAD_PRIORITY_HIGHEST,
		REL_IDLE = THREAD_PRIORITY_IDLE,
		REL_CRITICAL = THREAD_PRIORITY_TIME_CRITICAL
	};
#else	//on posix threads; priority setting not completely implemented
		//probably these definitions dont make sense
	enum PRIORITY_CLASS {
		IDLE,
		NORMAL,
		HIGH,
		REALTIME
	};
	enum REL_PRIORITY {
		REL_LOWEST,
		REL_BELOW_NORMAL,	
		REL_NORMAL,	
		REL_ABOVE_NORMAL,	
		REL_HIGHEST,
		REL_IDLE,
		REL_CRITICAL
	};
#endif
public:
	///Constructor
	DesThread();

	///~DesThread() waits until thread is terminated
	virtual ~DesThread();

	//======================================================================================
	/**@name Thread start, stop and exit functions*/
	//@{
	///use from outside, e.g. in destructors from derived classes, to terminate the thread
	///Waits until thread is terminated.
	void waitForThreadTerminated();

	///has thread Function ended ?
	bool threadEnded();

	///is thread scheduled for termination (by terminateThreadSmoothly()) ?
	bool threadScheduledForTermination(){return mTerminateThread;}

	///status, STILL_ACTIVE or return status of the thread exit
	DWORD  getStatus();

	/**Suspends the thread if !threadEnded()
	 *If the function succeeds under windows, the return value is the thread’s previous suspend count;
	 *otherwise, it is 0xFFFFFFFF
	 *On posix, only inside the thread controlled by this class suspension is possible,
	 *but no suspension from outside
	*/
	DWORD  suspend();

	/**Resumes the thread if !threadEnded()
	 *If the function succeeds under windows, the return value is the thread’s previous suspend count;
	 *otherwise, it is 0xFFFFFFFF
	*/
	DWORD  resume();

	///called from outside to terminate the thread on thread Exit points
	bool terminateThreadSmoothly();

	///terminate thread at once; use carefully and only on emergency cases!
	bool terminateThreadAtOnce();

	///defines a thread exit point if the thread is mCurrentThread
	void threadExitPoint();
	//@}
	//===============================================================================
	/**@name Priority Functions, not implemented under posix*/
	//@{
	///
	BOOL   setPriorityClass(PRIORITY_CLASS priority);
	///
	PRIORITY_CLASS getPriorityClass();
	///
	REL_PRIORITY getPriority();
	///
	BOOL   setPriority(REL_PRIORITY priority);
	///
	BOOL   raisePriority();
	///
	BOOL   lowerPriority();
	//@}

	HANDLE getHandle();
protected:
	//called by ThreadEntryFunc
	virtual DWORD WINAPI ThreadFunc() = 0;

private:
	//used to protect deletion of the class while thread still running 
	CRITICAL_SECTION mProtectRunningThread;
	//protect any of the thread interface functions like Suspend, Resume etc...
	CRITICAL_SECTION mProtectThreadFunctions;
	//true iff thread is scheduled for termination
	bool mTerminateThread;
	//true iff thread is suspended
	bool mThreadSuspended;
#ifdef _WIN32	//WINDOW==================
	//Thread entry function as used used by Win API ::CreateThread; deprivated by MS
	//static DWORD WINAPI ThreadEntryFunc(LPVOID lpvThreadParm);	

	//Thread entry function as used used by Win API ::_beginThreadEx
	//unfortunately, ::Suspend() may block in C Runtime lib
	static unsigned WINAPI ThreadEntryFunc(LPVOID lpvThreadParm);	

	//handle of the thread controlled by this class
	HANDLE cThreadHandle;

	//Systemwide unique ID of the thread controlled by this class
	unsigned int  cThreadID;

	//EventHandle for implementation of suspend()
	HANDLE mSuspendEvent;

#else	//posix	==============================
	//Thread entry function as used used by ::pthread_create
	static void* ThreadEntryFunc(LPVOID lpvThreadParm);

	//Systemwide unique ID of the thread controlled by this class
	pthread_t cThreadID;

	//used for suspend / resume, which does not exist on posix
	pthread_cond_t mCvp;	
	pthread_mutex_t mSuspendMutex;
	bool mSuspendCond;

	//true if thread has ended
	bool mThreadEnded;
#endif
	REL_PRIORITY cPriority;
	PRIORITY_CLASS cPriorityClass;
};

#endif
