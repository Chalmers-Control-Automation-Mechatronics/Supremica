package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ABBS4Procedures
public class ABBS4Procedures extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ProceduresJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedures {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xA1DB06F1,(short)0x95D8,(short)0x11D4,new char[]{0xAE,0x1B,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ABBS4Procedures getABBS4ProceduresFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Procedures(comPtr,bAddRef); }
  public static ABBS4Procedures getABBS4ProceduresFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Procedures(comPtr); }
  public static ABBS4Procedures getABBS4ProceduresFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ABBS4Procedures(unk); }
  public static ABBS4Procedures convertComPtrToABBS4Procedures(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Procedures(comPtr,true,releaseComPtr); }
  protected ABBS4Procedures(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ABBS4Procedures(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ABBS4Procedures(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ABBS4Procedures(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
