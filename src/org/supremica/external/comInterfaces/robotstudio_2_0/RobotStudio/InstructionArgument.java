package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass InstructionArgument
public class InstructionArgument extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IInstructionArgumentJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IInstructionArgument {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D776C,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static InstructionArgument getInstructionArgumentFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new InstructionArgument(comPtr,bAddRef); }
  public static InstructionArgument getInstructionArgumentFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new InstructionArgument(comPtr); }
  public static InstructionArgument getInstructionArgumentFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new InstructionArgument(unk); }
  public static InstructionArgument convertComPtrToInstructionArgument(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new InstructionArgument(comPtr,true,releaseComPtr); }
  protected InstructionArgument(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected InstructionArgument(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected InstructionArgument(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected InstructionArgument(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
