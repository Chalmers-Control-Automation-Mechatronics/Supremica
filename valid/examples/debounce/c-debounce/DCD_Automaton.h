/*****************************************************************************
** Automaton baseclass for DES-to-C-Generator, Revision 2.6
** (C) Siemens AG, ZT SE 4, 1999. All Rights Reserved.
** Authors: Dr. Markus Kaltenbach, Dr. Robi Malik
*****************************************************************************/

#ifndef _DCD_AUTOMATON_H_
#define _DCD_AUTOMATON_H_

#if _MSC_VER >= 1000
#pragma once
#endif

#ifdef __GNUG__
#pragma interface
#endif


/*****************************************************************************
** TYPEDEF DCD_TraceFunction
*****************************************************************************/

typedef void DCD_TraceFunction(char* line);


/*****************************************************************************
** CLASS DCD_SignalledEvent
*****************************************************************************/

typedef struct DCD_SignalledEvent
{
  /* Points to the existing event family definition. */
  unsigned short mEventFamily;
  /* Points to the parameter. */
  unsigned short mParameter;
  /* For single linked list. */
  struct DCD_SignalledEvent* mNext;
} DCD_SignalledEvent;

extern void DCD_SignalledEvent_create
  (DCD_SignalledEvent* event,
   unsigned short evFamily,
   unsigned short parameter);

#ifdef DES_ONLY_ONE_EVENT
extern int DCD_SignalledEvent_isEqual
  (DCD_SignalledEvent* lhs,
   DCD_SignalledEvent* rhs);
#endif


/*****************************************************************************
** CLASS DCD_EventQueue
******************************************************************************
** Single linked list of DCD_SignalledEvent objects
*****************************************************************************/

typedef struct DCD_EventQueue
{
  DCD_SignalledEvent* mFirst;
  DCD_SignalledEvent* mLast;
} DCD_EventQueue;


extern void DCD_EventQueue_create(DCD_EventQueue* queue);
extern void DCD_EventQueue_destroy(DCD_EventQueue* queue);
	
/* List is empty or not? */
extern int DCD_EventQueue_isEmpty(DCD_EventQueue* queue);
/* Get first element */
extern DCD_SignalledEvent* DCD_EventQueue_getFirst
  (DCD_EventQueue* queue);
/* Get last element */
extern DCD_SignalledEvent* DCD_EventQueue_getLast
  (DCD_EventQueue* queue);
/* Get the successor of the given event */
extern DCD_SignalledEvent* DCD_EventQueue_getNext
  (DCD_EventQueue* queue, DCD_SignalledEvent* pred);

/* Add at the top of the list, use with lifo. */
extern void DCD_EventQueue_push
  (DCD_EventQueue* queue, DCD_SignalledEvent* event);
/* Add at end of the list, use with fifo. */
extern void DCD_EventQueue_append
  (DCD_EventQueue* queue, DCD_SignalledEvent* event);

/* Clear list, deletes the list members. */
extern void DCD_EventQueue_clear(DCD_EventQueue* queue);

/* Get first element and remove it from list. */
extern DCD_SignalledEvent* DCD_EventQueue_pop
  (DCD_EventQueue* queue);
/* Remove event after "event" from the list; fast! */
extern int DCD_EventQueue_removeAfter
  (DCD_EventQueue* queue, DCD_SignalledEvent* event);




/*****************************************************************************
** CLASS DCD_Automaton
******************************************************************************
** This is a default base class for all DES automata with fuctional interface
** for instance polling or message based for uncontrollables
*****************************************************************************/

typedef struct DCD_Automaton 
{
  DCD_EventQueue mUncontrollableEventQueue;
#ifdef WITHTRACE
  DCD_TraceFunction* mTraceFunc;
#endif  /* WITHTRACE */
} DCD_Automaton;


/* General object handling */
extern void DCD_Automaton_create(DCD_Automaton* automaton);
extern void DCD_Automaton_destroy(DCD_Automaton* automaton);

/* Initialize the queue (empty it). Can be called anytime. */
extern void DCD_Automaton_init(DCD_Automaton* automaton);

#if defined(WITHTRACE) 
/* Set the trace stream. */
extern void DCD_Automaton_setTraceFunction
  (DCD_Automaton* automaton, DCD_TraceFunction* f);
#endif  /* WITHTRACE */

/* Queue functions for uncontrollable events, creates a new event. */
extern void DCD_Automaton_signalEvent
  (DCD_Automaton* automaton, DCD_SignalledEvent* newEvent);
extern DCD_SignalledEvent* DCD_Automaton_getNextUncontrollable
  (DCD_Automaton* automaton);

#if defined(WITHTRACE) || defined (DES_SIMULATOR)
extern void DCD_Automaton_debugAction
  (DCD_Automaton* automaton, char* eventName);
extern void DCD_Automaton_debugSignal
  (DCD_Automaton* automaton, char* eventName);
extern void DCD_Automaton_debugWarning
  (DCD_Automaton* automaton, char* eventName, char* text);
#endif  /* WITHTRACE || DES_SIMULATOR */

#if defined(WITHTRACE)
/* Stuff for tracing */
void DCD_Automaton_printTrace(DCD_Automaton* automaton, char* line);
#endif  /* WITHTRACE */


#endif  /* !_DCD_AUTOMATON_H_ */
