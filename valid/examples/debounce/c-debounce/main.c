
#include <stdio.h>

#include "DCD_Project_debounce.h"


static void trace(char* line)
{
  fprintf(stdout, line);
  fflush(stdout);
}


int main(int argc, char** argv)
{
  DCD_Project_debounce application;
  DCD_Project_debounce_create(&application);
#if defined(WITHTRACE) || defined(DES_SIMULATOR)
  DCD_Project_debounce_setTraceFunction(&application, trace);
#endif  /* WITHTRACE || DES_SIMULATOR */
  DCD_Project_debounce_execute(&application);
  DCD_Project_debounce_destroy(&application);
  return 0;
}