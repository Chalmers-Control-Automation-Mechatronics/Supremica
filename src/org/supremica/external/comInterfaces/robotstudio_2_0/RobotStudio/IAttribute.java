package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IAttribute Declaration
public interface IAttribute extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x3E4EC4EE,(short)0xE50E,(short)0x11D3,new char[]{0xAD,0x4B,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getValue() throws com.inzoom.comjni.ComJniException;
  public void setValue(com.inzoom.comjni.Variant pVal) throws com.inzoom.comjni.ComJniException;
  public void setAttributes(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes rhs) throws com.inzoom.comjni.ComJniException;
  public boolean getDoCopy() throws com.inzoom.comjni.ComJniException;
  public void setDoCopy(boolean pVal) throws com.inzoom.comjni.ComJniException;
}
