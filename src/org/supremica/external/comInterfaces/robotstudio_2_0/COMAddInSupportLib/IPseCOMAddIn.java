package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib;

// interface IPseCOMAddIn Declaration
public interface IPseCOMAddIn extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x5052EF14,(short)0x5688,(short)0x11D3,new char[]{0x80,0xD2,0x00,0x50,0x04,0x29,0xD1,0x09});
  public String getDescription() throws com.inzoom.comjni.ComJniException;
  public void setDescription(String pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getConnect() throws com.inzoom.comjni.ComJniException;
  public void setConnect(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public String getGuid() throws com.inzoom.comjni.ComJniException;
  public void setGuid(String pVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void setParent(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException;
  public String getProgID() throws com.inzoom.comjni.ComJniException;
  public void setProgID(String pVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getObject() throws com.inzoom.comjni.ComJniException;
  public void setObject(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException;
  public String getFriendlyName() throws com.inzoom.comjni.ComJniException;
  public void setFriendlyName(String pVal) throws com.inzoom.comjni.ComJniException;
  public int getLoadBehavior() throws com.inzoom.comjni.ComJniException;
  public void setLoadBehavior(int pVal) throws com.inzoom.comjni.ComJniException;
  public String getFileName() throws com.inzoom.comjni.ComJniException;
  public void setFileName(String pVal) throws com.inzoom.comjni.ComJniException;
  public int getRestricted() throws com.inzoom.comjni.ComJniException;
  public void setRestricted(int pVal) throws com.inzoom.comjni.ComJniException;
}
