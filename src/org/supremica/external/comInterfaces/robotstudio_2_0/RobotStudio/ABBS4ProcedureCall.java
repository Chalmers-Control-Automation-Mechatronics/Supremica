package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ABBS4ProcedureCall
public class ABBS4ProcedureCall extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ProcedureCallJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCall {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8E7D4864,(short)0x98F7,(short)0x11D4,new char[]{0xAE,0x1E,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ABBS4ProcedureCall getABBS4ProcedureCallFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4ProcedureCall(comPtr,bAddRef); }
  public static ABBS4ProcedureCall getABBS4ProcedureCallFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4ProcedureCall(comPtr); }
  public static ABBS4ProcedureCall getABBS4ProcedureCallFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ABBS4ProcedureCall(unk); }
  public static ABBS4ProcedureCall convertComPtrToABBS4ProcedureCall(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4ProcedureCall(comPtr,true,releaseComPtr); }
  protected ABBS4ProcedureCall(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ABBS4ProcedureCall(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ABBS4ProcedureCall(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ABBS4ProcedureCall(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
