//Thread class on windows or posix
//Partly from Gerald Kirchner, PL EA 1
//modified by Martin Witte, 1/99
//(c) Siemens AG 1999
	
#ifdef _WIN32		
	#include <process.h>
#else	//posix
#endif
#include <iostream>
#include <assert.h>
#include "DesThread.h"

DesThread::DesThread() 
:	mTerminateThread(false),
#ifdef _WIN32
	mThreadSuspended(true),
	cThreadHandle(0),
	cThreadID(0),
	mSuspendEvent(0)
#else
	mThreadSuspended(false),
	cThreadID(0),
	mSuspendCond(false),
	mThreadEnded(false)
#endif
	
{
	InitializeCriticalSection(&mProtectRunningThread);
	InitializeCriticalSection(&mProtectThreadFunctions);
#ifdef _WIN32
	//first create an Event with non-manual reset attribute 
	mSuspendEvent = CreateEvent(0,FALSE,FALSE,0);
	unsigned retCode=_beginthreadex( NULL, 0, DesThread::ThreadEntryFunc,
									reinterpret_cast<LPVOID>(this),
									0, &cThreadID );
	if(retCode==0){
		std::cerr << "Thread::Thread(): could not create thread" << std::endl;
		LPVOID lpMsgBuf;
		FormatMessage( 
			FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
			NULL,
			GetLastError(),
			MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
			(LPTSTR) &lpMsgBuf,
			0,NULL );
		// Display the string.
		MessageBox( NULL,(const char*) lpMsgBuf, "GetLastError", MB_OK|MB_ICONINFORMATION );
		// Free the buffer.
		LocalFree( lpMsgBuf );
		assert(0);
		exit(2);
	}
	else {
		cThreadHandle = reinterpret_cast<HANDLE>(retCode);
	}
#else	//posix
	pthread_cond_init(&mCvp,NULL);
	pthread_mutex_init(&mSuspendMutex,NULL);
	int retCode = pthread_create(&cThreadID,NULL,DesThread::ThreadEntryFunc,
								reinterpret_cast<void*>(this));
	if(retCode!=0){
		std::cerr << "Thread::Thread(): could not create thread" << std::endl;
		assert(0);
		exit(retCode);
	}
#endif
}

DesThread::~DesThread()
{
	waitForThreadTerminated();
	DeleteCriticalSection(&mProtectRunningThread);
	DeleteCriticalSection(&mProtectThreadFunctions);
	::CloseHandle(cThreadHandle);
	::CloseHandle(mSuspendEvent);
}

void DesThread::waitForThreadTerminated()
{
	if(!threadEnded()){
		terminateThreadSmoothly();
		resume();
		EnterCriticalSection(&mProtectRunningThread);	//wait until thread ended
		LeaveCriticalSection(&mProtectRunningThread);
	}
}

	///thread Function has ended
bool DesThread::threadEnded()
{
#ifdef _WIN32
	if(cThreadHandle)
		return getStatus() != STILL_ACTIVE;
	return false;
#else
	return mThreadEnded;
#endif
}
		
///status, STILL_ACTIVE or return status of the thread exit
DWORD DesThread::getStatus()
{
#ifdef _WIN32
	if(cThreadHandle){	//thread has started
		DWORD exitCode;
		GetExitCodeThread(cThreadHandle, &exitCode);
		return exitCode;
	}
	else	//thread not started yet
		return STILL_ACTIVE+1;
#else
#warning "no getStatus implemented"
	return 0;
#endif
}

///Suspends the thread "cThreadHandle" if !threadEnded()
//If the function succeeds, the return value is the thread’s previous suspend count,
// which should be zero;
// otherwise, it is 0xFFFFFFFF
DWORD DesThread::suspend()
{
	//would be better named yield; dont call from another thread than the own one
	//it stops the calling thread! 
	assert(cThreadID == GetCurrentThreadId());	//only suspension from inside the controlled thread
	EnterCriticalSection(&mProtectThreadFunctions);
	DWORD retValue;
#ifdef _WIN32
	if(!threadEnded()){
		mThreadSuspended = true;
		LeaveCriticalSection(&mProtectThreadFunctions);
		retValue = WaitForSingleObject(mSuspendEvent,INFINITE);
		assert(retValue == WAIT_OBJECT_0);
		//retValue = SuspendThread(cThreadHandle);
		EnterCriticalSection(&mProtectThreadFunctions);
		mThreadSuspended=false;
	}
	else
		retValue = 0;
#else
	if(!threadEnded() && !mThreadSuspended){
		pthread_mutex_lock(&mSuspendMutex);
		mSuspendCond = true;
		mThreadSuspended=true;
		while(mSuspendCond){
			LeaveCriticalSection(&mProtectThreadFunctions);
			pthread_cond_wait(&mCvp,&mSuspendMutex);
			EnterCriticalSection(&mProtectThreadFunctions);
		}
		pthread_mutex_unlock(&mSuspendMutex);
	}
	retValue = 0;
#endif
	LeaveCriticalSection(&mProtectThreadFunctions);
	return retValue;
}

///Resumes the thread if !threadEnded()
DWORD DesThread::resume()
{
	EnterCriticalSection(&mProtectThreadFunctions);
	DWORD retValue;
#ifdef _WIN32
	if(mThreadSuspended && !threadEnded()){
		retValue = SetEvent(mSuspendEvent);
	}
	else {
		retValue = 0;
	}
#else
#pragma "first suspension of thread to be corrected"
	if(mThreadSuspended && !threadEnded()){
		mThreadSuspended=false;
		pthread_mutex_lock(&mSuspendMutex);
		mSuspendCond = false;
		pthread_cond_signal(&mCvp);
		pthread_mutex_unlock(&mSuspendMutex);
	}
	retValue = 0;
#endif
	LeaveCriticalSection(&mProtectThreadFunctions);
	return retValue;
}

