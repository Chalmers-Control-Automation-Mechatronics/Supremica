package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Wires
public class Wires extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWiresJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWires {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xA3A6DFFA,(short)0x87AB,(short)0x11D3,new char[]{0x8B,0xA0,0x00,0xC0,0x4F,0x68,0xDF,0x58});
  public static Wires getWiresFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Wires(comPtr,bAddRef); }
  public static Wires getWiresFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Wires(comPtr); }
  public static Wires getWiresFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Wires(unk); }
  public static Wires convertComPtrToWires(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Wires(comPtr,true,releaseComPtr); }
  protected Wires(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Wires(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Wires(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Wires(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
