package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IABBS4Procedures Declaration
public interface IABBS4Procedures extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xA1DB06F2,(short)0x95D8,(short)0x11D4,new char[]{0xAE,0x1B,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public void setModuleName(String rhs) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Module getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure add(com.inzoom.comjni.Variant Name,boolean IsMain) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure add(com.inzoom.comjni.Variant Name) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure add() throws com.inzoom.comjni.ComJniException;
  public void insert(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath pPath) throws com.inzoom.comjni.ComJniException;
}
