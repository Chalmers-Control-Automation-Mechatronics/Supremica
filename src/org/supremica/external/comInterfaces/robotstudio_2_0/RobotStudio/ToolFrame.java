package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ToolFrame
public class ToolFrame extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IToolFrame2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xF21E9772,(short)0xFF3F,(short)0x11D3,new char[]{0xA1,0xE2,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static ToolFrame getToolFrameFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ToolFrame(comPtr,bAddRef); }
  public static ToolFrame getToolFrameFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ToolFrame(comPtr); }
  public static ToolFrame getToolFrameFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ToolFrame(unk); }
  public static ToolFrame convertComPtrToToolFrame(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ToolFrame(comPtr,true,releaseComPtr); }
  protected ToolFrame(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ToolFrame(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ToolFrame(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ToolFrame(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
