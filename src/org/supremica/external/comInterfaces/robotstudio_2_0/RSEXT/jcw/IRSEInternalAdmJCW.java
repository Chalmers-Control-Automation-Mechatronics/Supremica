package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw;

// interface IRSEInternalAdm Implementation
public class IRSEInternalAdmJCW extends com.inzoom.comjni.jcw.IUnknownJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm, com.inzoom.comjni.IUnknown {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm getIRSEInternalAdmFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRSEInternalAdmJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm getIRSEInternalAdmFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRSEInternalAdmJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm getIRSEInternalAdmFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IRSEInternalAdmJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm convertComPtrToIRSEInternalAdm(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRSEInternalAdmJCW(comPtr,true,releaseComPtr); }
  protected IRSEInternalAdmJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IRSEInternalAdmJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID); }
  protected IRSEInternalAdmJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IRSEInternalAdmJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID); }
  protected IRSEInternalAdmJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IRSEInternalAdmJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID,releaseComPtr);}
  protected IRSEInternalAdmJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void setID(int Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Id,false)
    };
    vtblCall(12,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getID() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(16,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setParentID(int Id) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Id,false)
    };
    vtblCall(20,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getParentID() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(24,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setMgr(com.inzoom.comjni.IUnknown pMgr) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pMgr,false)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setParent(com.inzoom.comjni.IDispatch pParent) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pParent,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void getParent(com.inzoom.comjni.IDispatch[] ppParent) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ppParent[0],true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    ppParent[0] = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setChild(com.inzoom.comjni.IUnknown pChild) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pChild,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void removeChild(com.inzoom.comjni.IUnknown pChild) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pChild,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void notifyChildren() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void notifyParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSEInternalAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
