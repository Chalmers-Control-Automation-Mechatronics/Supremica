package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface COMAddIn Declaration
public interface COMAddIn extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C033A,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getDescription() throws com.inzoom.comjni.ComJniException;
  public void setDescription(String RetValue) throws com.inzoom.comjni.ComJniException;
  public String getProgId() throws com.inzoom.comjni.ComJniException;
  public String getGuid() throws com.inzoom.comjni.ComJniException;
  public boolean getConnect() throws com.inzoom.comjni.ComJniException;
  public void setConnect(boolean RetValue) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getObject() throws com.inzoom.comjni.ComJniException;
  public void setObject(com.inzoom.comjni.IDispatch RetValue) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
}
