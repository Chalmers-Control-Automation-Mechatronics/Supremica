package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface TextFrame Implementation
public class TextFrameJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame getTextFrameFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TextFrameJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame getTextFrameFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TextFrameJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame getTextFrameFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new TextFrameJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame convertComPtrToTextFrame(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TextFrameJCW(comPtr,true,releaseComPtr); }
  protected TextFrameJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected TextFrameJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID); }
  protected TextFrameJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected TextFrameJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID); }
  protected TextFrameJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected TextFrameJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID,releaseComPtr);}
  protected TextFrameJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public float getMarginBottom() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMarginBottom(float MarginBottom) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(MarginBottom,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getMarginLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMarginLeft(float MarginLeft) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(MarginLeft,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getMarginRight() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMarginRight(float MarginRight) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(MarginRight,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getMarginTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMarginTop(float MarginTop) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(MarginTop,false)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getOrientation() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setOrientation(int Orientation) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Orientation,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.TextFrame.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
