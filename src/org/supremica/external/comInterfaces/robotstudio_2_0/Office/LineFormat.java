package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface LineFormat Declaration
public interface LineFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0317,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getBackColor() throws com.inzoom.comjni.ComJniException;
  public void setBackColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat BackColor) throws com.inzoom.comjni.ComJniException;
  public int getBeginArrowheadLength() throws com.inzoom.comjni.ComJniException;
  public void setBeginArrowheadLength(int BeginArrowheadLength) throws com.inzoom.comjni.ComJniException;
  public int getBeginArrowheadStyle() throws com.inzoom.comjni.ComJniException;
  public void setBeginArrowheadStyle(int BeginArrowheadStyle) throws com.inzoom.comjni.ComJniException;
  public int getBeginArrowheadWidth() throws com.inzoom.comjni.ComJniException;
  public void setBeginArrowheadWidth(int BeginArrowheadWidth) throws com.inzoom.comjni.ComJniException;
  public int getDashStyle() throws com.inzoom.comjni.ComJniException;
  public void setDashStyle(int DashStyle) throws com.inzoom.comjni.ComJniException;
  public int getEndArrowheadLength() throws com.inzoom.comjni.ComJniException;
  public void setEndArrowheadLength(int EndArrowheadLength) throws com.inzoom.comjni.ComJniException;
  public int getEndArrowheadStyle() throws com.inzoom.comjni.ComJniException;
  public void setEndArrowheadStyle(int EndArrowheadStyle) throws com.inzoom.comjni.ComJniException;
  public int getEndArrowheadWidth() throws com.inzoom.comjni.ComJniException;
  public void setEndArrowheadWidth(int EndArrowheadWidth) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getForeColor() throws com.inzoom.comjni.ComJniException;
  public void setForeColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat ForeColor) throws com.inzoom.comjni.ComJniException;
  public int getPattern() throws com.inzoom.comjni.ComJniException;
  public void setPattern(int Pattern) throws com.inzoom.comjni.ComJniException;
  public int getStyle() throws com.inzoom.comjni.ComJniException;
  public void setStyle(int Style) throws com.inzoom.comjni.ComJniException;
  public float getTransparency() throws com.inzoom.comjni.ComJniException;
  public void setTransparency(float Transparency) throws com.inzoom.comjni.ComJniException;
  public int getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(int Visible) throws com.inzoom.comjni.ComJniException;
  public float getWeight() throws com.inzoom.comjni.ComJniException;
  public void setWeight(float Weight) throws com.inzoom.comjni.ComJniException;
}
