package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IABBS4ProcedureCalls Declaration
public interface IABBS4ProcedureCalls extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8E7D4867,(short)0x98F7,(short)0x11D4,new char[]{0xAE,0x1E,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCall item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public void setModuleName(String rhs) throws com.inzoom.comjni.ComJniException;
  public void setProcedureName(String rhs) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCall add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure Procedure,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCall ProcedureCall) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCall add(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure Procedure) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4ProcedureCall add() throws com.inzoom.comjni.ComJniException;
}
