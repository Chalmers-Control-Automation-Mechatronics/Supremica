package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IToolFrame2 Declaration
public interface IToolFrame2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x2C59F50A,(short)0x4883,(short)0x11D5,new char[]{0xBC,0x91,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public String getModuleName() throws com.inzoom.comjni.ComJniException;
  public void setModuleName(String pVal) throws com.inzoom.comjni.ComJniException;
}
