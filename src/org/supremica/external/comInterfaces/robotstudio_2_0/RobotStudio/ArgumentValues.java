package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ArgumentValues
public class ArgumentValues extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IArgumentValuesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IArgumentValues {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D7778,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static ArgumentValues getArgumentValuesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ArgumentValues(comPtr,bAddRef); }
  public static ArgumentValues getArgumentValuesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ArgumentValues(comPtr); }
  public static ArgumentValues getArgumentValuesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ArgumentValues(unk); }
  public static ArgumentValues convertComPtrToArgumentValues(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ArgumentValues(comPtr,true,releaseComPtr); }
  protected ArgumentValues(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ArgumentValues(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ArgumentValues(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ArgumentValues(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
