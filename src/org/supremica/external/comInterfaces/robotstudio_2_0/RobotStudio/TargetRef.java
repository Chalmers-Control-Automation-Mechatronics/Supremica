package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass TargetRef
public class TargetRef extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetRef2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8080E649,(short)0x7D66,(short)0x11D3,new char[]{0xAC,0xD5,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static TargetRef getTargetRefFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetRef(comPtr,bAddRef); }
  public static TargetRef getTargetRefFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetRef(comPtr); }
  public static TargetRef getTargetRefFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new TargetRef(unk); }
  public static TargetRef convertComPtrToTargetRef(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetRef(comPtr,true,releaseComPtr); }
  protected TargetRef(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected TargetRef(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected TargetRef(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected TargetRef(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
