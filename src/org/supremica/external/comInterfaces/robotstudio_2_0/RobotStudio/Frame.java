package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Frame
public class Frame extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFrameJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFrame {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x082DAA8B,(short)0x8079,(short)0x11D3,new char[]{0xAC,0xD7,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Frame getFrameFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Frame(comPtr,bAddRef); }
  public static Frame getFrameFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Frame(comPtr); }
  public static Frame getFrameFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Frame(unk); }
  public static Frame convertComPtrToFrame(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Frame(comPtr,true,releaseComPtr); }
  protected Frame(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Frame(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Frame(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Frame(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
