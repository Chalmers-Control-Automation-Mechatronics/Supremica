package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw;

// interface IPseAddInManager Implementation
public class IPseAddInManagerJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager getIPseAddInManagerFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseAddInManagerJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager getIPseAddInManagerFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseAddInManagerJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager getIPseAddInManagerFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPseAddInManagerJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager convertComPtrToIPseAddInManager(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseAddInManagerJCW(comPtr,true,releaseComPtr); }
  protected IPseAddInManagerJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPseAddInManagerJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID); }
  protected IPseAddInManagerJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPseAddInManagerJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID); }
  protected IPseAddInManagerJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPseAddInManagerJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID,releaseComPtr);}
  protected IPseAddInManagerJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns getCOMAddIns() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns rv = org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw.IPseColCOMAddInsJCW.getIPseColCOMAddInsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void doModal() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getRegistryPath() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRegistryPath(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IDispatch getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setApplication(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void startup() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void startupComplete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void shutdown() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getConnectMode() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getDisconnectMode() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void beginShutdown() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getAllowUnload() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAllowUnload(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
