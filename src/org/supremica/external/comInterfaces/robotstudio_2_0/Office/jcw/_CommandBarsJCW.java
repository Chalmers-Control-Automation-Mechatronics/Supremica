package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface _CommandBars Implementation
public class _CommandBarsJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._IMsoDispObjJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars get_CommandBarsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars get_CommandBarsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars get_CommandBarsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _CommandBarsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars convertComPtrTo_CommandBars(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarsJCW(comPtr,true,releaseComPtr); }
  protected _CommandBarsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _CommandBarsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID); }
  protected _CommandBarsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _CommandBarsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID); }
  protected _CommandBarsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _CommandBarsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID,releaseComPtr);}
  protected _CommandBarsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl getActionControl() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getActiveMenuBar() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar,com.inzoom.comjni.Variant Temporary) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Name),
      createVTblVArg(Position),
      createVTblVArg(MenuBar),
      createVTblVArg(Temporary),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Name),
      createVTblVArg(Position),
      createVTblVArg(MenuBar),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name,com.inzoom.comjni.Variant Position) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Name),
      createVTblVArg(Position),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add(com.inzoom.comjni.Variant Name) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Name),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar add() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getDisplayTooltips() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDisplayTooltips(boolean pvarfDisplayTooltips) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfDisplayTooltips,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getDisplayKeysInTooltips() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDisplayKeysInTooltips(boolean pvarfDisplayKeys) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfDisplayKeys,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(Visible),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl findControl() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW.getCommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getLargeButtons() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLargeButtons(boolean pvarfLargeButtons) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfLargeButtons,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getMenuAnimationStyle() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMenuAnimationStyle(int pma) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pma,false)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void releaseFocus() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getIdsString(int ids,String[] pbstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ids,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(pbstrName[0],true),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    pbstrName[0] = _v[1].getString();
    int rv = _v[2].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getTmcGetName(int tmc,String[] pbstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(tmc,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant(pbstrName[0],true),
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    pbstrName[0] = _v[1].getString();
    int rv = _v[2].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getAdaptiveMenus() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAdaptiveMenus(boolean pvarfAdaptiveMenus) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfAdaptiveMenus,false)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(Visible),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlsJCW.getCommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlsJCW.getCommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlsJCW.getCommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlsJCW.getCommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls findControls() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlsJCW.getCommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar,com.inzoom.comjni.Variant Temporary,com.inzoom.comjni.Variant TbtrProtection) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(TbidOrName),
      createVTblVArg(Position),
      createVTblVArg(MenuBar),
      createVTblVArg(Temporary),
      createVTblVArg(TbtrProtection),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar,com.inzoom.comjni.Variant Temporary) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(TbidOrName),
      createVTblVArg(Position),
      createVTblVArg(MenuBar),
      createVTblVArg(Temporary),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position,com.inzoom.comjni.Variant MenuBar) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(TbidOrName),
      createVTblVArg(Position),
      createVTblVArg(MenuBar),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName,com.inzoom.comjni.Variant Position) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(TbidOrName),
      createVTblVArg(Position),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx(com.inzoom.comjni.Variant TbidOrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(TbidOrName),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar addEx() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getDisplayFonts() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDisplayFonts(boolean pvarfDisplayFonts) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfDisplayFonts,false)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
