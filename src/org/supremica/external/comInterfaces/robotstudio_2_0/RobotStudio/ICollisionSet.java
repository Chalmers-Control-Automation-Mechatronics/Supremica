package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ICollisionSet Declaration
public interface ICollisionSet extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8422D9CB,(short)0xE530,(short)0x11D3,new char[]{0x80,0xEA,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getActive() throws com.inzoom.comjni.ComJniException;
  public void setActive(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public double getNearMiss() throws com.inzoom.comjni.ComJniException;
  public void setNearMiss(double pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionObjects getObjectsA() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionObjects getObjectsB() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getParent() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
}
