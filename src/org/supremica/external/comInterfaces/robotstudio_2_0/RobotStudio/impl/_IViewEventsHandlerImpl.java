package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl;

// Dispinterface _IViewEvents Event Handler
public class _IViewEventsHandlerImpl extends com.inzoom.comjni.JDispEventHandler {
  public static boolean dbg = false;
  public final org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IViewEvents listener;
  public _IViewEventsHandlerImpl(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IViewEvents listener){ this.listener = listener; }
  public int invoke(int dispid,int flags,int argPtr,int argCnt,int[] namedArgs,int retvalPtr,int[] argErr){
    int posArgCnt = argCnt;
    if(namedArgs != null) posArgCnt -= namedArgs.length;
    switch(dispid) {
      case 1: {
        if(dbg) System.err.println("Event click called");
        try{
          listener.click();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 2: {
        if(dbg) System.err.println("Event dblClick called");
        try{
          listener.dblClick();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 3: {
        if(dbg) System.err.println("Event keyDown called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.keyDown(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 4: {
        if(dbg) System.err.println("Event keyUp called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.keyUp(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 5: {
        if(dbg) System.err.println("Event lButtonDblClk called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.lButtonDblClk(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 6: {
        if(dbg) System.err.println("Event lButtonDown called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.lButtonDown(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 7: {
        if(dbg) System.err.println("Event lButtonUp called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.lButtonUp(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 8: {
        if(dbg) System.err.println("Event mButtonDblClk called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.mButtonDblClk(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 9: {
        if(dbg) System.err.println("Event mButtonDown called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.mButtonDown(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 10: {
        if(dbg) System.err.println("Event mButtonUp called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.mButtonUp(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 11: {
        if(dbg) System.err.println("Event rButtonDblClk called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.rButtonDblClk(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 12: {
        if(dbg) System.err.println("Event rButtonDown called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.rButtonDown(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 13: {
        if(dbg) System.err.println("Event rButtonUp called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.rButtonUp(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 14: {
        if(dbg) System.err.println("Event mouseMove called");
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
        int arg1 = 0;
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
            arg1 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos1 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos1 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 1;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        int arg2 = 0;
        int argPos2 = -1;
        if(posArgCnt > 2){
          argPos2 = argCnt - 3;
        } else if(namedArgs != null) {
          for(int i = 0; i < namedArgs.length; i++){
            if(namedArgs[i] == 2){
              argPos2 = i;
              break;
            }
          }
        }
        if(argPos2 >= 0){
          try {
            arg2 = com.inzoom.comjni.Variant.jniGetInt(argPtr + argPos2 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos2 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 2;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          listener.mouseMove(arg0,arg1,arg2);
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 15: {
        if(dbg) System.err.println("Event gotFocus called");
        try{
          listener.gotFocus();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 16: {
        if(dbg) System.err.println("Event lostFocus called");
        try{
          listener.lostFocus();
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      case 17: {
        if(dbg) System.err.println("Event unload called");
        boolean[] arg0 = {false};
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
            arg0[0] = com.inzoom.comjni.Variant.jniGetBOOL(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof());
          } catch(com.inzoom.comjni.ComJniException e) {
            if(dbg) {System.err.print("Caught exception reading arg" + argPos0 + ':'); e.printStackTrace(System.err);}
            if(argErr!=null) argErr[0] = 0;
            return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
          }
        }
        try{
          int retVal = listener.unload(arg0);
          if(retvalPtr != 0){
            com.inzoom.comjni.Variant.jniSetInt(retvalPtr,0,retVal,(short)25);
          }
        } catch(Exception e) {
          if(dbg) {System.err.print("Caught exception while handling event: "); e.printStackTrace(System.err);}
          return com.inzoom.comjni.enum.HResult.E_FAIL;
        }
        try {
          if(argPos0 >= 0)com.inzoom.comjni.Variant.jniSetRefBOOL(argPtr + argPos0 * com.inzoom.comjni.Variant.sizeof(),arg0[0]);
        } catch(com.inzoom.comjni.ComJniException e) {
          if(dbg) {System.err.print("Caught exception: returning arg" + argPos0 + ':'); e.printStackTrace(System.err);}
          if(argErr!=null) argErr[0] = 0;
          return com.inzoom.comjni.enum.HResult.DISP_E_TYPEMISMATCH;
        }
        return  com.inzoom.comjni.enum.HResult.S_OK;
      }
      default:
        if(dbg) System.err.println("Unsupported event with dispid " + dispid + "called");
        return  com.inzoom.comjni.enum.HResult.DISP_E_MEMBERNOTFOUND;
    }
  }
}
