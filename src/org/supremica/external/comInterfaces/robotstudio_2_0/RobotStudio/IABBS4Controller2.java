package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IABBS4Controller2 Declaration
public interface IABBS4Controller2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x568DC29C,(short)0x8BD4,(short)0x11D5,new char[]{0xBC,0xA1,0x00,0xD0,0xB7,0xE6,0x16,0x7C});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechUnits getMechUnits() throws com.inzoom.comjni.ComJniException;
  public int getLastError() throws com.inzoom.comjni.ComJniException;
  public int getLastStatus() throws com.inzoom.comjni.ComJniException;
}
