package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPseProxyAdm Declaration
public interface IPseProxyAdm extends com.inzoom.comjni.IUnknown {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x3D999F36,(short)0x9684,(short)0x11D3,new char[]{0x80,0xB7,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void attach(int pNode) throws com.inzoom.comjni.ComJniException;
  public int detach() throws com.inzoom.comjni.ComJniException;
  public void setApp(int pApp) throws com.inzoom.comjni.ComJniException;
  public int getPtr_() throws com.inzoom.comjni.ComJniException;
}
