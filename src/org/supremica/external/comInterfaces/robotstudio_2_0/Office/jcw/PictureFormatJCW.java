package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface PictureFormat Implementation
public class PictureFormatJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat getPictureFormatFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PictureFormatJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat getPictureFormatFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PictureFormatJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat getPictureFormatFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new PictureFormatJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat convertComPtrToPictureFormat(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PictureFormatJCW(comPtr,true,releaseComPtr); }
  protected PictureFormatJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected PictureFormatJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID); }
  protected PictureFormatJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected PictureFormatJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID); }
  protected PictureFormatJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected PictureFormatJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID,releaseComPtr);}
  protected PictureFormatJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void incrementBrightness(float Increment) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Increment,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void incrementContrast(float Increment) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Increment,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getBrightness() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBrightness(float Brightness) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Brightness,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getColorType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setColorType(int ColorType) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ColorType,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getContrast() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setContrast(float Contrast) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Contrast,false)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getCropBottom() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCropBottom(float CropBottom) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(CropBottom,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getCropLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCropLeft(float CropLeft) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(CropLeft,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getCropRight() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCropRight(float CropRight) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(CropRight,false)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getCropTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCropTop(float CropTop) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(CropTop,false)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTransparencyColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTransparencyColor(int TransparencyColor) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(TransparencyColor,false)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTransparentBackground() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTransparentBackground(int TransparentBackground) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(TransparentBackground,false)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.PictureFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
