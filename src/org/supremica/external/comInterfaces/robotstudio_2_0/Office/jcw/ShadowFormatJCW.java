package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface ShadowFormat Implementation
public class ShadowFormatJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat getShadowFormatFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShadowFormatJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat getShadowFormatFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShadowFormatJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat getShadowFormatFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ShadowFormatJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat convertComPtrToShadowFormat(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ShadowFormatJCW(comPtr,true,releaseComPtr); }
  protected ShadowFormatJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ShadowFormatJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID); }
  protected ShadowFormatJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ShadowFormatJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID); }
  protected ShadowFormatJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ShadowFormatJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID,releaseComPtr);}
  protected ShadowFormatJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void incrementOffsetX(float Increment) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Increment,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void incrementOffsetY(float Increment) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Increment,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getForeColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ColorFormatJCW.getColorFormatFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setForeColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat ForeColor) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ForeColor,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getObscured() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setObscured(int Obscured) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Obscured,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getOffsetX() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setOffsetX(float OffsetX) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(OffsetX,false)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getOffsetY() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setOffsetY(float OffsetY) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(OffsetY,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getTransparency() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTransparency(float Transparency) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Transparency,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setType(int Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(int Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Visible,false)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ShadowFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
