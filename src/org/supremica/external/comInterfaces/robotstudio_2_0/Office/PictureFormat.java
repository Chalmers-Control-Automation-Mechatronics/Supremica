package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface PictureFormat Declaration
public interface PictureFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C031A,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void incrementBrightness(float Increment) throws com.inzoom.comjni.ComJniException;
  public void incrementContrast(float Increment) throws com.inzoom.comjni.ComJniException;
  public float getBrightness() throws com.inzoom.comjni.ComJniException;
  public void setBrightness(float Brightness) throws com.inzoom.comjni.ComJniException;
  public int getColorType() throws com.inzoom.comjni.ComJniException;
  public void setColorType(int ColorType) throws com.inzoom.comjni.ComJniException;
  public float getContrast() throws com.inzoom.comjni.ComJniException;
  public void setContrast(float Contrast) throws com.inzoom.comjni.ComJniException;
  public float getCropBottom() throws com.inzoom.comjni.ComJniException;
  public void setCropBottom(float CropBottom) throws com.inzoom.comjni.ComJniException;
  public float getCropLeft() throws com.inzoom.comjni.ComJniException;
  public void setCropLeft(float CropLeft) throws com.inzoom.comjni.ComJniException;
  public float getCropRight() throws com.inzoom.comjni.ComJniException;
  public void setCropRight(float CropRight) throws com.inzoom.comjni.ComJniException;
  public float getCropTop() throws com.inzoom.comjni.ComJniException;
  public void setCropTop(float CropTop) throws com.inzoom.comjni.ComJniException;
  public int getTransparencyColor() throws com.inzoom.comjni.ComJniException;
  public void setTransparencyColor(int TransparencyColor) throws com.inzoom.comjni.ComJniException;
  public int getTransparentBackground() throws com.inzoom.comjni.ComJniException;
  public void setTransparentBackground(int TransparentBackground) throws com.inzoom.comjni.ComJniException;
}
