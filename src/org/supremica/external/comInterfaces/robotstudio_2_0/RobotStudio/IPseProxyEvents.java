package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPseProxyEvents Declaration
public interface IPseProxyEvents extends com.inzoom.comjni.IUnknown {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x902FC2A7,(short)0xC2B9,(short)0x11D3,new char[]{0x80,0xC0,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void fireSelected() throws com.inzoom.comjni.ComJniException;
  public void fireUnSelected() throws com.inzoom.comjni.ComJniException;
  public void fireChanged(int Type) throws com.inzoom.comjni.ComJniException;
  public void fireCollisionStart(com.inzoom.comjni.IUnknown Obj) throws com.inzoom.comjni.ComJniException;
  public void fireCollisionEnd(com.inzoom.comjni.IUnknown Obj) throws com.inzoom.comjni.ComJniException;
}