///called from outside to terminate the thread on thread Exit points
//return false if thread already terminated
bool DesThread::terminateThreadSmoothly()
{
	bool ret = true;
	EnterCriticalSection(&mProtectThreadFunctions);
#ifdef _WIN32
	if(!threadEnded()){	//thread exists and is still running
		mTerminateThread=true;
	}
	else
		ret=false;
#else
	if( cThreadID && !threadEnded()){
		ret=pthread_cancel(cThreadID);
		mTerminateThread=true;
	}
	else
		ret=false;
#endif
	LeaveCriticalSection(&mProtectThreadFunctions);
	return ret;
}

///terminate thread at once; use carefully!
bool DesThread::terminateThreadAtOnce()
{
	bool ret = true;
	if(GetCurrentThreadId() != cThreadID){
		std::cerr << " try to terminate thread which is not owned by this DesThread Object!" 
			<< std::endl;
		assert(0);
		return false;
	}
	EnterCriticalSection(&mProtectThreadFunctions);
#ifdef _WIN32
	if(cThreadHandle && !threadEnded()){
		//_endthreadex(0);
		::TerminateThread(cThreadHandle,0);
	}
#else
	if( cThreadID && !threadEnded()){
		ret=pthread_cancel(cThreadID);
		cThreadID=0;
		mThreadEnded = true;
	}
#endif
	LeaveCriticalSection(&mProtectThreadFunctions);
	LeaveCriticalSection(&mProtectRunningThread);
	return ret;
}


	///defines a thread exit point if the thread is mCurrentThread
void DesThread::threadExitPoint()
{
	if(mTerminateThread)
		terminateThreadAtOnce();
}

//===========================================Priority Functions===========================

DesThread::REL_PRIORITY DesThread::getPriority()
{
#ifdef _WIN32
	return (DesThread::REL_PRIORITY)GetThreadPriority(cThreadHandle);
#else
	assert(0);
	#warning "no thread getPriority under posix"
	return cPriority;
#endif
}

BOOL DesThread::setPriority(DesThread::REL_PRIORITY priority)
{
#ifdef _WIN32
	return SetThreadPriority(cThreadHandle, priority);
#else
	priority = priority;
	assert(0);
	#warning "no thread setPriority under posix"
	return 0;
#endif
}

DesThread::PRIORITY_CLASS DesThread::getPriorityClass()
{
#ifdef _WIN32
	return (DesThread::PRIORITY_CLASS)::GetPriorityClass(cThreadHandle);
#else
	assert(0);
	#warning "no thread priority class under posix"
	return cPriorityClass;
#endif
}

BOOL DesThread::setPriorityClass(DesThread::PRIORITY_CLASS priority)
{
#ifdef _WIN32
	return ::SetPriorityClass(cThreadHandle, priority);
#else
	assert(0);
	priority = priority;
	#warning "no thread SetPriorityClass under posix"
	return 0;
#endif
}

BOOL DesThread::raisePriority()
{
#ifdef _WIN32
	REL_PRIORITY newP = (REL_PRIORITY)::GetThreadPriority(cThreadHandle);
	switch(newP) {
	case REL_LOWEST:
		newP = REL_BELOW_NORMAL;
		break;
	case REL_BELOW_NORMAL:
		newP = REL_NORMAL;
		break;
	case REL_NORMAL:
		newP = REL_ABOVE_NORMAL;
		break;
	case REL_ABOVE_NORMAL:
		newP = REL_HIGHEST;
		break;
	default:
		break;
	}
	return ::SetThreadPriority(cThreadHandle,newP);
#else
	assert(0);
#pragma "no raisPriority"
	return 0;
#endif
}

BOOL DesThread::lowerPriority()
{
#ifdef _WIN32
	REL_PRIORITY newP = (REL_PRIORITY)::GetThreadPriority(cThreadHandle);
	switch(newP) {
	case REL_LOWEST:
	case REL_BELOW_NORMAL:
		newP = REL_LOWEST;
		break;
	case REL_NORMAL:
		newP = REL_BELOW_NORMAL;
		break;
	case REL_ABOVE_NORMAL:
		newP = REL_NORMAL;
		break;
	case REL_HIGHEST:
		newP = REL_ABOVE_NORMAL;
		break;
	default:
		break;
	}
	return SetThreadPriority(cThreadHandle,newP);
#else
	assert(0);
#pragma "no lowerPriority"
	return 0;
#endif

}

#ifdef _WIN32 
//DWORD WINAPI		//CreateThread
unsigned WINAPI	//_beginthreadex
#else
void*
#endif
DesThread::ThreadEntryFunc(LPVOID lpvThreadParm)
{
	DesThread* thisThread = reinterpret_cast<DesThread*>(lpvThreadParm);
	EnterCriticalSection(&(thisThread->mProtectRunningThread));
#ifdef _WIN32
	thisThread->suspend();
	DWORD returnval = thisThread->ThreadFunc();
#else
	thisThread->Suspend();
	void* returnval = (void*)(thisThread->ThreadFunc());
	thisThread->mThreadEnded=true; 
#endif
	LeaveCriticalSection(&(thisThread->mProtectRunningThread));
	return returnval;
}


HANDLE DesThread::getHandle()
{
#ifdef _WIN32
	return cThreadHandle;
#else
	#warning "no handles under posix"
	return 0;
#endif
}


