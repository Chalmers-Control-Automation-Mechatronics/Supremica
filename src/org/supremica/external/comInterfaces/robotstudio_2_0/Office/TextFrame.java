package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface TextFrame Declaration
public interface TextFrame extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0320,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public float getMarginBottom() throws com.inzoom.comjni.ComJniException;
  public void setMarginBottom(float MarginBottom) throws com.inzoom.comjni.ComJniException;
  public float getMarginLeft() throws com.inzoom.comjni.ComJniException;
  public void setMarginLeft(float MarginLeft) throws com.inzoom.comjni.ComJniException;
  public float getMarginRight() throws com.inzoom.comjni.ComJniException;
  public void setMarginRight(float MarginRight) throws com.inzoom.comjni.ComJniException;
  public float getMarginTop() throws com.inzoom.comjni.ComJniException;
  public void setMarginTop(float MarginTop) throws com.inzoom.comjni.ComJniException;
  public int getOrientation() throws com.inzoom.comjni.ComJniException;
  public void setOrientation(int Orientation) throws com.inzoom.comjni.ComJniException;
}
