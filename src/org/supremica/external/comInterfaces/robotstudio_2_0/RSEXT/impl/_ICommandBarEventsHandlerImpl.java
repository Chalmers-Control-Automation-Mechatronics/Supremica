package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl;

// Dispinterface _ICommandBarEvents Event Handler
public class _ICommandBarEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarEvents listener;
  public _ICommandBarEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarEvents listener){ this.listener = listener; }
  public int invoke(int dispid,int flags,int argPtr,int argCnt,int[] namedArgs,int retvalPtr,int[] argErr){
    int posArgCnt = argCnt;
    if(namedArgs != null) posArgCnt -= namedArgs.length;
    switch(dispid) {
      default:
        if(dbg) System.err.println("Unsupported event with dispid " + dispid + "called");
        return  com.inzoom.comjni.enum.HResult.DISP_E_MEMBERNOTFOUND;
    }
  }
}
