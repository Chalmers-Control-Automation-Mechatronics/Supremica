package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IWorkObjects Declaration
public interface IWorkObjects extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8D6E0E2F,(short)0x0097,(short)0x11D4,new char[]{0xA1,0xE2,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject3 item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject3 add(com.inzoom.comjni.Variant WorkObject) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject3 add() throws com.inzoom.comjni.ComJniException;
}
