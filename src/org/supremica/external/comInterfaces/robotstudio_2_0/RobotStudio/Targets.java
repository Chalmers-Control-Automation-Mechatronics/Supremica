package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Targets
public class Targets extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargets {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xB2415070,(short)0x4E26,(short)0x11D3,new char[]{0xAC,0xA2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Targets getTargetsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Targets(comPtr,bAddRef); }
  public static Targets getTargetsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Targets(comPtr); }
  public static Targets getTargetsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Targets(unk); }
  public static Targets convertComPtrToTargets(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Targets(comPtr,true,releaseComPtr); }
  protected Targets(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Targets(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Targets(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Targets(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
