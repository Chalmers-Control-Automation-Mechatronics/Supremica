package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass TargetRefs
public class TargetRefs extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetRefsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRefs {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8080E64B,(short)0x7D66,(short)0x11D3,new char[]{0xAC,0xD5,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static TargetRefs getTargetRefsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetRefs(comPtr,bAddRef); }
  public static TargetRefs getTargetRefsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetRefs(comPtr); }
  public static TargetRefs getTargetRefsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new TargetRefs(unk); }
  public static TargetRefs convertComPtrToTargetRefs(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetRefs(comPtr,true,releaseComPtr); }
  protected TargetRefs(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected TargetRefs(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected TargetRefs(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected TargetRefs(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
