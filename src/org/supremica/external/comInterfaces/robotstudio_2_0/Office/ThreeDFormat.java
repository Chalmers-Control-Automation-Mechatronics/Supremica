package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ThreeDFormat Declaration
public interface ThreeDFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0321,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void incrementRotationX(float Increment) throws com.inzoom.comjni.ComJniException;
  public void incrementRotationY(float Increment) throws com.inzoom.comjni.ComJniException;
  public void resetRotation() throws com.inzoom.comjni.ComJniException;
  public void setThreeDFormat(int PresetThreeDFormat) throws com.inzoom.comjni.ComJniException;
  public void setExtrusionDirection(int PresetExtrusionDirection) throws com.inzoom.comjni.ComJniException;
  public float getDepth() throws com.inzoom.comjni.ComJniException;
  public void setDepth(float Depth) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getExtrusionColor() throws com.inzoom.comjni.ComJniException;
  public int getExtrusionColorType() throws com.inzoom.comjni.ComJniException;
  public void setExtrusionColorType(int ExtrusionColorType) throws com.inzoom.comjni.ComJniException;
  public int getPerspective() throws com.inzoom.comjni.ComJniException;
  public void setPerspective(int Perspective) throws com.inzoom.comjni.ComJniException;
  public int getPresetExtrusionDirection() throws com.inzoom.comjni.ComJniException;
  public int getPresetLightingDirection() throws com.inzoom.comjni.ComJniException;
  public void setPresetLightingDirection(int PresetLightingDirection) throws com.inzoom.comjni.ComJniException;
  public int getPresetLightingSoftness() throws com.inzoom.comjni.ComJniException;
  public void setPresetLightingSoftness(int PresetLightingSoftness) throws com.inzoom.comjni.ComJniException;
  public int getPresetMaterial() throws com.inzoom.comjni.ComJniException;
  public void setPresetMaterial(int PresetMaterial) throws com.inzoom.comjni.ComJniException;
  public int getPresetThreeDFormat() throws com.inzoom.comjni.ComJniException;
  public float getRotationX() throws com.inzoom.comjni.ComJniException;
  public void setRotationX(float RotationX) throws com.inzoom.comjni.ComJniException;
  public float getRotationY() throws com.inzoom.comjni.ComJniException;
  public void setRotationY(float RotationY) throws com.inzoom.comjni.ComJniException;
  public int getVisible() throws com.inzoom.comjni.ComJniException;
  public void setVisible(int Visible) throws com.inzoom.comjni.ComJniException;
}
