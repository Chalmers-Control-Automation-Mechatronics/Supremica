package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IApplication Declaration
public interface IApplication extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x282D0CBD,(short)0x0771,(short)0x11D3,new char[]{0xAC,0x7A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkspace getWorkspace() throws com.inzoom.comjni.ComJniException;
  public void fireSelectionChangedEvent() throws com.inzoom.comjni.ComJniException;
  public String getCaption() throws com.inzoom.comjni.ComJniException;
  public void setCaption(String pVal) throws com.inzoom.comjni.ComJniException;
  public String getBuild() throws com.inzoom.comjni.ComJniException;
  public String getVersion() throws com.inzoom.comjni.ComJniException;
  public int getHeight() throws com.inzoom.comjni.ComJniException;
  public void setHeight(int pVal) throws com.inzoom.comjni.ComJniException;
  public int getLeft() throws com.inzoom.comjni.ComJniException;
  public void setLeft(int pVal) throws com.inzoom.comjni.ComJniException;
  public int getTop() throws com.inzoom.comjni.ComJniException;
  public void setTop(int pVal) throws com.inzoom.comjni.ComJniException;
  public int getWidth() throws com.inzoom.comjni.ComJniException;
  public void setWidth(int pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public int getWindowState() throws com.inzoom.comjni.ComJniException;
  public void setWindowState(int pVal) throws com.inzoom.comjni.ComJniException;
  public void quit(boolean SaveChanges) throws com.inzoom.comjni.ComJniException;
  public void quit() throws com.inzoom.comjni.ComJniException;
  public void fireQuitEvent() throws com.inzoom.comjni.ComJniException;
  public void fireStationChangeEvent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISelectionLevels getSelectionLevels() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2 getUserOptions() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getActiveStation() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView getActiveView() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns getCOMAddIns() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSE getRSE() throws com.inzoom.comjni.ComJniException;
  public int getHWnd() throws com.inzoom.comjni.ComJniException;
  public void setActiveView(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView ppView) throws com.inzoom.comjni.ComJniException;
  public void importSettingsFile(String FileName) throws com.inzoom.comjni.ComJniException;
  public void exportSettingsFile(String FileName) throws com.inzoom.comjni.ComJniException;
  public void exportBmpFile(String FileName) throws com.inzoom.comjni.ComJniException;
  public int checkLicense(String Feature,String Version,String[] Options) throws com.inzoom.comjni.ComJniException;
}
