package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ProcessType
public class ProcessType extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IProcessTypeJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessType {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBF271986,(short)0xA4C6,(short)0x11D4,new char[]{0xAE,0x30,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ProcessType getProcessTypeFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProcessType(comPtr,bAddRef); }
  public static ProcessType getProcessTypeFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProcessType(comPtr); }
  public static ProcessType getProcessTypeFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ProcessType(unk); }
  public static ProcessType convertComPtrToProcessType(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ProcessType(comPtr,true,releaseComPtr); }
  protected ProcessType(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ProcessType(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ProcessType(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ProcessType(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
