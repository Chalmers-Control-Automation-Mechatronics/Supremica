package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface VBE Declaration
public interface VBE extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Application {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E166,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProjects getVBProjects() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBars getCommandBars() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePanes getCodePanes() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Windows getWindows() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Events getEvents() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject getActiveVBProject() throws com.inzoom.comjni.ComJniException;
  public void setActiveVBProject(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject lppptReturn) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent getSelectedVBComponent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getMainWindow() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getActiveWindow() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane getActiveCodePane() throws com.inzoom.comjni.ComJniException;
  public void setActiveCodePane(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane ppCodePane) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Addins getAddins() throws com.inzoom.comjni.ComJniException;
}
