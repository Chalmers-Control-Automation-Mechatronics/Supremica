package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ISelectionLevel Declaration
public interface ISelectionLevel extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xC02A25DA,(short)0xE9C2,(short)0x11D3,new char[]{0xAD,0x4F,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getParent() throws com.inzoom.comjni.ComJniException;
  public boolean getBuiltIn() throws com.inzoom.comjni.ComJniException;
  public void setBuiltIn(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public String getAttributeName() throws com.inzoom.comjni.ComJniException;
  public void setAttributeName(String pVal) throws com.inzoom.comjni.ComJniException;
  public String getAttributeValue() throws com.inzoom.comjni.ComJniException;
  public void setAttributeValue(String pVal) throws com.inzoom.comjni.ComJniException;
  public void setSelectionLevels(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevels rhs) throws com.inzoom.comjni.ComJniException;
  public boolean getActive() throws com.inzoom.comjni.ComJniException;
  public void setActive(boolean pVal) throws com.inzoom.comjni.ComJniException;
}
