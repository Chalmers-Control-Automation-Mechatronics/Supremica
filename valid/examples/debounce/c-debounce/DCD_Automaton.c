/*****************************************************************************
** Automaton baseclass for DES-to-C-Generator, Revision 2.6
** (C) Siemens AG, ZT SE 4, 1999. All Rights Reserved.
** Authors: Dr. Markus Kaltenbach, Dr. Robi Malik
*****************************************************************************/

#ifdef __GNUG__
#pragma implementation
#endif

#include "DCD_Automaton.h"

#include <assert.h>
#include <malloc.h>
#include <stdio.h>
#include <string.h>

#ifdef DES_SIMULATOR
extern void setEvent(char* eventName);
#endif


/*****************************************************************************
** CLASS DCD_SignalledEvent
*****************************************************************************/

void DCD_SignalledEvent_create
  (DCD_SignalledEvent* event,
   unsigned short evFamily,
   unsigned short parameter)
{
  event->mEventFamily = evFamily;
  event->mParameter = parameter;
  event->mNext = 0;
}


#ifdef DES_ONLY_ONE_EVENT

int DCD_SignalledEvent_isEqual
  (DCD_SignalledEvent* lhs, DCD_SignalledEvent* rhs)
{
	return (lhs->mEventFamily == rhs->mEventFamily) &&
           (lhs->mParameter == rhs->mParameter);
}

#endif  /* DES_ONLY_ONE_EVENT */


/*****************************************************************************
** CLASS DCD_EventQueue
******************************************************************************
** Single linked list of DCD_SignalledEvent objects
*****************************************************************************/

void DCD_EventQueue_create(DCD_EventQueue* queue)
{
  queue->mFirst = queue->mLast = 0;
}


void DCD_EventQueue_destroy(DCD_EventQueue* queue)
{
  DCD_EventQueue_clear(queue);
}


int DCD_EventQueue_isEmpty(DCD_EventQueue* queue)
{
  return queue->mFirst == 0;
}


DCD_SignalledEvent* DCD_EventQueue_getFirst(DCD_EventQueue* queue)
{
  return queue->mFirst;
}


DCD_SignalledEvent* DCD_EventQueue_getLast(DCD_EventQueue* queue)
{
  return queue->mLast;
}


DCD_SignalledEvent* DCD_EventQueue_getNext
  (DCD_EventQueue* queue, DCD_SignalledEvent* pred)
{
  assert(pred); 
  return pred->mNext;
}


/* Add at the top of the list, use with lifo. */

void DCD_EventQueue_push
  (DCD_EventQueue* queue, DCD_SignalledEvent* event)
{
  assert(event);
  assert(event->mNext == 0);
  if (queue->mFirst) {	/* queue not empty */
    event->mNext = queue->mFirst;
    queue->mFirst = event;
  } else {	/* queue empty */
    assert(queue->mLast == 0);
    queue->mLast = queue->mFirst = event;
  }
}


/* Add at end of the list, use with fifo. */

void DCD_EventQueue_append
  (DCD_EventQueue* queue, DCD_SignalledEvent* event)
{
  assert(event);
  assert(event->mNext == 0);
  if (queue->mFirst) {
	assert(queue->mLast->mNext == 0);
	queue->mLast->mNext = event;
	queue->mLast = event;
  } else {
	assert(queue->mLast == 0);
	queue->mFirst = queue->mLast = event;
  }
}


/* Clear list. */

void DCD_EventQueue_clear(DCD_EventQueue* queue)
{
  while(!DCD_EventQueue_isEmpty(queue)) {
    free(DCD_EventQueue_pop(queue));
  }
}


/* Get first element and remove it from list; returns 0 if list empty. */

DCD_SignalledEvent* DCD_EventQueue_pop(DCD_EventQueue* queue)
{
  if (queue->mFirst) {	/* Queue not empty */
	DCD_SignalledEvent* ret = queue->mFirst;
	queue->mFirst = queue->mFirst->mNext;
	if (queue->mFirst == 0){
      queue->mLast = 0;
	}
	return ret;
  }
  return 0;
}


/* Remove event after the event given as parameter. Return true on success. */

int DCD_EventQueue_removeAfter
  (DCD_EventQueue* queue, DCD_SignalledEvent* event)
{
  /* Cannot remove mFirst for empty queue */
  assert(event);
  if (event->mNext) {	/* There is a next element. */
	event->mNext = event->mNext->mNext;
    if (event->mNext == 0) {  /* removed last element */
	  queue->mLast = event;
    }
	return 1;
  }
  return 0;	/* Nothing done. */
}


