package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IApplication3 Declaration
public interface IApplication3 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2 {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x647AF24E,(short)0x8281,(short)0x11D5,new char[]{0xBC,0x9E,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public com.inzoom.comjni.IDispatch addBrowserTab(String CLSID,String TabName,com.inzoom.comjni.IDispatch Icon,String LicenseString) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch addBrowserTab(String CLSID,String TabName,com.inzoom.comjni.IDispatch Icon) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch addBrowserTab(String CLSID,String TabName) throws com.inzoom.comjni.ComJniException;
  public void removeBrowserTab(com.inzoom.comjni.Variant Tab) throws com.inzoom.comjni.ComJniException;
  public void setActiveBrowserTab(com.inzoom.comjni.Variant Tab) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getActiveBrowserTab() throws com.inzoom.comjni.ComJniException;
}
