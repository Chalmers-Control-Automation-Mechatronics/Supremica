package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ArgumentValue
public class ArgumentValue extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IArgumentValueJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IArgumentValue {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D7775,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static ArgumentValue getArgumentValueFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ArgumentValue(comPtr,bAddRef); }
  public static ArgumentValue getArgumentValueFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ArgumentValue(comPtr); }
  public static ArgumentValue getArgumentValueFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ArgumentValue(unk); }
  public static ArgumentValue convertComPtrToArgumentValue(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ArgumentValue(comPtr,true,releaseComPtr); }
  protected ArgumentValue(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ArgumentValue(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ArgumentValue(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ArgumentValue(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
