package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ABBS4Modules
public class ABBS4Modules extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ModulesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Modules {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xABE01205,(short)0x92B0,(short)0x11D4,new char[]{0xAE,0x10,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ABBS4Modules getABBS4ModulesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Modules(comPtr,bAddRef); }
  public static ABBS4Modules getABBS4ModulesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Modules(comPtr); }
  public static ABBS4Modules getABBS4ModulesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ABBS4Modules(unk); }
  public static ABBS4Modules convertComPtrToABBS4Modules(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Modules(comPtr,true,releaseComPtr); }
  protected ABBS4Modules(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ABBS4Modules(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ABBS4Modules(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ABBS4Modules(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
