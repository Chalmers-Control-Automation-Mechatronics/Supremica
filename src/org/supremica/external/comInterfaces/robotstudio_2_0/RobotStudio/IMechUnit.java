package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IMechUnit Declaration
public interface IMechUnit extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x568DC29B,(short)0x8BD4,(short)0x11D5,new char[]{0xBC,0xA1,0x00,0xD0,0xB7,0xE6,0x16,0x7C});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public boolean getActive() throws com.inzoom.comjni.ComJniException;
  public void setActive(boolean ppIsActive) throws com.inzoom.comjni.ComJniException;
  public boolean getCanActivate() throws com.inzoom.comjni.ComJniException;
  public boolean forceActive() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 getParent() throws com.inzoom.comjni.ComJniException;
  public void setParent(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 ppController) throws com.inzoom.comjni.ComJniException;
}
