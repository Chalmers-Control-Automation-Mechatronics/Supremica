package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ABBS4Module
public class ABBS4Module extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ModuleJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Module {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xABE01210,(short)0x92B0,(short)0x11D4,new char[]{0xAE,0x10,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ABBS4Module getABBS4ModuleFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Module(comPtr,bAddRef); }
  public static ABBS4Module getABBS4ModuleFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Module(comPtr); }
  public static ABBS4Module getABBS4ModuleFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ABBS4Module(unk); }
  public static ABBS4Module convertComPtrToABBS4Module(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Module(comPtr,true,releaseComPtr); }
  protected ABBS4Module(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ABBS4Module(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ABBS4Module(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ABBS4Module(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
