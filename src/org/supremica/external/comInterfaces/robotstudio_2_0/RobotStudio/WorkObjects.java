package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass WorkObjects
public class WorkObjects extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWorkObjectsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObjects {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8D6E0E2E,(short)0x0097,(short)0x11D4,new char[]{0xA1,0xE2,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static WorkObjects getWorkObjectsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WorkObjects(comPtr,bAddRef); }
  public static WorkObjects getWorkObjectsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WorkObjects(comPtr); }
  public static WorkObjects getWorkObjectsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new WorkObjects(unk); }
  public static WorkObjects convertComPtrToWorkObjects(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WorkObjects(comPtr,true,releaseComPtr); }
  protected WorkObjects(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected WorkObjects(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected WorkObjects(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected WorkObjects(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
