package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Face
public class Face extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFace2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x4C6EF998,(short)0x87AB,(short)0x11D3,new char[]{0x8B,0xA0,0x00,0xC0,0x4F,0x68,0xDF,0x58});
  public static Face getFaceFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Face(comPtr,bAddRef); }
  public static Face getFaceFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Face(comPtr); }
  public static Face getFaceFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Face(unk); }
  public static Face convertComPtrToFace(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Face(comPtr,true,releaseComPtr); }
  protected Face(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Face(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Face(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Face(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
