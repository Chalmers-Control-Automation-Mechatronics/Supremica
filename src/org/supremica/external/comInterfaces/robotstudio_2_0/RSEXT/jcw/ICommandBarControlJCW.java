package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw;

// interface ICommandBarControl Implementation
public class ICommandBarControlJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl getICommandBarControlFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarControlJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl getICommandBarControlFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarControlJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl getICommandBarControlFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarControlJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl convertComPtrToICommandBarControl(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarControlJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarControlJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarControlJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID); }
  protected ICommandBarControlJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarControlJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID); }
  protected ICommandBarControlJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarControlJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID,releaseComPtr);}
  protected ICommandBarControlJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void execute() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getId() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getEnabled() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setEnabled(boolean pEnabled) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pEnabled,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getBeginGroup() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBeginGroup(boolean pvarfBeginGroup) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfBeginGroup,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getCaption() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setCaption(String pbstrCaption) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrCaption,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControl.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
