package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IControllers Declaration
public interface IControllers extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x63AB1B1A,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IController item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public void add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IController Controller) throws com.inzoom.comjni.ComJniException;
  public void remove(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IController Controller) throws com.inzoom.comjni.ComJniException;
}
