package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IArgument Declaration
public interface IArgument extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x4A663EE7,(short)0xA985,(short)0x11D4,new char[]{0xAE,0x35,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void setParent(com.inzoom.comjni.IDispatch ppDisp) throws com.inzoom.comjni.ComJniException;
  public void setInstance(int rhs) throws com.inzoom.comjni.ComJniException;
  public boolean getOptional() throws com.inzoom.comjni.ComJniException;
  public boolean getEnabled() throws com.inzoom.comjni.ComJniException;
  public void setEnabled(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public String getValue() throws com.inzoom.comjni.ComJniException;
  public void setValue(String pVal) throws com.inzoom.comjni.ComJniException;
}
