package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass RsAttribute
public class RsAttribute extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributeJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttribute {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xF1D0FF06,(short)0xE2C8,(short)0x11D3,new char[]{0xAD,0x47,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static RsAttribute getRsAttributeFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsAttribute(comPtr,bAddRef); }
  public static RsAttribute getRsAttributeFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsAttribute(comPtr); }
  public static RsAttribute getRsAttributeFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new RsAttribute(unk); }
  public static RsAttribute convertComPtrToRsAttribute(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsAttribute(comPtr,true,releaseComPtr); }
  protected RsAttribute(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected RsAttribute(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected RsAttribute(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected RsAttribute(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
