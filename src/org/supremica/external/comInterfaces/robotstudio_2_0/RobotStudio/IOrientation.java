package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IOrientation Declaration
public interface IOrientation extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x84F85ADE,(short)0x263D,(short)0x11D4,new char[]{0xAD,0x96,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public com.inzoom.comjni.IDispatch getNode() throws com.inzoom.comjni.ComJniException;
  public void setNode(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException;
  public double getRx() throws com.inzoom.comjni.ComJniException;
  public void setRx(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getRy() throws com.inzoom.comjni.ComJniException;
  public void setRy(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getRz() throws com.inzoom.comjni.ComJniException;
  public void setRz(double pVal) throws com.inzoom.comjni.ComJniException;
  public void setListener(int pListener,int nCookie) throws com.inzoom.comjni.ComJniException;
}
