package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface _IPseDynClassInternal Declaration
public interface _IPseDynClassInternal extends com.inzoom.comjni.IUnknown {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xA01D8332,(short)0x91E3,(short)0x11D3,new char[]{0x80,0xB6,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void deleteInstance(com.inzoom.comjni.IDispatch pDispatch) throws com.inzoom.comjni.ComJniException;
  public void addEvent(String bstrName) throws com.inzoom.comjni.ComJniException;
  public void removeEvent(String bstrName) throws com.inzoom.comjni.ComJniException;
  public void fireEvent(String bstrName) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch createInstance() throws com.inzoom.comjni.ComJniException;
  public int getTypeID() throws com.inzoom.comjni.ComJniException;
  public void initNew(int pProj,String sClassName) throws com.inzoom.comjni.ComJniException;
  public void register(int pProj,com.inzoom.comjni.IDispatch pHostObject,String sClassName) throws com.inzoom.comjni.ComJniException;
  public void deleteClassObject() throws com.inzoom.comjni.ComJniException;
}
