package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IEntities Declaration
public interface IEntities extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x82273A11,(short)0x59FA,(short)0x11D3,new char[]{0xAC,0xB2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 getParent() throws com.inzoom.comjni.ComJniException;
}
