package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface FillFormat Declaration
public interface FillFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0314,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void background() throws com.inzoom.comjni.ComJniException;
  public void oneColorGradient(int Style,int Variant,float Degree) throws com.inzoom.comjni.ComJniException;
  public void patterned(int Pattern) throws com.inzoom.comjni.ComJniException;
  public void presetGradient(int Style,int Variant,int PresetGradientType) throws com.inzoom.comjni.ComJniException;
  public void presetTextured(int PresetTexture) throws com.inzoom.comjni.ComJniException;
  public void solid() throws com.inzoom.comjni.ComJniException;
  public void twoColorGradient(int Style,int Variant) throws com.inzoom.comjni.ComJniException;
  public void userPicture(String PictureFile) throws com.inzoom.comjni.ComJniException;
  public void userTextured(String TextureFile) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getBackColor() throws com.inzoom.comjni.ComJniException;
  public void setBackColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat BackColor) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getForeColor() throws com.inzoom.comjni.ComJniException;
  public void setForeColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat ForeColor) throws com.inzoom.comjni.ComJniException;
  public int getGradientColorType() throws com.inzoom.comjni.ComJniException;
  public float getGradientDegree() throws com.inzoom.comjni.ComJniException;
  public int getGradientStyle() throws com.inzoom.comjni.ComJniException;
  public int getGradientVariant() throws com.inzoom.comjni.ComJniException;
  public int getPattern() throws com.inzoom.comjni.ComJniException;
  public int getPresetGradientType() throws com.inzoom.comjni.ComJniException;
  public int getPresetTexture() throws com.inzoom.comjni.ComJniException;
  public String getTextureName() throws com.inzoom.comjni.ComJniException;
  public int getTextureType() throws com.inzoom.comjni.ComJniException;
  public float getTransparency() throws com.inzoom.comjni.ComJniException;
  public void setTransparency(float Transparency) throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public int getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(int Visible) throws com.inzoom.comjni.ComJniException;
}
