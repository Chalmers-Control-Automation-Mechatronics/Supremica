package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _VBProject Implementation
public class _VBProjectJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._VBProject_OldJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject get_VBProjectFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProjectJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject get_VBProjectFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProjectJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject get_VBProjectFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _VBProjectJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject convertComPtrTo_VBProject(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBProjectJCW(comPtr,true,releaseComPtr); }
  protected _VBProjectJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _VBProjectJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID); }
  protected _VBProjectJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _VBProjectJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID); }
  protected _VBProjectJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _VBProjectJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID,releaseComPtr);}
  protected _VBProjectJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void saveAs(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void makeCompiledFile() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getFileName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getBuildFileName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBuildFileName(String lpbstrBldFName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(lpbstrBldFName,false)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBProject.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
