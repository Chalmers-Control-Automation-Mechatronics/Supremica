package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl;

// Dispinterface _PartEvents Event Handler
public class _PartEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._PartEvents listener;
  public _PartEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._PartEvents listener){ this.listener = listener; }
  public int invoke(int dispid,int flags,int argPtr,int argCnt,int[] namedArgs,int retvalPtr,int[] argErr){
    int posArgCnt = argCnt;
    if(namedArgs != null) posArgCnt -= namedArgs.length;
    switch(dispid) {
      case 112: {
        if(dbg) System.err.println("Event changed called");
        int arg0 = 0;
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
            arg0 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.changed(arg0);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 113: {
        if(dbg) System.err.println("Event collisionEnd called");
        org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject arg0 = null;
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
            arg0 = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject.convertComPtrToRsObject(com.inzoom.comjni.Variant.jniGetUnknown(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof()),false);
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.collisionEnd(arg0);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 114: {
        if(dbg) System.err.println("Event collisionStart called");
        org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject arg0 = null;
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
            arg0 = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject.convertComPtrToRsObject(com.inzoom.comjni.Variant.jniGetUnknown(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof()),false);
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.collisionStart(arg0);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 115: {
        if(dbg) System.err.println("Event selected called");
        try{
          listener.selected();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 116: {
        if(dbg) System.err.println("Event unSelected called");
        try{
          listener.unSelected();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 6: {
        if(dbg) System.err.println("Event open called");
        try{
          listener.open();
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
