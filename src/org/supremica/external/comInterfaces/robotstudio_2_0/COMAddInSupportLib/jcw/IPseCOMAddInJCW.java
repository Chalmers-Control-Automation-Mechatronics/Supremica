package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw;

// interface IPseCOMAddIn Implementation
public class IPseCOMAddInJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn getIPseCOMAddInFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseCOMAddInJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn getIPseCOMAddInFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseCOMAddInJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn getIPseCOMAddInFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPseCOMAddInJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn convertComPtrToIPseCOMAddIn(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseCOMAddInJCW(comPtr,true,releaseComPtr); }
  protected IPseCOMAddInJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPseCOMAddInJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID); }
  protected IPseCOMAddInJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPseCOMAddInJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID); }
  protected IPseCOMAddInJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPseCOMAddInJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID,releaseComPtr);}
  protected IPseCOMAddInJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getDescription() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDescription(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getConnect() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setConnect(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getGuid() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setGuid(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setParent(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getProgID() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setProgID(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.IDispatch getObject() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setObject(com.inzoom.comjni.IDispatch pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getFriendlyName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFriendlyName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getLoadBehavior() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setLoadBehavior(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getFileName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFileName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getRestricted() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRestricted(int pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseCOMAddIn.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
