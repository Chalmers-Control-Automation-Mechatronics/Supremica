package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface HTMLProject Declaration
public interface HTMLProject extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0356,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public int getState() throws com.inzoom.comjni.ComJniException;
  public void refreshProject(boolean Refresh) throws com.inzoom.comjni.ComJniException;
  public void refreshProject() throws com.inzoom.comjni.ComJniException;
  public void refreshDocument(boolean Refresh) throws com.inzoom.comjni.ComJniException;
  public void refreshDocument() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.HTMLProjectItems getHTMLProjectItems() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void open(int OpenKind) throws com.inzoom.comjni.ComJniException;
  public void open() throws com.inzoom.comjni.ComJniException;
}
