package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass PathInstruction
public class PathInstruction extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathInstructionJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D777A,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static PathInstruction getPathInstructionFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathInstruction(comPtr,bAddRef); }
  public static PathInstruction getPathInstructionFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathInstruction(comPtr); }
  public static PathInstruction getPathInstructionFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new PathInstruction(unk); }
  public static PathInstruction convertComPtrToPathInstruction(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathInstruction(comPtr,true,releaseComPtr); }
  protected PathInstruction(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected PathInstruction(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected PathInstruction(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected PathInstruction(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
