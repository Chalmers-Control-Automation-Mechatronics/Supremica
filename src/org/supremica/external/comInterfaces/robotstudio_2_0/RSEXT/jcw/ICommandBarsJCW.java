package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw;

// interface ICommandBars Implementation
public class ICommandBarsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars getICommandBarsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars getICommandBarsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars getICommandBarsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars convertComPtrToICommandBars(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarsJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID); }
  protected ICommandBarsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID); }
  protected ICommandBarsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID,releaseComPtr);}
  protected ICommandBarsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarJCW.getICommandBarFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar add(String Name,int BarPosition,boolean IntitallyFloating,boolean InitiallyVisible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(BarPosition,false),
      new com.inzoom.comjni.Variant(IntitallyFloating,false),
      new com.inzoom.comjni.Variant(InitiallyVisible,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarJCW.getICommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar add(String Name,int BarPosition,boolean IntitallyFloating) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(BarPosition,false),
      new com.inzoom.comjni.Variant(IntitallyFloating,false),
      new com.inzoom.comjni.Variant((boolean)true,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarJCW.getICommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar add(String Name,int BarPosition) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(BarPosition,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)true,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarJCW.getICommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar add(String Name) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant((int)1,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)true,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarJCW.getICommandBarFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl findControl(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(Visible),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
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
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
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
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
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
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
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
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag,com.inzoom.comjni.Variant Visible) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(Visible),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlsJCW.getICommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id,com.inzoom.comjni.Variant Tag) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(Tag),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlsJCW.getICommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls findControls(com.inzoom.comjni.Variant Type,com.inzoom.comjni.Variant Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(Id),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlsJCW.getICommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls findControls(com.inzoom.comjni.Variant Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Type),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlsJCW.getICommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls findControls() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlsJCW.getICommandBarControlsFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
