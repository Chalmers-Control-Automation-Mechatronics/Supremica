package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPath2 Declaration
public interface IPath2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x1E89EEE3,(short)0x9D35,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstructions getPathInstructions() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction insertPathInstruction(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject NewItem,int order,com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction insertPathInstruction(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject NewItem,int order) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction insertPathInstruction(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject NewItem) throws com.inzoom.comjni.ComJniException;
}
