package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IWorkObject Declaration
public interface IWorkObject extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xF21E9771,(short)0xFF3F,(short)0x11D3,new char[]{0xA1,0xE2,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public boolean getRobHold() throws com.inzoom.comjni.ComJniException;
  public void setRobHold(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getProgrammed() throws com.inzoom.comjni.ComJniException;
  public void setProgrammed(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public String getMechanicalUnit() throws com.inzoom.comjni.ComJniException;
  public void setMechanicalUnit(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargets getTargets() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getUserFrame() throws com.inzoom.comjni.ComJniException;
  public void setUserFrame(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform ppTransform) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getObjectFrame() throws com.inzoom.comjni.ComJniException;
  public void setObjectFrame(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform ppTransform) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues getExternalAxesValues() throws com.inzoom.comjni.ComJniException;
  public void setExternalAxesValues(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues pVal) throws com.inzoom.comjni.ComJniException;
}
