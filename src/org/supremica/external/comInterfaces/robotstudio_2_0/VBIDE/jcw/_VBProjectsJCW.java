package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _VBProjects Implementation
public class _VBProjectsJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._VBProjects_OldJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects get_VBProjectsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProjectsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects get_VBProjectsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProjectsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects get_VBProjectsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _VBProjectsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects convertComPtrTo_VBProjects(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProjectsJCW(comPtr,true,releaseComPtr); }
  protected _VBProjectsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _VBProjectsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID); }
  protected _VBProjectsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _VBProjectsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID); }
  protected _VBProjectsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _VBProjectsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID,releaseComPtr);}
  protected _VBProjectsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject add(int Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject.getVBProjectFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void remove(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject lpc) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)lpc,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject open(String bstrPath) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(bstrPath,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProjects.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject.getVBProjectFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
