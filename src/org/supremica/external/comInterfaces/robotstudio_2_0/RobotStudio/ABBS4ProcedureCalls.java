package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ABBS4ProcedureCalls
public class ABBS4ProcedureCalls extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ProcedureCallsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCalls {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8E7D4866,(short)0x98F7,(short)0x11D4,new char[]{0xAE,0x1E,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ABBS4ProcedureCalls getABBS4ProcedureCallsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4ProcedureCalls(comPtr,bAddRef); }
  public static ABBS4ProcedureCalls getABBS4ProcedureCallsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4ProcedureCalls(comPtr); }
  public static ABBS4ProcedureCalls getABBS4ProcedureCallsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ABBS4ProcedureCalls(unk); }
  public static ABBS4ProcedureCalls convertComPtrToABBS4ProcedureCalls(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4ProcedureCalls(comPtr,true,releaseComPtr); }
  protected ABBS4ProcedureCalls(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ABBS4ProcedureCalls(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ABBS4ProcedureCalls(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ABBS4ProcedureCalls(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
