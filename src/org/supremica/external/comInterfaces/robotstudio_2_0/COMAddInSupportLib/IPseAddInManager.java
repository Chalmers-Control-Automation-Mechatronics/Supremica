package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib;

// interface IPseAddInManager Declaration
public interface IPseAddInManager extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x5052EF0D,(short)0x5688,(short)0x11D3,new char[]{0x80,0xD2,0x00,0x50,0x04,0x29,0xD1,0x09});
  public org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns getCOMAddIns() throws com.inzoom.comjni.ComJniException;
  public void doModal() throws com.inzoom.comjni.ComJniException;
  public String getRegistryPath() throws com.inzoom.comjni.ComJniException;
  public void setRegistryPath(String pVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException;
  public void setApplication(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException;
  public void startup() throws com.inzoom.comjni.ComJniException;
  public void startupComplete() throws com.inzoom.comjni.ComJniException;
  public void shutdown() throws com.inzoom.comjni.ComJniException;
  public int getConnectMode() throws com.inzoom.comjni.ComJniException;
  public int getDisconnectMode() throws com.inzoom.comjni.ComJniException;
  public void beginShutdown() throws com.inzoom.comjni.ComJniException;
  public boolean getAllowUnload() throws com.inzoom.comjni.ComJniException;
  public void setAllowUnload(boolean pVal) throws com.inzoom.comjni.ComJniException;
}
