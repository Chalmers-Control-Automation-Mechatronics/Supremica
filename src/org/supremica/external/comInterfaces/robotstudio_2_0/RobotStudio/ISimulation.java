package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ISimulation Declaration
public interface ISimulation extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x63AB1A06,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public double getResolution() throws com.inzoom.comjni.ComJniException;
  public void setResolution(double pVal) throws com.inzoom.comjni.ComJniException;
  public int getState() throws com.inzoom.comjni.ComJniException;
  public double getTime() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IControllers getControllers() throws com.inzoom.comjni.ComJniException;
  public void pause() throws com.inzoom.comjni.ComJniException;
  public void start() throws com.inzoom.comjni.ComJniException;
  public void stop() throws com.inzoom.comjni.ComJniException;
  public double trySetTime(double Time) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getParent() throws com.inzoom.comjni.ComJniException;
}
