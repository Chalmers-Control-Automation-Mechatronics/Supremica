#include <dlfcn.h>   
#include "AdlinkPCI7432.h"
#include "dask.h"



void* lib_handle;
const char* error_msg;


I16 (*dl_Register_Card)(U16 CardType, U16 card_num);
I16 (*dl_Release_Card)(U16 CardNumber);
I16 (*dl_DI_ReadPort)(U16 CardNumber, U16 Port, U32 *Value);
I16 (*dl_DO_WritePort)(U16 CardNumber, U16 Port, U32 Value);

/*
 * Class:     AdlinkPCI7432
 * Method:    RegisterCard
 * Signature: (SS)S
 */
JNIEXPORT jshort JNICALL Java_org_supremica_softplc_Drivers_AdlinkPCI7432_RegisterCard
  (JNIEnv *env, jclass cls, jshort cardid, jshort cardnr)
{
  lib_handle = dlopen("/usr/lib/libpci_dask.so", RTLD_LAZY);  
  if (!lib_handle)
  {
    fprintf(stderr, "Error during dlopen(): %s\n", dlerror());
    // exit(1);
  }
  
  dl_Register_Card = dlsym(lib_handle, "Register_Card");
  error_msg = dlerror();
  if (error_msg)
  {
    fprintf(stderr, "Error locating 'Register_Card' - %s\n", error_msg);
    // exit(1);
  }

  dl_Release_Card = dlsym(lib_handle, "Release_Card");
  error_msg = dlerror();
  if (error_msg)
  {
    fprintf(stderr, "Error locating 'Release_Card' - %s\n", error_msg);
    // exit(1);
  }

  dl_DI_ReadPort = dlsym(lib_handle, "DI_ReadPort");
  error_msg = dlerror();
  if (error_msg)
  {
    fprintf(stderr, "Error locating 'DI_ReadPort' - %s\n", error_msg);
    //exit(1);
  }
  
  dl_DO_WritePort = dlsym(lib_handle, "DO_WritePort");
  error_msg = dlerror();
  if (error_msg)
  {
    fprintf(stderr, "Error locating 'Release_Card' - %s\n", error_msg);
    //exit(1);
  }
  
  
  return (*dl_Register_Card)(cardid, cardnr);
}

/*
 * Class:     AdlinkPCI7432
 * Method:    ReleaseCard
 * Signature: (S)V
 */
JNIEXPORT void JNICALL Java_org_supremica_softplc_Drivers_AdlinkPCI7432_ReleaseCard
  (JNIEnv *env, jclass cls, jshort card)
{
  (*dl_Release_Card)(card);

  if (lib_handle)
  {
    dlclose(lib_handle); 
  }
}

/*
 * Class:     AdlinkPCI7432
 * Method:    WritePort
 * Signature: (SSI)V
 */
JNIEXPORT void JNICALL Java_org_supremica_softplc_Drivers_AdlinkPCI7432_WritePort
  (JNIEnv *env, jclass cls, jshort card, jshort channel, jint value)
{
  (*dl_DO_WritePort)(card, channel, value);
}

/*
 * Class:     AdlinkPCI7432
 * Method:    ReadPort
 * Signature: (SS)I
 */
JNIEXPORT jint JNICALL Java_org_supremica_softplc_Drivers_AdlinkPCI7432_ReadPort
  (JNIEnv *env, jclass cls, jshort card, jshort channel)
{
  U32 value;
  (*dl_DI_ReadPort)(card, channel, &value);
  return value;
}


