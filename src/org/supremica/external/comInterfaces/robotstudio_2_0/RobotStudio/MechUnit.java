package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass MechUnit
public class MechUnit extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMechUnitJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechUnit {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8D3A20A4,(short)0x53D4,(short)0x4063,new char[]{0xA8,0x37,0xAF,0xDF,0x6B,0xB5,0xAC,0xF2});
  public static MechUnit getMechUnitFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MechUnit(comPtr,bAddRef); }
  public static MechUnit getMechUnitFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MechUnit(comPtr); }
  public static MechUnit getMechUnitFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new MechUnit(unk); }
  public static MechUnit convertComPtrToMechUnit(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MechUnit(comPtr,true,releaseComPtr); }
  protected MechUnit(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected MechUnit(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected MechUnit(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected MechUnit(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
