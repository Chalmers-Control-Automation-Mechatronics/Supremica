package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface _CommandBarButton Declaration
public interface _CommandBarButton extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C030E,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public boolean getBuiltInFace() throws com.inzoom.comjni.ComJniException;
  public void setBuiltInFace(boolean pvarfBuiltIn) throws com.inzoom.comjni.ComJniException;
  public void copyFace() throws com.inzoom.comjni.ComJniException;
  public int getFaceId() throws com.inzoom.comjni.ComJniException;
  public void setFaceId(int pid) throws com.inzoom.comjni.ComJniException;
  public void pasteFace() throws com.inzoom.comjni.ComJniException;
  public String getShortcutText() throws com.inzoom.comjni.ComJniException;
  public void setShortcutText(String pbstrText) throws com.inzoom.comjni.ComJniException;
  public int getState() throws com.inzoom.comjni.ComJniException;
  public void setState(int pstate) throws com.inzoom.comjni.ComJniException;
  public int getStyle() throws com.inzoom.comjni.ComJniException;
  public void setStyle(int pstyle) throws com.inzoom.comjni.ComJniException;
  public int getHyperlinkType() throws com.inzoom.comjni.ComJniException;
  public void setHyperlinkType(int phlType) throws com.inzoom.comjni.ComJniException;
}
