package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IController Declaration
public interface IController extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x63AB1A14,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public String getVersion() throws com.inzoom.comjni.ComJniException;
  public void setVersion(String pVal) throws com.inzoom.comjni.ComJniException;
  public String getUniqueName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IIOs getIOs() throws com.inzoom.comjni.ComJniException;
}