/*****************************************************************************
** CLASS DCD_Automaton
******************************************************************************
** This is a default base class for all DES automata with fuctional interface
** for instance polling or message based for uncontrollables
*****************************************************************************/

/* DCD_Automaton: General object handling *******************************/

void DCD_Automaton_create(DCD_Automaton* automaton)	
{
  DCD_EventQueue_create(&automaton->mUncontrollableEventQueue);
#ifdef WITHTRACE
  automaton->mTraceFunc = 0;
#endif
}


void DCD_Automaton_destroy(DCD_Automaton* automaton)	
{
  DCD_EventQueue_destroy(&automaton->mUncontrollableEventQueue);
}


void DCD_Automaton_init(DCD_Automaton* automaton)
{
  DCD_EventQueue_clear(&automaton->mUncontrollableEventQueue);
}


/* DCD_Automaton: Standard methods **************************************/

void DCD_Automaton_signalEvent
  (DCD_Automaton* automaton, DCD_SignalledEvent* newEvent)
{
#ifdef DES_ONLY_ONE_EVENT
  DCD_SignalledEvent* event =
    DCD_EventQueue_getFirst(&automaton->mUncontrollableEventQueue);
  while (event && !DCD_SignalledEvent_isEqual(event, newEvent)) {
	event = DCD_EventQueue_getNext
      (&automaton->mUncontrollableEventQueue, event);
  }
  if (event) {
#ifdef WITHTRACE
    DCD_EventQueue_debugWarning(automaton, event, " already signalled!");
#endif  /* WITHTRACE */
	free(newEvent);
  } else {
    newEvent->mNext = 0;
	DCD_EventQueue_append(&automaton->mUncontrollableEventQueue, newEvent);
  }
#else  /* !DES_ONLY_ONE_EVENT */
  newEvent->mNext = 0;
  DCD_EventQueue_append(&automaton->mUncontrollableEventQueue, newEvent);
#endif  /* !DES_ONLY_ONE_EVENT */
}


DCD_SignalledEvent* DCD_Automaton_getNextUncontrollable
  (DCD_Automaton* automaton)
{
  if (DCD_EventQueue_isEmpty(&automaton->mUncontrollableEventQueue)) {
    return 0;
  } else {
    return DCD_EventQueue_pop(&automaton->mUncontrollableEventQueue);
  }
}


/* DCD_Automaton: Tracing ***********************************************/

#if defined(WITHTRACE) || defined(DES_SIMULATOR)

#if defined(WITHTRACE)
void DCD_Automaton_setTraceFunction
  (DCD_Automaton* automaton, DCD_TraceFunction* f)
{
  automaton->mTraceFunc = f;
}
#endif  /* WITHTRACE */


#if defined(WITHTRACE)
void DCD_Automaton_printTrace(DCD_Automaton* automaton, char* line)
{
  (*automaton->mTraceFunc)(line);
}
#endif  /* WITHTRACE */


void DCD_Automaton_debugAction
  (DCD_Automaton* automaton, char* eventName)
{
#ifdef WITHTRACE
  int len = strlen(eventName);
  char* line = malloc((len+15) * sizeof(char));
  sprintf(line, "EXECUTING: %s\n", eventName);
  DCD_Automaton_printTrace(automaton, line);
  free(line);
#endif  /* WITHTRACE */
#ifdef DES_SIMULATOR
  setEvent(eventName);
#endif  /* DES_SIMULATOR */
}


void DCD_Automaton_debugSignal
  (DCD_Automaton* automaton, char* eventName)
{
#ifdef WITHTRACE
  int len = strlen(eventName);
  char* line = malloc((len+15) * sizeof(char));
  sprintf(line, "GOT EVENT: %s\n", eventName);
  DCD_Automaton_printTrace(automaton, line);
  free(line);
#endif  /* WITHTRACE */
}


void DCD_Automaton_debugWarning
  (DCD_Automaton* automaton, char* eventName, char* text)
{
#ifdef WITHTRACE
  int len = strlen(eventName) + strlen(text);
  char* line = malloc((len+35) * sizeof(char));
  sprintf(line, "WARNING: uncontrollable %s %s\n", eventName, text);
  DCD_Automaton_printTrace(automaton, line);
  free(line);
#endif  /* WITHTRACE */
}

#endif  /* WITHTRACE || DES_SIMULATOR */

