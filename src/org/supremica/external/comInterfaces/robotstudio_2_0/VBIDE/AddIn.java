package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface AddIn Declaration
public interface AddIn extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xDA936B64,(short)0xAC8B,(short)0x11D1,new char[]{0xB6,0xE5,0x00,0xA0,0xC9,0x0F,0x27,0x44});
  public String getDescription() throws com.inzoom.comjni.ComJniException;
  public void setDescription(String lpbstr) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Addins getCollection() throws com.inzoom.comjni.ComJniException;
  public String getProgId() throws com.inzoom.comjni.ComJniException;
  public String getGuid() throws com.inzoom.comjni.ComJniException;
  public boolean getConnect() throws com.inzoom.comjni.ComJniException;
  public void setConnect(boolean lpfConnect) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getObject() throws com.inzoom.comjni.ComJniException;
  public void setObject(com.inzoom.comjni.IDispatch lppdisp) throws com.inzoom.comjni.ComJniException;
}
