package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface _CommandBarActiveX Declaration
public interface _CommandBarActiveX extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C030D,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getControlCLSID() throws com.inzoom.comjni.ComJniException;
  public void setControlCLSID(String pbstrClsid) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown getQueryControlInterface(String bstrIid) throws com.inzoom.comjni.ComJniException;
  public void setInnerObjectFactory(com.inzoom.comjni.IUnknown pUnk) throws com.inzoom.comjni.ComJniException;
  public void ensureControl() throws com.inzoom.comjni.ComJniException;
  public void setInitWith(com.inzoom.comjni.IUnknown rhs) throws com.inzoom.comjni.ComJniException;
}
