package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl;

// Dispinterface DControllerEvents Event Handler
public class DControllerEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DControllerEvents listener;
  public DControllerEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DControllerEvents listener){ this.listener = listener; }
  public int invoke(int dispid,int flags,int argPtr,int argCnt,int[] namedArgs,int retvalPtr,int[] argErr){
    int posArgCnt = argCnt;
    if(namedArgs != null) posArgCnt -= namedArgs.length;
    switch(dispid) {
      case 1: {
        if(dbg) System.err.println("Event afterTick called");
        double arg0 = (double) 0;
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
            arg0 = com.inzoom.comjni.Variant.jniGetDouble(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.afterTick(arg0);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 2: {
        if(dbg) System.err.println("Event beforeTick called");
        double arg0 = (double) 0;
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
            arg0 = com.inzoom.comjni.Variant.jniGetDouble(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        double[] arg1 = {(double) 0};
        int argPos1 = -1;
        if(posArgCnt > 1){
          argPos1 = argCnt - 2;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 1){
              argPos1 = i;
              break;
            }
          }
        }
        if(argPos1 >= 0){
          try {
            arg1[0] = com.inzoom.comjni.Variant.jniGetDouble(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.beforeTick(arg0,arg1);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        try {
          if(argPos1 >= 0)com.inzoom.comjni.Variant.jniSetRefDouble(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof(),arg1[0]);
        } catch(com.inzoom.comjni.ComJniException e) {
          if(dbg) {System.err.print("Caught exception: returning arg" + argPos1 + ':'); e.printStackTrace(System.err);}
          if(argErr!=null) argErr[0] = 1;
          return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 3: {
        if(dbg) System.err.println("Event create called");
        try{
          listener.create();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 4: {
        if(dbg) System.err.println("Event start called");
        try{
          listener.start();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 5: {
        if(dbg) System.err.println("Event stop called");
        try{
          listener.stop();
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
