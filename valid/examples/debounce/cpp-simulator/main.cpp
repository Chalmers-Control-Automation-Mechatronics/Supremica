// This may look like C code, but it really is -*- C++ -*-

//############################################################################
//# Debounce example, as of DESign 2.5
//############################################################################
//# This version shows how to use DESign generated together with the
//# DESign Simulator Library.
//############################################################################
//# The code has to be compiled together with the following libraries,
//# which can be found in <DESignDir>/lib:
//#   tcl_803vc.lib
//#   tk_803vc.lib
//#   turbo_mfc.lib
//#   DesProjectLib.lib
//#   DesSimulatorLib.lib
//# Furthermore, the preprocessor flag DES_SIMULATOR has to be set.
//# The include file DesSimExternals.h is found in <DESignDir>/include.
//############################################################################

#include <iostream.h>
#include <DesSimExternals.h>

#include "app_debounce.h"

// Path names are relative to the directory the applications is started in.
// You may have to adjust absolute path names according to your need.

const char* kDebounce_Path = ".";
const char* kDebounce_ProjectFile = "debounce.dpr";
const char* kDebounce_TclFile =
  "D:\\src\\deswin\\bin\\SimulatorScripts.tcl";
const char* kDebounce_GraphEditorCommand = 
  "D:\\src\\deswin\\bin\\DESGraphEditorRM.bat";

static DesCode_Actions_debounce* gAutomaton = NULL;


static void trace(const char* line)
{
  cout << line << flush;
}


static void idle(void* nothing)
{
}


static void cleanup(void* nothing)
{
  delete gAutomaton;
  gAutomaton = NULL;
}


int main(int argc, char** argv)
{
  initializeEnviroment(argv[0]);
  gAutomaton = new DesCode_Actions_debounce;
  gAutomaton->setTraceFunction(trace);
  initializeSimulatorInThread(kDebounce_Path, 
                              kDebounce_ProjectFile,
                              kDebounce_GraphEditorCommand,
                              kDebounce_TclFile,
                              cleanup,
                              idle, NULL,
                              idle, NULL);
  gAutomaton->execute();
  cleanUpSimulator();
  return 0;
}
