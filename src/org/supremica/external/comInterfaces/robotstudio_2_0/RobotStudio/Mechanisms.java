package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Mechanisms
public class Mechanisms extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMechanismsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanisms {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xE410164F,(short)0x884E,(short)0x11D3,new char[]{0xAC,0xE2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Mechanisms getMechanismsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Mechanisms(comPtr,bAddRef); }
  public static Mechanisms getMechanismsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Mechanisms(comPtr); }
  public static Mechanisms getMechanismsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Mechanisms(unk); }
  public static Mechanisms convertComPtrToMechanisms(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Mechanisms(comPtr,true,releaseComPtr); }
  protected Mechanisms(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Mechanisms(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Mechanisms(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Mechanisms(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
