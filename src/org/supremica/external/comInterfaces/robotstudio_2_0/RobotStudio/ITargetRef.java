package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ITargetRef Declaration
public interface ITargetRef extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8080E64A,(short)0x7D66,(short)0x11D3,new char[]{0xAC,0xD5,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public String getUniqueName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 getTarget() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException;
  public int getMotionType() throws com.inzoom.comjni.ComJniException;
  public void setMotionType(int pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame2 getToolFrame() throws com.inzoom.comjni.ComJniException;
  public void setToolFrame(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IToolFrame pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getPath() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 getViaTarget() throws com.inzoom.comjni.ComJniException;
  public void convertToCircular(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef ViaTargetRef) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessType getProcessType() throws com.inzoom.comjni.ComJniException;
  public void setProcessType(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IProcessType ppVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException;
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException;
}
