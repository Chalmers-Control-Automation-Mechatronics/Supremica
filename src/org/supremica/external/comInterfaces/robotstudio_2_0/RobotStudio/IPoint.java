package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPoint Declaration
public interface IPoint extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x64D98F21,(short)0xB200,(short)0x11D3,new char[]{0xBF,0x6E,0x00,0xC0,0x4F,0x68,0xDF,0x5A});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public String getUniqueName() throws com.inzoom.comjni.ComJniException;
  public double getX() throws com.inzoom.comjni.ComJniException;
  public void setX(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getY() throws com.inzoom.comjni.ComJniException;
  public void setY(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getZ() throws com.inzoom.comjni.ComJniException;
  public void setZ(double pVal) throws com.inzoom.comjni.ComJniException;
  public int getSize() throws com.inzoom.comjni.ComJniException;
  public void setSize(int pVal) throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
}
