package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface WebPageFont Declaration
public interface WebPageFont extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0913,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public String getProportionalFont() throws com.inzoom.comjni.ComJniException;
  public void setProportionalFont(String pstr) throws com.inzoom.comjni.ComJniException;
  public float getProportionalFontSize() throws com.inzoom.comjni.ComJniException;
  public void setProportionalFontSize(float pf) throws com.inzoom.comjni.ComJniException;
  public String getFixedWidthFont() throws com.inzoom.comjni.ComJniException;
  public void setFixedWidthFont(String pstr) throws com.inzoom.comjni.ComJniException;
  public float getFixedWidthFontSize() throws com.inzoom.comjni.ComJniException;
  public void setFixedWidthFontSize(float pf) throws com.inzoom.comjni.ComJniException;
}
