package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass MechUnits
public class MechUnits extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMechUnitsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechUnits {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x368E844E,(short)0xEE0D,(short)0x4D16,new char[]{0xAA,0x41,0x92,0x67,0x3B,0x10,0x7A,0x6D});
  public static MechUnits getMechUnitsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MechUnits(comPtr,bAddRef); }
  public static MechUnits getMechUnitsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MechUnits(comPtr); }
  public static MechUnits getMechUnitsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new MechUnits(unk); }
  public static MechUnits convertComPtrToMechUnits(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MechUnits(comPtr,true,releaseComPtr); }
  protected MechUnits(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected MechUnits(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected MechUnits(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected MechUnits(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
