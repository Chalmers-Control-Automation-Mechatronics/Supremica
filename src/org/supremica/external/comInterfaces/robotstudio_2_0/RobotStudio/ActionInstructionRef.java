package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ActionInstructionRef
public class ActionInstructionRef extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IActionInstructionRefJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstructionRef {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D777E,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static ActionInstructionRef getActionInstructionRefFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstructionRef(comPtr,bAddRef); }
  public static ActionInstructionRef getActionInstructionRefFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstructionRef(comPtr); }
  public static ActionInstructionRef getActionInstructionRefFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ActionInstructionRef(unk); }
  public static ActionInstructionRef convertComPtrToActionInstructionRef(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstructionRef(comPtr,true,releaseComPtr); }
  protected ActionInstructionRef(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ActionInstructionRef(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ActionInstructionRef(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ActionInstructionRef(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
