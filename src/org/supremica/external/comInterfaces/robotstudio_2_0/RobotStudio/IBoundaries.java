package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IBoundaries Declaration
public interface IBoundaries extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x092DBA49,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundary item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFace2 getParent() throws com.inzoom.comjni.ComJniException;
}
