package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Attributes
public class Attributes extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xF1D0FF04,(short)0xE2C8,(short)0x11D3,new char[]{0xAD,0x47,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Attributes getAttributesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Attributes(comPtr,bAddRef); }
  public static Attributes getAttributesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Attributes(comPtr); }
  public static Attributes getAttributesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Attributes(unk); }
  public static Attributes convertComPtrToAttributes(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Attributes(comPtr,true,releaseComPtr); }
  protected Attributes(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Attributes(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Attributes(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Attributes(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
