package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IView Implementation
public class IViewJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView getIViewFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IViewJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView getIViewFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IViewJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView getIViewFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IViewJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView convertComPtrToIView(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IViewJCW(comPtr,true,releaseComPtr); }
  protected IViewJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IViewJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID); }
  protected IViewJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IViewJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID); }
  protected IViewJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IViewJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID,releaseComPtr);}
  protected IViewJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTop(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLeft(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getWidth() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setWidth(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHeight() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHeight(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IStation2JCW.getIStation2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPresetView(int rhs) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(rhs,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getLockView() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLockView(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getLockWindowSize() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLockWindowSize(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getProjection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setProjection(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getFieldOfView() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFieldOfView(double angle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(angle,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void redraw() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getLockWindowPos() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLockWindowPos(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getShowState() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setShowState(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setFocus() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void sendToBack() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void bringToFront() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getLookAt() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPositionJCW.getIPositionFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLookAt(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition getLookFrom() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPositionJCW.getIPositionFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLookFrom(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void hideObject(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject pObj) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pObj,false)
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void showObject(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject pObj) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pObj,false)
    };
    vtblCall(168,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getScreenSizeCulling() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(172,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setScreenSizeCulling(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getNavigationSensitivity() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setNavigationSensitivity(double pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getDragDropEnabled() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDragDropEnabled(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getCaptureMouse() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(196,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCaptureMouse(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHWnd() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(204,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant screenPosDirection(int x,int y) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(x,false),
      new com.inzoom.comjni.Variant(y,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    com.inzoom.comjni.Variant rv = _v[2].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant screenPosOrigin(int x,int y) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(x,false),
      new com.inzoom.comjni.Variant(y,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(212,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView.IID);
    com.inzoom.comjni.Variant rv = _v[2].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
