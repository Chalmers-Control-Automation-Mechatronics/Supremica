package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ActionInstruction
public class ActionInstruction extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IActionInstructionJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstruction {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D7770,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static ActionInstruction getActionInstructionFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstruction(comPtr,bAddRef); }
  public static ActionInstruction getActionInstructionFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstruction(comPtr); }
  public static ActionInstruction getActionInstructionFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ActionInstruction(unk); }
  public static ActionInstruction convertComPtrToActionInstruction(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ActionInstruction(comPtr,true,releaseComPtr); }
  protected ActionInstruction(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ActionInstruction(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ActionInstruction(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ActionInstruction(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
