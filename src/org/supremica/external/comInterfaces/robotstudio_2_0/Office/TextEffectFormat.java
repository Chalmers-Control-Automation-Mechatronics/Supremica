package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface TextEffectFormat Declaration
public interface TextEffectFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C031F,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void toggleVerticalText() throws com.inzoom.comjni.ComJniException;
  public int getAlignment() throws com.inzoom.comjni.ComJniException;
  public void setAlignment(int Alignment) throws com.inzoom.comjni.ComJniException;
  public int getFontBold() throws com.inzoom.comjni.ComJniException;
  public void setFontBold(int FontBold) throws com.inzoom.comjni.ComJniException;
  public int getFontItalic() throws com.inzoom.comjni.ComJniException;
  public void setFontItalic(int FontItalic) throws com.inzoom.comjni.ComJniException;
  public String getFontName() throws com.inzoom.comjni.ComJniException;
  public void setFontName(String FontName) throws com.inzoom.comjni.ComJniException;
  public float getFontSize() throws com.inzoom.comjni.ComJniException;
  public void setFontSize(float FontSize) throws com.inzoom.comjni.ComJniException;
  public int getKernedPairs() throws com.inzoom.comjni.ComJniException;
  public void setKernedPairs(int KernedPairs) throws com.inzoom.comjni.ComJniException;
  public int getNormalizedHeight() throws com.inzoom.comjni.ComJniException;
  public void setNormalizedHeight(int NormalizedHeight) throws com.inzoom.comjni.ComJniException;
  public int getPresetShape() throws com.inzoom.comjni.ComJniException;
  public void setPresetShape(int PresetShape) throws com.inzoom.comjni.ComJniException;
  public int getPresetTextEffect() throws com.inzoom.comjni.ComJniException;
  public void setPresetTextEffect(int Preset) throws com.inzoom.comjni.ComJniException;
  public int getRotatedChars() throws com.inzoom.comjni.ComJniException;
  public void setRotatedChars(int RotatedChars) throws com.inzoom.comjni.ComJniException;
  public String getText() throws com.inzoom.comjni.ComJniException;
  public void setText(String Text) throws com.inzoom.comjni.ComJniException;
  public float getTracking() throws com.inzoom.comjni.ComJniException;
  public void setTracking(float Tracking) throws com.inzoom.comjni.ComJniException;
}
