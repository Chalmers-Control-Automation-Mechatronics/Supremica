package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _VBProject_Old Implementation
public class _VBProject_OldJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._ProjectTemplateJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old get_VBProject_OldFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProject_OldJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old get_VBProject_OldFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProject_OldJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old get_VBProject_OldFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _VBProject_OldJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old convertComPtrTo_VBProject_Old(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProject_OldJCW(comPtr,true,releaseComPtr); }
  protected _VBProject_OldJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _VBProject_OldJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID); }
  protected _VBProject_OldJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _VBProject_OldJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID); }
  protected _VBProject_OldJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _VBProject_OldJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID,releaseComPtr);}
  protected _VBProject_OldJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getHelpFile() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHelpFile(String lpbstrHelpFile) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lpbstrHelpFile,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHelpContextID() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHelpContextID(int lpdwContextID) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lpdwContextID,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getDescription() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setDescription(String lpbstrDescription) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lpbstrDescription,false)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getMode() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.References getReferences() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.References rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.References.getReferencesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String lpbstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lpbstrName,false)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.VBEJCW.getVBEFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProjects getCollection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProjects rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProjects.getVBProjectsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getProtection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getSaved() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponents getVBComponents() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject_Old.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponents rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponents.getVBComponentsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
