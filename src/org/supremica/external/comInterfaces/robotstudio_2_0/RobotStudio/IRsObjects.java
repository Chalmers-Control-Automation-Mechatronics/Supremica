package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IRsObjects Declaration
public interface IRsObjects extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8A96F013,(short)0xE542,(short)0x11D3,new char[]{0x80,0xE6,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
}
