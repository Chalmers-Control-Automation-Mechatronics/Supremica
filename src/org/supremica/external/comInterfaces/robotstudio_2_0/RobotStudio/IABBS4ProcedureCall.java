package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IABBS4ProcedureCall Declaration
public interface IABBS4ProcedureCall extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8E7D4865,(short)0x98F7,(short)0x11D4,new char[]{0xAE,0x1E,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure getParent() throws com.inzoom.comjni.ComJniException;
  public void setModuleName(String rhs) throws com.inzoom.comjni.ComJniException;
  public void setProcedureName(String rhs) throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure getProcedure() throws com.inzoom.comjni.ComJniException;
}
