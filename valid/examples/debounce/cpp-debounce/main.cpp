
#include <iostream.h>

#include "app_debounce.h"


static void trace(const char* line)
{
  cout << line << flush;
}


int main(int argc, char** argv)
{
  DesCode_Actions_debounce application;
  application.setTraceFunction(trace);
  application.execute();
  return 0;
}