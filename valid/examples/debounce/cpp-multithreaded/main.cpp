
#include <iostream.h>

#include "app_debounce.h"
#include "DesCode_AutomatonThread.h"
#include "DesCode_PrintTrace.h"


static DesCode_Tracing gTracer;	


static void trace(const char* line)
{
  gTracer.printTrace(line);
}


int main(int argc, char** argv)
{
  DesCode_AutomatonThread myThread;
  DesCode_Actions_debounce* automaton = new DesCode_Actions_debounce;
  automaton->setTraceFunction(trace);
  myThread.setAutomaton(automaton);
  myThread.start();

  // Loop forever ...
  for(;;) {
    // 1.) Read uncontrollables from keyboard and signal them:
    char buffer[1024];
    gTracer.lock();
    cout << "New input: " << flush;
    cin.getline(buffer, 1024);
    gTracer.unlock();
    if(!cin) {
      trace("<QUIT>\n");
      break;
    }
    automaton->receiveKey(buffer[0]);
    automaton->receiveTick();
  }

  return 0;
}