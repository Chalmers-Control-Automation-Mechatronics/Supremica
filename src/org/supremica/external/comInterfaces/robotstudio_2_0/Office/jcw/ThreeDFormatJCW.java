package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface ThreeDFormat Implementation
public class ThreeDFormatJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat getThreeDFormatFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ThreeDFormatJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat getThreeDFormatFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ThreeDFormatJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat getThreeDFormatFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ThreeDFormatJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat convertComPtrToThreeDFormat(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ThreeDFormatJCW(comPtr,true,releaseComPtr); }
  protected ThreeDFormatJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ThreeDFormatJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID); }
  protected ThreeDFormatJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ThreeDFormatJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID); }
  protected ThreeDFormatJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ThreeDFormatJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID,releaseComPtr);}
  protected ThreeDFormatJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void incrementRotationX(float Increment) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Increment,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void incrementRotationY(float Increment) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Increment,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void resetRotation() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setThreeDFormat(int PresetThreeDFormat) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(PresetThreeDFormat,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setExtrusionDirection(int PresetExtrusionDirection) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(PresetExtrusionDirection,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getDepth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDepth(float Depth) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Depth,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getExtrusionColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ColorFormatJCW.getColorFormatFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getExtrusionColorType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setExtrusionColorType(int ExtrusionColorType) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ExtrusionColorType,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPerspective() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPerspective(int Perspective) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Perspective,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPresetExtrusionDirection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getPresetLightingDirection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPresetLightingDirection(int PresetLightingDirection) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(PresetLightingDirection,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPresetLightingSoftness() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPresetLightingSoftness(int PresetLightingSoftness) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(PresetLightingSoftness,false)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPresetMaterial() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPresetMaterial(int PresetMaterial) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(PresetMaterial,false)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPresetThreeDFormat() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public float getRotationX() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRotationX(float RotationX) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(RotationX,false)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getRotationY() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRotationY(float RotationY) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(RotationY,false)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(int Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Visible,false)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ThreeDFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
