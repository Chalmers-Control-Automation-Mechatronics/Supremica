package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface Window Implementation
public class WindowJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getWindowFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WindowJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getWindowFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WindowJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getWindowFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new WindowJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window convertComPtrToWindow(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new WindowJCW(comPtr,true,releaseComPtr); }
  protected WindowJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected WindowJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID); }
  protected WindowJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected WindowJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID); }
  protected WindowJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected WindowJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID,releaseComPtr);}
  protected WindowJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.VBEJCW.getVBEFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Windows getCollection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Windows rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Windows.getWindowsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void close() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getCaption() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pfVisible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pfVisible,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLeft(int plLeft) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(plLeft,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTop(int plTop) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(plTop,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getWidth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setWidth(int plWidth) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(plWidth,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHeight() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHeight(int plHeight) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(plHeight,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getWindowState() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setWindowState(int plWindowState) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(plWindowState,false)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setFocus() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setKind(int eKind) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(eKind,false)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.LinkedWindows getLinkedWindows() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.LinkedWindows rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.LinkedWindows.getLinkedWindowsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getLinkedWindowFrame() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.WindowJCW.getWindowFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void detach() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void attach(int lWindowHandle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lWindowHandle,false)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHWnd() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
