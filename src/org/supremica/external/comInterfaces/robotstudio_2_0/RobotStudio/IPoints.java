package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPoints Declaration
public interface IPoints extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x64D98F1F,(short)0xB200,(short)0x11D3,new char[]{0xBF,0x6E,0x00,0xC0,0x4F,0x68,0xDF,0x5A});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPoint item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPoint add(com.inzoom.comjni.Variant Object) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPoint add() throws com.inzoom.comjni.ComJniException;
}
