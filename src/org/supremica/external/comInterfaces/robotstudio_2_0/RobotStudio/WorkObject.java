package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass WorkObject
public class WorkObject extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWorkObject3JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject3 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xF21E9770,(short)0xFF3F,(short)0x11D3,new char[]{0xA1,0xE2,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static WorkObject getWorkObjectFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WorkObject(comPtr,bAddRef); }
  public static WorkObject getWorkObjectFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WorkObject(comPtr); }
  public static WorkObject getWorkObjectFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new WorkObject(unk); }
  public static WorkObject convertComPtrToWorkObject(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WorkObject(comPtr,true,releaseComPtr); }
  protected WorkObject(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected WorkObject(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected WorkObject(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected WorkObject(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
