package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IStation2 Declaration
public interface IStation2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8D711417,(short)0x909C,(short)0x11D5,new char[]{0xBC,0xA3,0x00,0xD0,0xB7,0xE6,0x16,0x7C});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 importPart(String FileName,boolean Optimize) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 importPart(String FileName) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstructions getActionInstructions() throws com.inzoom.comjni.ComJniException;
  public void clearSyncHistory() throws com.inzoom.comjni.ComJniException;
}
