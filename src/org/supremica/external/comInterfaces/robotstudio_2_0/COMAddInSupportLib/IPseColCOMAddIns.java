package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib;

// interface IPseColCOMAddIns Declaration
public interface IPseColCOMAddIns extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x5052EF12,(short)0x5688,(short)0x11D3,new char[]{0x80,0xD2,0x00,0x50,0x04,0x29,0xD1,0x09});
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public void update() throws com.inzoom.comjni.ComJniException;
  public String getRegistryPath() throws com.inzoom.comjni.ComJniException;
  public void setRegistryPath(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager getAddInManager() throws com.inzoom.comjni.ComJniException;
  public void setAddInManager(org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager pVal) throws com.inzoom.comjni.ComJniException;
  public void beginShutdown() throws com.inzoom.comjni.ComJniException;
  public void shutdown() throws com.inzoom.comjni.ComJniException;
  public void startupComplete() throws com.inzoom.comjni.ComJniException;
  public void startup() throws com.inzoom.comjni.ComJniException;
  public void addInUpdate(org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn pAddIn) throws com.inzoom.comjni.ComJniException;
}
