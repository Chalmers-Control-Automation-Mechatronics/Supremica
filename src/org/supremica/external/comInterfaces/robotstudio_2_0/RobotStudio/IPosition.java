package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPosition Declaration
public interface IPosition extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x86593371,(short)0x7B03,(short)0x11D3,new char[]{0xAC,0xD3,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public com.inzoom.comjni.IDispatch getNode() throws com.inzoom.comjni.ComJniException;
  public void setNode(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException;
  public double getX() throws com.inzoom.comjni.ComJniException;
  public void setX(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getY() throws com.inzoom.comjni.ComJniException;
  public void setY(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getZ() throws com.inzoom.comjni.ComJniException;
  public void setZ(double pVal) throws com.inzoom.comjni.ComJniException;
  public void setListener(int pListener,int nCookie) throws com.inzoom.comjni.ComJniException;
}
