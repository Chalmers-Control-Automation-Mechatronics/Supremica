package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface LineFormat Implementation
public class LineFormatJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat getLineFormatFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LineFormatJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat getLineFormatFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LineFormatJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat getLineFormatFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new LineFormatJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat convertComPtrToLineFormat(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new LineFormatJCW(comPtr,true,releaseComPtr); }
  protected LineFormatJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected LineFormatJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID); }
  protected LineFormatJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected LineFormatJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID); }
  protected LineFormatJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected LineFormatJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID,releaseComPtr);}
  protected LineFormatJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getBackColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ColorFormatJCW.getColorFormatFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBackColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat BackColor) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)BackColor,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getBeginArrowheadLength() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBeginArrowheadLength(int BeginArrowheadLength) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(BeginArrowheadLength,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getBeginArrowheadStyle() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBeginArrowheadStyle(int BeginArrowheadStyle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(BeginArrowheadStyle,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getBeginArrowheadWidth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBeginArrowheadWidth(int BeginArrowheadWidth) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(BeginArrowheadWidth,false)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getDashStyle() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDashStyle(int DashStyle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(DashStyle,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getEndArrowheadLength() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setEndArrowheadLength(int EndArrowheadLength) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(EndArrowheadLength,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getEndArrowheadStyle() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setEndArrowheadStyle(int EndArrowheadStyle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(EndArrowheadStyle,false)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getEndArrowheadWidth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setEndArrowheadWidth(int EndArrowheadWidth) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(EndArrowheadWidth,false)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat getForeColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.ColorFormatJCW.getColorFormatFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setForeColor(org.supremica.external.comInterfaces.robotstudio_2_0.Office.ColorFormat ForeColor) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ForeColor,false)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPattern() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPattern(int Pattern) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Pattern,false)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getStyle() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setStyle(int Style) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Style,false)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getTransparency() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTransparency(float Transparency) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Transparency,false)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(int Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Visible,false)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getWeight() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setWeight(float Weight) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Weight,false)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.LineFormat.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
