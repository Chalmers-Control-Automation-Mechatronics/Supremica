package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl;

// Dispinterface _MechanismEvents Event Handler
public class _MechanismEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._MechanismEvents listener;
  public _MechanismEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._MechanismEvents listener){ this.listener = listener; }
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
          int retVal = listener.changed(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
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
          int retVal = listener.collisionEnd(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
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
          int retVal = listener.collisionStart(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 115: {
        if(dbg) System.err.println("Event selected called");
        try{
          int retVal = listener.selected();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 5: {
        if(dbg) System.err.println("Event tick called");
        float arg0 = (float) 0;
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
            arg0 = com.inzoom.comjni.Variant.jniGetFloat(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.tick(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 116: {
        if(dbg) System.err.println("Event unSelected called");
        try{
          int retVal = listener.unSelected();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 7: {
        if(dbg) System.err.println("Event targetReached called");
        try{
          int retVal = listener.targetReached();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 10: {
        if(dbg) System.err.println("Event iOChange called");
        String arg0 = null;
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
            arg0 = com.inzoom.comjni.Variant.jniGetBSTR(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int[] arg1 = {0};
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
            arg1[0] = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.iOChange(arg0,arg1);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        try {
          if(argPos1 >= 0)com.inzoom.comjni.Variant.jniSetRefInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof(),arg1[0]);
        } catch(com.inzoom.comjni.ComJniException e) {
          if(dbg) {System.err.print("Caught exception: returning arg" + argPos1 + ':'); e.printStackTrace(System.err);}
          if(argErr!=null) argErr[0] = 1;
          return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 11: {
        if(dbg) System.err.println("Event jointLimit called");
        try{
          int retVal = listener.jointLimit();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 12: {
        if(dbg) System.err.println("Event singularity called");
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
        String arg1 = null;
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
            arg1 = com.inzoom.comjni.Variant.jniGetBSTR(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.singularity(arg0,arg1);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 13: {
        if(dbg) System.err.println("Event toolChanged called");
        try{
          int retVal = listener.toolChanged();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 14: {
        if(dbg) System.err.println("Event workObjectChanged called");
        try{
          int retVal = listener.workObjectChanged();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 17: {
        if(dbg) System.err.println("Event controllerError called");
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
        String arg1 = null;
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
            arg1 = com.inzoom.comjni.Variant.jniGetBSTR(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.controllerError(arg0,arg1);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 19: {
        if(dbg) System.err.println("Event open called");
        try{
          listener.open();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 117: {
        if(dbg) System.err.println("Event beforeControllerStarted called");
        try{
          int retVal = listener.beforeControllerStarted();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 118: {
        if(dbg) System.err.println("Event afterControllerStarted called");
        try{
          int retVal = listener.afterControllerStarted();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 119: {
        if(dbg) System.err.println("Event afterControllerShutdown called");
        try{
          int retVal = listener.afterControllerShutdown();
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
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
