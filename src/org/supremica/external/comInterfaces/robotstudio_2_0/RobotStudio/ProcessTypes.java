package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ProcessTypes
public class ProcessTypes extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IProcessTypesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessTypes {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBF271941,(short)0xA4C6,(short)0x11D4,new char[]{0xAE,0x30,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ProcessTypes getProcessTypesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProcessTypes(comPtr,bAddRef); }
  public static ProcessTypes getProcessTypesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProcessTypes(comPtr); }
  public static ProcessTypes getProcessTypesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ProcessTypes(unk); }
  public static ProcessTypes convertComPtrToProcessTypes(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProcessTypes(comPtr,true,releaseComPtr); }
  protected ProcessTypes(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ProcessTypes(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ProcessTypes(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ProcessTypes(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
