package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IArguments Declaration
public interface IArguments extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x4A663EE5,(short)0xA985,(short)0x11D4,new char[]{0xAE,0x35,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IArgument item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void setParent(com.inzoom.comjni.IDispatch ppDisp) throws com.inzoom.comjni.ComJniException;
  public void setInstance(int rhs) throws com.inzoom.comjni.ComJniException;
}
