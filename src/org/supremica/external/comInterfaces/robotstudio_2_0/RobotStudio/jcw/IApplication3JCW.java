package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IApplication3 Implementation
public class IApplication3JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getIApplication3FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IApplication3JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getIApplication3FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IApplication3JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getIApplication3FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IApplication3JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 convertComPtrToIApplication3(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IApplication3JCW(comPtr,true,releaseComPtr); }
  protected IApplication3JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IApplication3JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID); }
  protected IApplication3JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IApplication3JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID); }
  protected IApplication3JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IApplication3JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID,releaseComPtr);}
  protected IApplication3JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public com.inzoom.comjni.IDispatch addBrowserTab(String CLSID,String TabName,com.inzoom.comjni.IDispatch Icon,String LicenseString) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(CLSID,false),
      new com.inzoom.comjni.Variant(TabName,false),
      new com.inzoom.comjni.Variant(Icon,false),
      new com.inzoom.comjni.Variant(LicenseString,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch addBrowserTab(String CLSID,String TabName,com.inzoom.comjni.IDispatch Icon) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(CLSID,false),
      new com.inzoom.comjni.Variant(TabName,false),
      new com.inzoom.comjni.Variant(Icon,false),
      new com.inzoom.comjni.Variant((String)null,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.IDispatch addBrowserTab(String CLSID,String TabName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(CLSID,false),
      new com.inzoom.comjni.Variant(TabName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,false),
      new com.inzoom.comjni.Variant((String)null,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID);
    com.inzoom.comjni.IDispatch rv = com.inzoom.comjni.jcw.IDispatchJCW.getIDispatchFromPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void removeBrowserTab(com.inzoom.comjni.Variant Tab) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Tab)
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setActiveBrowserTab(com.inzoom.comjni.Variant Tab) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(Tab)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public com.inzoom.comjni.Variant getActiveBrowserTab() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
