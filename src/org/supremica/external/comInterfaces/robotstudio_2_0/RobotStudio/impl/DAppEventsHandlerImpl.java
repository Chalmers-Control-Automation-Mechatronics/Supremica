package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl;

// Dispinterface DAppEvents Event Handler
public class DAppEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DAppEvents listener;
  public DAppEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DAppEvents listener){ this.listener = listener; }
  public int invoke(int dispid,int flags,int argPtr,int argCnt,int[] namedArgs,int retvalPtr,int[] argErr){
    int posArgCnt = argCnt;
    if(namedArgs != null) posArgCnt -= namedArgs.length;
    switch(dispid) {
      case 101: {
        if(dbg) System.err.println("Event selectionChanged called");
        try{
          listener.selectionChanged();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 102: {
        if(dbg) System.err.println("Event quit called");
        try{
          listener.quit();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 103: {
        if(dbg) System.err.println("Event stationBeforeOpen called");
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
        boolean[] arg1 = {false};
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
            arg1[0] = com.inzoom.comjni.Variant.jniGetBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.stationBeforeOpen(arg0,arg1);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        try {
          if(argPos1 >= 0)com.inzoom.comjni.Variant.jniSetRefBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof(),arg1[0]);
        } catch(com.inzoom.comjni.ComJniException e) {
          if(dbg) {System.err.print("Caught exception: returning arg" + argPos1 + ':'); e.printStackTrace(System.err);}
          if(argErr!=null) argErr[0] = 1;
          return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 105: {
        if(dbg) System.err.println("Event stationAfterOpen called");
        org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station arg0 = null;
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
            arg0 = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station.convertComPtrToStation(com.inzoom.comjni.Variant.jniGetUnknown(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof()),false);
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.stationAfterOpen(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 104: {
        if(dbg) System.err.println("Event stationBeforeSave called");
        org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station arg0 = null;
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
            arg0 = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station.convertComPtrToStation(com.inzoom.comjni.Variant.jniGetUnknown(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof()),false);
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        boolean[] arg1 = {false};
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
            arg1[0] = com.inzoom.comjni.Variant.jniGetBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.stationBeforeSave(arg0,arg1);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        try {
          if(argPos1 >= 0)com.inzoom.comjni.Variant.jniSetRefBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof(),arg1[0]);
        } catch(com.inzoom.comjni.ComJniException e) {
          if(dbg) {System.err.print("Caught exception: returning arg" + argPos1 + ':'); e.printStackTrace(System.err);}
          if(argErr!=null) argErr[0] = 1;
          return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 106: {
        if(dbg) System.err.println("Event stationAfterSave called");
        org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station arg0 = null;
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
            arg0 = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station.convertComPtrToStation(com.inzoom.comjni.Variant.jniGetUnknown(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof()),false);
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.stationAfterSave(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 107: {
        if(dbg) System.err.println("Event libraryBeforeOpen called");
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
        boolean[] arg1 = {false};
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
            arg1[0] = com.inzoom.comjni.Variant.jniGetBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.libraryBeforeOpen(arg0,arg1);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        try {
          if(argPos1 >= 0)com.inzoom.comjni.Variant.jniSetRefBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof(),arg1[0]);
        } catch(com.inzoom.comjni.ComJniException e) {
          if(dbg) {System.err.print("Caught exception: returning arg" + argPos1 + ':'); e.printStackTrace(System.err);}
          if(argErr!=null) argErr[0] = 1;
          return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 108: {
        if(dbg) System.err.println("Event libraryAfterOpen called");
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
          int retVal = listener.libraryAfterOpen(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 109: {
        if(dbg) System.err.println("Event libraryBeforeSave called");
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
        boolean[] arg1 = {false};
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
            arg1[0] = com.inzoom.comjni.Variant.jniGetBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.libraryBeforeSave(arg0,arg1);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        try {
          if(argPos1 >= 0)com.inzoom.comjni.Variant.jniSetRefBOOL(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof(),arg1[0]);
        } catch(com.inzoom.comjni.ComJniException e) {
          if(dbg) {System.err.print("Caught exception: returning arg" + argPos1 + ':'); e.printStackTrace(System.err);}
          if(argErr!=null) argErr[0] = 1;
          return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 110: {
        if(dbg) System.err.println("Event libraryAfterSave called");
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
          int retVal = listener.libraryAfterSave(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 111: {
        if(dbg) System.err.println("Event started called");
        try{
          int retVal = listener.started();
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
