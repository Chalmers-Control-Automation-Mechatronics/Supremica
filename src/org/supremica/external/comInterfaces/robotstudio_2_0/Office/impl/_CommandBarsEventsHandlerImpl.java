package org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl;

// Dispinterface _CommandBarsEvents Event Handler
public class _CommandBarsEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarsEvents listener;
  public _CommandBarsEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarsEvents listener){ this.listener = listener; }
  public int invoke(int dispid,int flags,int argPtr,int argCnt,int[] namedArgs,int retvalPtr,int[] argErr){
    int posArgCnt = argCnt;
    if(namedArgs != null) posArgCnt -= namedArgs.length;
    switch(dispid) {
      case 1: {
        if(dbg) System.err.println("Event onUpdate called");
        try{
          listener.onUpdate();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      default:
        if(dbg) System.err.println("Unsupported event with dispid " + dispid + "called");
        return  com.inzoom.comjni.enum.HResult.DISP_E_MEMBERNOTFOUND;
    }
  }
}
