package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ColorFormat Declaration
public interface ColorFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0312,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public int getRGB() throws com.inzoom.comjni.ComJniException;
  public void setRGB(int RGB) throws com.inzoom.comjni.ComJniException;
  public int getSchemeColor() throws com.inzoom.comjni.ComJniException;
  public void setSchemeColor(int SchemeColor) throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
}
