package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IToolFrame Declaration
public interface IToolFrame extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xF21E9773,(short)0xFF3F,(short)0x11D3,new char[]{0xA1,0xE2,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getTransform() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public double getMass() throws com.inzoom.comjni.ComJniException;
  public void setMass(double pVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getCenterOfGravity() throws com.inzoom.comjni.ComJniException;
  public void setCenterOfGravity(com.inzoom.comjni.Variant pXYZ) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getAxisOfMoment() throws com.inzoom.comjni.ComJniException;
  public void setAxisOfMoment(com.inzoom.comjni.Variant pXYZW) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getInertia() throws com.inzoom.comjni.ComJniException;
  public void setInertia(com.inzoom.comjni.Variant pXYZ) throws com.inzoom.comjni.ComJniException;
  public boolean getRobHold() throws com.inzoom.comjni.ComJniException;
  public void setRobHold(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues getExternalAxesValues() throws com.inzoom.comjni.ComJniException;
  public void setExternalAxesValues(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues pVal) throws com.inzoom.comjni.ComJniException;
}
