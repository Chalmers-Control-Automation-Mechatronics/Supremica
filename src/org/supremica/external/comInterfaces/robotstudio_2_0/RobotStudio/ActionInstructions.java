package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ActionInstructions
public class ActionInstructions extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IActionInstructionsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstructions {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D7773,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static ActionInstructions getActionInstructionsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstructions(comPtr,bAddRef); }
  public static ActionInstructions getActionInstructionsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstructions(comPtr); }
  public static ActionInstructions getActionInstructionsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ActionInstructions(unk); }
  public static ActionInstructions convertComPtrToActionInstructions(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstructions(comPtr,true,releaseComPtr); }
  protected ActionInstructions(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ActionInstructions(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ActionInstructions(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ActionInstructions(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
