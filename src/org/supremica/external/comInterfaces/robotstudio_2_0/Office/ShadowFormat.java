package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ShadowFormat Declaration
public interface ShadowFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C031B,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void incrementOffsetX(float Increment) throws com.inzoom.comjni.ComJniException;
  public void incrementOffsetY(float Increment) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getForeColor() throws com.inzoom.comjni.ComJniException;
  public void setForeColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat ForeColor) throws com.inzoom.comjni.ComJniException;
  public int getObscured() throws com.inzoom.comjni.ComJniException;
  public void setObscured(int Obscured) throws com.inzoom.comjni.ComJniException;
  public float getOffsetX() throws com.inzoom.comjni.ComJniException;
  public void setOffsetX(float OffsetX) throws com.inzoom.comjni.ComJniException;
  public float getOffsetY() throws com.inzoom.comjni.ComJniException;
  public void setOffsetY(float OffsetY) throws com.inzoom.comjni.ComJniException;
  public float getTransparency() throws com.inzoom.comjni.ComJniException;
  public void setTransparency(float Transparency) throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void setType(int Type) throws com.inzoom.comjni.ComJniException;
  public int getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(int Visible) throws com.inzoom.comjni.ComJniException;
}
