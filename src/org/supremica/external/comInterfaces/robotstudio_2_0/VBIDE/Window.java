package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// interface Window Declaration
public interface Window extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x0002E16B,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Windows getCollection() throws com.inzoom.comjni.ComJniException;
  public void close() throws com.inzoom.comjni.ComJniException;
  public String getCaption() throws com.inzoom.comjni.ComJniException;
  public boolean getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(boolean pfVisible) throws com.inzoom.comjni.ComJniException;
  public int getLeft() throws com.inzoom.comjni.ComJniException;
  public void setLeft(int plLeft) throws com.inzoom.comjni.ComJniException;
  public int getTop() throws com.inzoom.comjni.ComJniException;
  public void setTop(int plTop) throws com.inzoom.comjni.ComJniException;
  public int getWidth() throws com.inzoom.comjni.ComJniException;
  public void setWidth(int plWidth) throws com.inzoom.comjni.ComJniException;
  public int getHeight() throws com.inzoom.comjni.ComJniException;
  public void setHeight(int plHeight) throws com.inzoom.comjni.ComJniException;
  public int getWindowState() throws com.inzoom.comjni.ComJniException;
  public void setWindowState(int plWindowState) throws com.inzoom.comjni.ComJniException;
  public void setFocus() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void setKind(int eKind) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.LinkedWindows getLinkedWindows() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getLinkedWindowFrame() throws com.inzoom.comjni.ComJniException;
  public void detach() throws com.inzoom.comjni.ComJniException;
  public void attach(int lWindowHandle) throws com.inzoom.comjni.ComJniException;
  public int getHWnd() throws com.inzoom.comjni.ComJniException;
}
