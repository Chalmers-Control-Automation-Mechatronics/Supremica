package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw;

// interface ICommandBarControls Implementation
public class ICommandBarControlsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls getICommandBarControlsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarControlsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls getICommandBarControlsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarControlsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls getICommandBarControlsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarControlsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls convertComPtrToICommandBarControls(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarControlsJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarControlsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarControlsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID); }
  protected ICommandBarControlsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarControlsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID); }
  protected ICommandBarControlsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarControlsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID,releaseComPtr);}
  protected ICommandBarControlsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl add(int Type,String Name,com.inzoom.comjni.IDispatch Icon,com.inzoom.comjni.Variant Before) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false),
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(Icon,false),
      createVTblVArg(Before),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl add(int Type,String Name,com.inzoom.comjni.IDispatch Icon) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false),
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant(Icon,false),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl add(int Type,String Name) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false),
      new com.inzoom.comjni.Variant(Name,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,false),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl rv = org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW.getICommandBarControlFromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setIsFindControls(boolean bFindCtrls) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(bFindCtrls,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void getFindControls(com.inzoom.comjni.IUnknown[] ppCtrls) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ppCtrls[0],true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID);
    ppCtrls[0] = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
