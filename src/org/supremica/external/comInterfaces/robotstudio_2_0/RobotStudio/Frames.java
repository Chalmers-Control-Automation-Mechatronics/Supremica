package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Frames
public class Frames extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFramesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFrames {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x082DAA8D,(short)0x8079,(short)0x11D3,new char[]{0xAC,0xD7,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Frames getFramesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Frames(comPtr,bAddRef); }
  public static Frames getFramesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Frames(comPtr); }
  public static Frames getFramesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Frames(unk); }
  public static Frames convertComPtrToFrames(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Frames(comPtr,true,releaseComPtr); }
  protected Frames(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Frames(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Frames(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Frames(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
