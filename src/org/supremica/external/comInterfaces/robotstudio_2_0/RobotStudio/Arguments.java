package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Arguments
public class Arguments extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IArgumentsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IArguments {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x4A663EE4,(short)0xA985,(short)0x11D4,new char[]{0xAE,0x35,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Arguments getArgumentsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Arguments(comPtr,bAddRef); }
  public static Arguments getArgumentsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Arguments(comPtr); }
  public static Arguments getArgumentsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Arguments(unk); }
  public static Arguments convertComPtrToArguments(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Arguments(comPtr,true,releaseComPtr); }
  protected Arguments(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Arguments(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Arguments(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Arguments(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
