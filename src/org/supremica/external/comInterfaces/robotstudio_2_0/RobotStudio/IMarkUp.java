package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IMarkUp Declaration
public interface IMarkUp extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x51E3B62D,(short)0x3160,(short)0x11D4,new char[]{0x80,0xEE,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException;
  public String getText() throws com.inzoom.comjni.ComJniException;
  public void setText(String pText) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getTextPosition() throws com.inzoom.comjni.ComJniException;
  public void setTextPosition(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition ppPos) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getArrowPosition() throws com.inzoom.comjni.ComJniException;
  public void setArrowPosition(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition ppPos) throws com.inzoom.comjni.ComJniException;
  public boolean getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(boolean pVisible) throws com.inzoom.comjni.ComJniException;
  public void delete() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void setType(int pType) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IOrientation getTextOrientation() throws com.inzoom.comjni.ComJniException;
  public void setTextOrientation(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IOrientation pVal) throws com.inzoom.comjni.ComJniException;
  public double getTextSize() throws com.inzoom.comjni.ComJniException;
  public void setTextSize(double pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getBackground() throws com.inzoom.comjni.ComJniException;
  public void setBackground(boolean pBackground) throws com.inzoom.comjni.ComJniException;
}
