package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ABBS4Procedure
public class ABBS4Procedure extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ProcedureJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xA1DB06F3,(short)0x95D8,(short)0x11D4,new char[]{0xAE,0x1B,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ABBS4Procedure getABBS4ProcedureFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Procedure(comPtr,bAddRef); }
  public static ABBS4Procedure getABBS4ProcedureFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Procedure(comPtr); }
  public static ABBS4Procedure getABBS4ProcedureFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ABBS4Procedure(unk); }
  public static ABBS4Procedure convertComPtrToABBS4Procedure(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Procedure(comPtr,true,releaseComPtr); }
  protected ABBS4Procedure(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ABBS4Procedure(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ABBS4Procedure(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ABBS4Procedure(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
