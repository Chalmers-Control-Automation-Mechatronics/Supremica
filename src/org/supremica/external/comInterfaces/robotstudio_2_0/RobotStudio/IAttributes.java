package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IAttributes Declaration
public interface IAttributes extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xF1D0FF05,(short)0xE2C8,(short)0x11D3,new char[]{0xAD,0x47,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttribute item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttribute add(String key,com.inzoom.comjni.Variant Value,boolean DoCopy) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttribute add(String key,com.inzoom.comjni.Variant Value) throws com.inzoom.comjni.ComJniException;
  public void remove(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
}
