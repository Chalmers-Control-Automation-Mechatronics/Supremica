package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IABBS4Procedure Declaration
public interface IABBS4Procedure extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xA1DB06F4,(short)0x95D8,(short)0x11D4,new char[]{0xAE,0x1B,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Module getParent() throws com.inzoom.comjni.ComJniException;
  public void setModuleName(String rhs) throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCalls getProcedureCalls() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public void execute() throws com.inzoom.comjni.ComJniException;
  public void refresh() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getPath() throws com.inzoom.comjni.ComJniException;
  public void syncToStation() throws com.inzoom.comjni.ComJniException;
}
