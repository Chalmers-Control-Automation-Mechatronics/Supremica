package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IPath Implementation
public class IPathJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath getIPathFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPathJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath getIPathFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPathJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath getIPathFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPathJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath convertComPtrToIPath(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPathJCW(comPtr,true,releaseComPtr); }
  protected IPathJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPathJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID); }
  protected IPathJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPathJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID); }
  protected IPathJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPathJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID,releaseComPtr);}
  protected IPathJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsObjectJCW.getIRsObjectFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getUniqueName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRefs getTargetRefs() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRefs rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetRefsJCW.getITargetRefsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 insert(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target,int order,int Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      new com.inzoom.comjni.Variant(order,false),
      new com.inzoom.comjni.Variant(Index,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetRef2JCW.getITargetRef2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 insert(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target,int order) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      new com.inzoom.comjni.Variant(order,false),
      new com.inzoom.comjni.Variant((int)1,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetRef2JCW.getITargetRef2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 insert(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget Target) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      new com.inzoom.comjni.Variant((int)0,false),
      new com.inzoom.comjni.Variant((int)1,com.inzoom.comjni.enum.VarType.I4,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetRef2JCW.getITargetRef2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributesJCW.getIAttributesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(RGBA)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure getProcedure() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Procedure rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4ProcedureJCW.getIABBS4ProcedureFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void refresh() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void syncToVirtualController(String ModuleName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ModuleName,false)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
