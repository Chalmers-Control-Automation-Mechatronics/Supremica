package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass PathInstructions
public class PathInstructions extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathInstructionsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstructions {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCE7D777C,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public static PathInstructions getPathInstructionsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathInstructions(comPtr,bAddRef); }
  public static PathInstructions getPathInstructionsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathInstructions(comPtr); }
  public static PathInstructions getPathInstructionsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new PathInstructions(unk); }
  public static PathInstructions convertComPtrToPathInstructions(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PathInstructions(comPtr,true,releaseComPtr); }
  protected PathInstructions(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected PathInstructions(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected PathInstructions(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected PathInstructions(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
