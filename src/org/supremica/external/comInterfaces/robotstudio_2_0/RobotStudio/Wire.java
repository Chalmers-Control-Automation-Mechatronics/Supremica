package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Wire
public class Wire extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWireJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWire {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xA1127498,(short)0x87AB,(short)0x11D3,new char[]{0x8B,0xA0,0x00,0xC0,0x4F,0x68,0xDF,0x58});
  public static Wire getWireFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Wire(comPtr,bAddRef); }
  public static Wire getWireFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Wire(comPtr); }
  public static Wire getWireFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Wire(unk); }
  public static Wire convertComPtrToWire(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Wire(comPtr,true,releaseComPtr); }
  protected Wire(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Wire(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Wire(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Wire(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
