package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw;

// interface IPseColCOMAddIns Implementation
public class IPseColCOMAddInsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns getIPseColCOMAddInsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseColCOMAddInsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns getIPseColCOMAddInsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseColCOMAddInsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns getIPseColCOMAddInsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPseColCOMAddInsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns convertComPtrToIPseColCOMAddIns(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseColCOMAddInsJCW(comPtr,true,releaseComPtr); }
  protected IPseColCOMAddInsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPseColCOMAddInsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID); }
  protected IPseColCOMAddInsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPseColCOMAddInsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID); }
  protected IPseColCOMAddInsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPseColCOMAddInsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID,releaseComPtr);}
  protected IPseColCOMAddInsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IUnknown get_NewEnum() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IUnknown)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    com.inzoom.comjni.IUnknown rv = com.inzoom.comjni.jcw.IUnknownJCW.getIUnknownFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn getItem(com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn rv = org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw.IPseCOMAddInJCW.getIPseCOMAddInFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCount() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void update() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getRegistryPath() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRegistryPath(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager getAddInManager() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager rv = org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw.IPseAddInManagerJCW.getIPseAddInManagerFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setAddInManager(org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void beginShutdown() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void shutdown() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void startupComplete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void startup() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addInUpdate(org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn pAddIn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pAddIn,false)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseColCOMAddIns.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
