package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass InstructionArguments
public class InstructionArguments extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IInstructionArgumentsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IInstructionArguments {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D776E,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static InstructionArguments getInstructionArgumentsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new InstructionArguments(comPtr,bAddRef); }
  public static InstructionArguments getInstructionArgumentsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new InstructionArguments(comPtr); }
  public static InstructionArguments getInstructionArgumentsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new InstructionArguments(unk); }
  public static InstructionArguments convertComPtrToInstructionArguments(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new InstructionArguments(comPtr,true,releaseComPtr); }
  protected InstructionArguments(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected InstructionArguments(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected InstructionArguments(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected InstructionArguments(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
