package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IUserOptions2 Declaration
public interface IUserOptions2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xB8C3FE80,(short)0x8273,(short)0x11D5,new char[]{0xBC,0x9E,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public boolean getShowSyncDialog() throws com.inzoom.comjni.ComJniException;
  public void setShowSyncDialog(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getQuickRefreshSystem() throws com.inzoom.comjni.ComJniException;
  public void setQuickRefreshSystem(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getQuickRefreshProgram() throws com.inzoom.comjni.ComJniException;
  public void setQuickRefreshProgram(boolean pVal) throws com.inzoom.comjni.ComJniException;
}
