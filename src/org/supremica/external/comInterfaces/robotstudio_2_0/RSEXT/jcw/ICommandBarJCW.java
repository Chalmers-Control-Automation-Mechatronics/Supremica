package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw;

// interface ICommandBar Implementation
public class ICommandBarJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar getICommandBarFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar getICommandBarFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar getICommandBarFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar convertComPtrToICommandBar(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID); }
  protected ICommandBarJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID); }
  protected ICommandBarJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID,releaseComPtr);}
  protected ICommandBarJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls getControls() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlsJCW.getICommandBarControlsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getLeft() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLeft(int pxpLeft) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pxpLeft,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getPosition() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setPosition(int ppos) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ppos,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getRowIndex() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRowIndex(int piRow) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(piRow,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTop() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTop(int pypTop) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pypTop,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pvarfVisible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfVisible,false)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(Visible),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl findControl(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl findControl() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
