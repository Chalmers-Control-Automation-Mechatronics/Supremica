package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Argument
public class Argument extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IArgumentJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IArgument {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x4A663EE6,(short)0xA985,(short)0x11D4,new char[]{0xAE,0x35,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Argument getArgumentFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Argument(comPtr,bAddRef); }
  public static Argument getArgumentFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Argument(comPtr); }
  public static Argument getArgumentFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Argument(unk); }
  public static Argument convertComPtrToArgument(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Argument(comPtr,true,releaseComPtr); }
  protected Argument(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Argument(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Argument(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Argument(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
