package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass RsObject
public class RsObject extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsObjectJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8A96F010,(short)0xE542,(short)0x11D3,new char[]{0x80,0xE6,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static RsObject getRsObjectFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsObject(comPtr,bAddRef); }
  public static RsObject getRsObjectFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsObject(comPtr); }
  public static RsObject getRsObjectFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new RsObject(unk); }
  public static RsObject convertComPtrToRsObject(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsObject(comPtr,true,releaseComPtr); }
  protected RsObject(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected RsObject(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected RsObject(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected RsObject(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
