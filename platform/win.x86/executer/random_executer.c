
#include <jni.h>
#include <stdlib.h>
#include <string.h>


/* --[ DEFINITIONS ] ------------------------------------------------------------------------------------- */

#define NO_EXTERNAL_EVENT -1
#define NO_INTERNAL_EVENT NO_EXTERNAL_EVENT

struct event {
	int index; // index for Supremica
	int external_index; // index for the outside
	int external;
	int initialized;
	int active;
	char *name;
};

/* globals */
struct event *events;
struct event **external_events;

int events_size, external_size;


/* foward refs */
int isExternalEvent(struct event *e);
void fireCommand(struct event *e);



/* --[ INTERNAL JNI CODE ] ------------------------------------------------------------------------------------- */


JNIEXPORT void JNICALL Java_org_supremica_gui_simulator_ExternalEventExecuter_native_1initialize(JNIEnv *env, jobject this_, jint size)
 {
	  int i;
	  events_size = size;
	  external_size = 0;
	  events = (struct event *) malloc( sizeof(struct event) * events_size);
	  external_events = (struct event **) malloc( sizeof(struct event *) * events_size);

	  for(i = 0; i < events_size; i++)
	  {
		  events[i].initialized = 0;
		  events[i].name= 0;

	  }

  }

JNIEXPORT void JNICALL Java_org_supremica_gui_simulator_ExternalEventExecuter_native_1cleanup(JNIEnv *env, jobject this_)
 {
	  int i;
	  for(i = 0; i < events_size; i++)
	  {
		if(events[i].initialized != 0)
		{
			free(events[i].name);
		}
	  }

	  free(events);
	  free(external_events);
  }


JNIEXPORT jboolean JNICALL Java_org_supremica_gui_simulator_ExternalEventExecuter_native_1register_1event(JNIEnv *env_, jobject this_, jint index, jstring name)
 {
	  if(index >= 0 && index < events_size) {
		const char *str = (*env_)->GetStringUTFChars(env_, name, 0);

		events[index].name = strdup(str);
		events[index].index = index;
		events[index].initialized = 1;
		events[index].external_index = NO_EXTERNAL_EVENT;
		events[index].external = isExternalEvent(events +index);
		(*env_)->ReleaseStringUTFChars(env_, name, str);

		if(events[index].external )
		{
			external_events[external_size] = events + index;
			external_size++;
			return JNI_TRUE;
		} else {
			return JNI_FALSE;
		}
	  } // else make some noise!

	return  JNI_FALSE; // shouldn't get here if everything works
 }

JNIEXPORT void JNICALL Java_org_supremica_gui_simulator_ExternalEventExecuter_native_1fire(JNIEnv * env_, jobject this_, jint index)
{
	if(index >= 0 && index < events_size) {
		fireCommand(events +index);
	}
}


JNIEXPORT void JNICALL Java_org_supremica_gui_simulator_ExternalEventExecuter_native_1check_1external_1events
	(JNIEnv *env_, jobject this_)
{

	jclass cls;
	jmethodID mid;
	int event;


	for(;;) {
		event = checkExternalEvents();

		if(event != NO_EXTERNAL_EVENT)
		{
			if(event >= 0 && event < events_size)
			{
				if(events[event].external)
				{
					printf("Fireing EXTERNAL '%s'\n", events[event].name); // DEBUG

					cls = (*env_)->GetObjectClass(env_, this_);

					if(cls) {
						mid = (*env_)->GetMethodID(env_, cls, "from_native_fire", "(I)V");
						if(mid != 0) {
							(*env_)->CallVoidMethod(env_, this_, mid, event);
						} else printf("INTERNAL: GetMethodID failed!\n");
					} else printf("INTERNAL: GetObjectClass failed!\n");
				} else printf("fired external event is NOT external: %s\n", events[event].name);

			} else printf("BAD external events fired: %d\n", event);
		}
		else
		{
			return;
		}
	}
}



/* --[ UTILITY FUNCTIONS] -------------------------------------------------------------------------- */


/*
 * given the internal (supremica side) event index (ID), give the internal one
 * return NO_EXTERNAL_EVENT if event not found (or registred)
 */

int getExternalIndexFromInternal(int supremica_index)
{
	if(supremica_index >= 0 && supremica_index < events_size)
	{
		return events[supremica_index].external_index;
	} else printf("BAD supremica_index: %d\n", supremica_index);
	return NO_EXTERNAL_EVENT;
}


/*
 * given the external (plant side) event index (ID), give the internal one (supremica side)
 * returns NO_INTERNAL_EVENT if no internal event with such id exists
 *
 */

int getInternalIndexFromExternal(int external_index)
{
	int i;
	for(i = 0; i < events_size; i++)
	{
		if(events[i].external_index == external_index)
			return events[i].index;
	}
	 return NO_INTERNAL_EVENT;
}
/* --[ USER CODE STARTS HERE ] -------------------------------------------------------------------------- */


/*
 * an external event is initiated by the MODEL (example: sensor event).
 * an internal event is initiated by SUPREMICA (example: command event).
 *
 * we whould also assign the external_index here
 * return 1 if this is a sensor event, 0 otherwise (i.e. it's a command-event)
 */
int isExternalEvent(struct event *e) {

	/* example:
	if( !strcmp(ne->name,"some-event")) {
		ne->external_index = 25; // on the plant side, we call this event 25
		return 1;                // yes, this is an external event
	}
	*/

	// just do it random :)
	int random_bool = (rand() & 1);
	if(random_bool)
	{
		printf("Registring '%s' as external event\n", e->name);
		return 1;
	}
	else
	{
		return 0;
	}

}

/*
 * flow: SUPREMICA --> MODEL
 * an event must be fired, this is probably a "command" comming from supremica
 * forward it to the model (robot?)
 */
void fireCommand(struct event *e)
{
	// debug:
	printf("Fireing INTERNAL '%s'\n", e->name);

	// now send e->external_index via the radio link to the robots...

}

/*
 * flow: MODEL --> SUPREMICA
 * check wich events have been fired by the model. probably "sensor" events
 * return _one_ active events OR return NO_EXTERNAL_EVENT if there are no pending events
 */
int  checkExternalEvents()
{

	// example:
	// int external_event = get_event_from_radio_link()
	// if(external_event is OK)
	//		return getInternalIndexFromExternal(external_event);
	//  else
	//		return NO_EXTERNAL_EVENT;


	// example random event generation
	int random_event;
	if(external_size == 0) return NO_EXTERNAL_EVENT; // no events to fire?


	// only 10% of times
	if( (rand() % 10) != 0) return NO_EXTERNAL_EVENT;

	random_event = rand() % external_size;
	printf("Generating random EXTERNAL event '%s'\n",  external_events[random_event]->name);
	return external_events[random_event]->index;
}