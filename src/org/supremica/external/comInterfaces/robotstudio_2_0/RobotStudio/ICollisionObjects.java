package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ICollisionObjects Declaration
public interface ICollisionObjects extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xFE75F2F8,(short)0xE5E8,(short)0x11D3,new char[]{0x80,0xEC,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public void add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject RsObject) throws com.inzoom.comjni.ComJniException;
  public void remove(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject RsObject) throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionSet getParent() throws com.inzoom.comjni.ComJniException;
}
