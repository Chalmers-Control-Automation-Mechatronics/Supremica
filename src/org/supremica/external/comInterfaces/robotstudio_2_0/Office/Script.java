package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface Script Declaration
public interface Script extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0341,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public String getExtended() throws com.inzoom.comjni.ComJniException;
  public void setExtended(String Extended) throws com.inzoom.comjni.ComJniException;
  public String getId() throws com.inzoom.comjni.ComJniException;
  public void setId(String Id) throws com.inzoom.comjni.ComJniException;
  public int getLanguage() throws com.inzoom.comjni.ComJniException;
  public void setLanguage(int Language) throws com.inzoom.comjni.ComJniException;
  public int getLocation() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getShape() throws com.inzoom.comjni.ComJniException;
  public String getScriptText() throws com.inzoom.comjni.ComJniException;
  public void setScriptText(String Script) throws com.inzoom.comjni.ComJniException;
}
