package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IAttachments Declaration
public interface IAttachments extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xC59DDBA0,(short)0x2717,(short)0x11D4,new char[]{0xAD,0x97,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public com.inzoom.comjni.IDispatch item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject RsObject,com.inzoom.comjni.Variant KeepCurrentPosition) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject RsObject) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject remove(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
}
