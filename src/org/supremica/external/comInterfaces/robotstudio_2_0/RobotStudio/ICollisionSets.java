package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface ICollisionSets Declaration
public interface ICollisionSets extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xFE75F340,(short)0xE5E8,(short)0x11D3,new char[]{0x80,0xEC,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionSet item(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException;
  public int getCount() throws com.inzoom.comjni.ComJniException;
  public double getNearMiss() throws com.inzoom.comjni.ComJniException;
  public void setNearMiss(double pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getHighlightCollisions() throws com.inzoom.comjni.ComJniException;
  public void setHighlightCollisions(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getStopAtCollision() throws com.inzoom.comjni.ComJniException;
  public void setStopAtCollision(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getGenerateLogFile() throws com.inzoom.comjni.ComJniException;
  public void setGenerateLogFile(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public String getLogFile() throws com.inzoom.comjni.ComJniException;
  public void setLogFile(String pVal) throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getParent() throws com.inzoom.comjni.ComJniException;
  public int getMode() throws com.inzoom.comjni.ComJniException;
  public void setMode(int pVal) throws com.inzoom.comjni.ComJniException;
  public boolean checkCollisions() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionSet add() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getNearMissColor() throws com.inzoom.comjni.ComJniException;
  public void setNearMissColor(com.inzoom.comjni.Variant pVal) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getCollisionColor() throws com.inzoom.comjni.ComJniException;
  public void setCollisionColor(com.inzoom.comjni.Variant pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getGenerateLogToOutputWindow() throws com.inzoom.comjni.ComJniException;
  public void setGenerateLogToOutputWindow(boolean pVal) throws com.inzoom.comjni.ComJniException;
}
