package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPathInstruction Declaration
public interface IPathInstruction extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xCE7D777B,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getParent() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
}
