package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPath Declaration
public interface IPath extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xEEC147A5,(short)0x5164,(short)0x11D3,new char[]{0xAC,0xA6,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public String getUniqueName() throws com.inzoom.comjni.ComJniException;
  public boolean getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRefs getTargetRefs() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 insert(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target,int order,int Index) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 insert(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target,int order) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 insert(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target) throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException;
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure getProcedure() throws com.inzoom.comjni.ComJniException;
  public void refresh() throws com.inzoom.comjni.ComJniException;
  public void syncToVirtualController(String ModuleName) throws com.inzoom.comjni.ComJniException;
}
