package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IPart Implementation
public class IPartJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart getIPartFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPartJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart getIPartFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPartJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart getIPartFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPartJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart convertComPtrToIPart(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPartJCW(comPtr,true,releaseComPtr); }
  protected IPartJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPartJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID); }
  protected IPartJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPartJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID); }
  protected IPartJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPartJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID,releaseComPtr);}
  protected IPartJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getTransform() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITransformJCW.getITransformFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTransform(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getUniqueName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities getEntities() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW.getIEntitiesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributesJCW.getIAttributesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsObjectJCW.getIRsObjectFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void examine() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void unexamine() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createArc(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Start,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition End,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Circumference) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Start,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)End,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Circumference,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createCircle(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createEllipse(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Center,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition MajorAxisEnd,double MinorAxisDistance) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)MajorAxisEnd,false),
      new com.inzoom.comjni.Variant(MinorAxisDistance,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createEllipticArc(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Center,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition MajorAxisEnd,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition MinorAxisEnd,double StartAngle,double EndAngle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)MajorAxisEnd,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)MinorAxisEnd,false),
      new com.inzoom.comjni.Variant(StartAngle,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(EndAngle,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createLine(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Start,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition End) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Start,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)End,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createPolygon(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Center,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Vertex,int NumEdges) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Vertex,false),
      new com.inzoom.comjni.Variant(NumEdges,com.inzoom.comjni.enum.VarType.INT,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createRectangle(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform FirstCorner,double Length,double Width) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)FirstCorner,false),
      new com.inzoom.comjni.Variant(Length,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Width,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSolidBox(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Start,double Length,double Width,double Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Start,false),
      new com.inzoom.comjni.Variant(Length,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Width,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Height,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSolidCone(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius,double Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Height,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSolidCylinder(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius,double Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Height,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSolidPyramid(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius,double Height,double NumSides) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Height,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(NumSides,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSolidSphere(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSolidTorus(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double MajorRadius,double MinorRadius) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(MajorRadius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(MinorRadius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfaceCone(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius,double Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Height,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfaceCylinder(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius,double Height) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(Height,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfaceSphere(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfaceTorus(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double MajorRadius,double MinorRadius) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(MajorRadius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(MinorRadius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfacePolygon(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection OuterPositions,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection InnerBoundaries) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)OuterPositions,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)InnerBoundaries,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfacePolygon(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection OuterPositions) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)OuterPositions,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfaceRectangle(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform FirstCorner,double LengthX,double WidthY) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)FirstCorner,false),
      new com.inzoom.comjni.Variant(LengthX,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(WidthY,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSurfaceCircle(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform Center,double Radius) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Center,false),
      new com.inzoom.comjni.Variant(Radius,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void loadGeometry() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(164,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void saveToLibrary(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false)
    };
    vtblCall(168,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void disconnectFromLibrary() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(172,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addEntity(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox getBoundingBox() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(180,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IBoundingBoxJCW.getIBoundingBoxFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttachments getAttachments() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(184,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttachments rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttachmentsJCW.getIAttachmentsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(188,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(RGBA)
    };
    vtblCall(192,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public float getRelativeTransparency() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(196,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRelativeTransparency(float RelTransp) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(RelTransp,false)
    };
    vtblCall(200,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getShowCoordinateSystem() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(204,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setShowCoordinateSystem(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(208,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void fireOpen() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(212,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUps getMarkUps() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(216,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUps rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMarkUpsJCW.getIMarkUpsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createPolyLine(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Positions) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Positions,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(220,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSplineCurve(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Positions,double Tolerance) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Positions,false),
      new com.inzoom.comjni.Variant(Tolerance,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createSplineCurve(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Positions) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Positions,false),
      new com.inzoom.comjni.Variant((double)0.0,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(224,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createLineByEdges(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition StartPos,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition EndPos,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection ViaPos,com.inzoom.comjni.IDispatch Geometry,String resName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)StartPos,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)EndPos,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ViaPos,false),
      new com.inzoom.comjni.Variant(Geometry,false),
      new com.inzoom.comjni.Variant(resName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(228,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 createLineByEdges(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition StartPos,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition EndPos,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection ViaPos,com.inzoom.comjni.IDispatch Geometry) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)StartPos,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)EndPos,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)ViaPos,false),
      new com.inzoom.comjni.Variant(Geometry,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(228,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities createLineByIntersection(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities1,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities2,String resName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entities1,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entities2,false),
      new com.inzoom.comjni.Variant(resName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(232,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW.getIEntitiesFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities createLineByIntersection(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities1,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities2) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entities1,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entities2,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(232,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW.getIEntitiesFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
