package org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl;

// Dispinterface _CommandBarComboBoxEvents Event Handler
public class _CommandBarComboBoxEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBoxEvents listener;
  public _CommandBarComboBoxEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBoxEvents listener){ this.listener = listener; }
  public int invoke(int dispid,int flags,int argPtr,int argCnt,int[] namedArgs,int retvalPtr,int[] argErr){
    int posArgCnt = argCnt;
    if(namedArgs != null) posArgCnt -= namedArgs.length;
    switch(dispid) {
      case 1: {
        if(dbg) System.err.println("Event change called");
        org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarComboBox arg0 = null;
        int argPos0 = -1;
        if(posArgCnt > 0){
          argPos0 = argCnt - 1;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 0){
              argPos0 = i;
              break;
            }
          }
        }
        if(argPos0 >= 0){
          try {
            arg0 = org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarComboBox.convertComPtrToCommandBarComboBox(com.inzoom.comjni.Variant.jniGetUnknown(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof()),false);
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.change(arg0);
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
