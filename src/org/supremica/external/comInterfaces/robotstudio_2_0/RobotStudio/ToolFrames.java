package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ToolFrames
public class ToolFrames extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IToolFramesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrames {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDD23F8CC,(short)0x2C87,(short)0x11D4,new char[]{0xAD,0x9E,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ToolFrames getToolFramesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ToolFrames(comPtr,bAddRef); }
  public static ToolFrames getToolFramesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ToolFrames(comPtr); }
  public static ToolFrames getToolFramesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ToolFrames(unk); }
  public static ToolFrames convertComPtrToToolFrames(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ToolFrames(comPtr,true,releaseComPtr); }
  protected ToolFrames(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ToolFrames(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ToolFrames(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ToolFrames(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
