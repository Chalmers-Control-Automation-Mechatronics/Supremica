
#include <jni.h>
#include <stdlib.h>
#include <string.h>


/* --[ DEFINITIONS ] ------------------------------------------------------------------------------------- */

#define NO_EXTERNAL_EVENT -1

struct event {
	int index;
	int external;
	int initialized;
	int active;
	char *name;
};

/* globals */
struct event *events;
struct event *external_events;
int events_size, external_size;


/* foward refs */
int isExternalEvent(char *name);
void fireCommand(struct event *e);

/* --[ INTERNAL JNI CODE ] ------------------------------------------------------------------------------------- */


JNIEXPORT void JNICALL Java_org_supremica_gui_simulator_ExternalEventExecuter_native_1initialize(JNIEnv *env, jobject this_, jint size)
 {
	  int i;
	  events_size = size;
	  external_size = 0;
	  events = (struct event *) malloc( sizeof(struct event) * events_size);
	  external_events = (struct event *) malloc( sizeof(struct event) * events_size);

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
  }


JNIEXPORT jboolean JNICALL Java_org_supremica_gui_simulator_ExternalEventExecuter_native_1register_1event(JNIEnv *env_, jobject this_, jint index, jstring name)
 {
	  if(index >= 0 && index < events_size) {
		const char *str = (*env_)->GetStringUTFChars(env_, name, 0);
		events[index].name = strdup(str);
		events[index].index = index;
		events[index].initialized = 1;
		events[index].external = isExternalEvent(events[index].name);
		(*env_)->ReleaseStringUTFChars(env_, name, str);
		if(events[index].external )
		{
			external_events[external_size] = events[index];
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
	int event = checkExternalEvents();


	if(event != NO_EXTERNAL_EVENT)
	{
		if(event >= 0 && event < events_size)
		{

			if(events[event].external)
			{
				printf("Fireing EXTERNAL '%s'\n", events[event].external); // DEBUG
				cls = (*env_)->GetObjectClass(env_, this_);
				    mid = (*env_)->GetMethodID(env_, cls, "from_native_fire", "(I)V");
				    if(mid != 0) {
						(*env_)->CallVoidMethod(env_, this_, mid, event);
					} else printf("INTERNAL: GetMethodID failed!\n");
			} else printf("fired external event is NOT external: %s\n", events[event].name);

		} else printf("BAD external events fired: %d\n", event);
	}
}

/* --[ USER CODE STARTS HERE ] -------------------------------------------------------------------------- */


/*
 * an external event is initiated by the MODEL (example: sensor event).
 * an internal event is initiated by SUPREMICA (example: command event).
 *
 * return 1 if this is a sensor event, 0 otherwise (i.e. it's a command-event)
 */
int isExternalEvent(char *name) {
	// just do it random :)
	return (rand() & 1);
}

/*
 * flow: SUPREMICA --> MODEL
 * an event must be fired, this is probably a "command" comming from supremica
 * forward it to the model (robot?)
 */
void fireCommand(struct event *e)
{
	// no model
	printf("Fireing INTERNAL '%s'\n", e->name);
}

/*
 * flow: MODEL --> SUPREMICA
 * check wich events have been fired by the model. probably "sensor" events
 * return _one_ active events OR return NO_EXTERNAL_EVENT if there are no pending events
 */
int  checkExternalEvents()
{
	if(external_size == 0) return NO_EXTERNAL_EVENT;
	return external_events[rand() % external_size].index;
}