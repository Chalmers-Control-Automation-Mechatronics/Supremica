package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ITransform Declaration
public interface ITransform extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x7EB93CA8,(short)0x218D,(short)0x11D4,new char[]{0xAD,0x90,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public com.inzoom.comjni.IDispatch getNode() throws com.inzoom.comjni.ComJniException;
  public void setNode(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException;
  public double getX() throws com.inzoom.comjni.ComJniException;
  public void setX(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getY() throws com.inzoom.comjni.ComJniException;
  public void setY(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getZ() throws com.inzoom.comjni.ComJniException;
  public void setZ(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getRx() throws com.inzoom.comjni.ComJniException;
  public void setRx(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getRy() throws com.inzoom.comjni.ComJniException;
  public void setRy(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getRz() throws com.inzoom.comjni.ComJniException;
  public void setRz(double pVal) throws com.inzoom.comjni.ComJniException;
  public void setListener(int pListener,int nCookie) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getPosition() throws com.inzoom.comjni.ComJniException;
  public void setPosition(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IOrientation getOrientation() throws com.inzoom.comjni.ComJniException;
  public void setOrientation(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IOrientation pVal) throws com.inzoom.comjni.ComJniException;
}
