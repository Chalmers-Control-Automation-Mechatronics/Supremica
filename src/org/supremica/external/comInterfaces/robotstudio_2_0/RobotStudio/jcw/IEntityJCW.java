package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IEntity Implementation
public class IEntityJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity getIEntityFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IEntityJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity getIEntityFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IEntityJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity getIEntityFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IEntityJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity convertComPtrToIEntity(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IEntityJCW(comPtr,true,releaseComPtr); }
  protected IEntityJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IEntityJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID); }
  protected IEntityJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IEntityJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID); }
  protected IEntityJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IEntityJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID,releaseComPtr);}
  protected IEntityJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getTransform() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITransformJCW.getITransformFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTransform(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)pVal,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getUniqueName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getVisible() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setVisible(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public double getArea() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public double getVolume() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    double rv = _v[0].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getSelected() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getHasSelectedWire() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAttributesJCW.getIAttributesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW.getIApplication3FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPart2JCW.getIPart2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getCenterOfGravity() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public float getRelativeTransparency() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((float)0,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    float rv = _v[0].getFloat();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setRelativeTransparency(float RelTransp) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(RelTransp,false)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void examine() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void unexamine() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(104,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IShells getShells() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(108,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IShells rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IShellsJCW.getIShellsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFaces getFaces() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(112,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFaces rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFacesJCW.getIFacesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdges getEdges() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(116,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdges rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEdgesJCW.getIEdgesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant getColor() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(120,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    com.inzoom.comjni.Variant rv = _v[0].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setColor(com.inzoom.comjni.Variant RGBA) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(RGBA)
    };
    vtblCall(124,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities intersect(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal,String BaseName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false),
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant(BaseName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW.getIEntitiesFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities intersect(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false),
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(128,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW.getIEntitiesFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 join(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal,String BaseName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false),
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant(BaseName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 join(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false),
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(132,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities cut(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal,String BaseName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false),
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant(BaseName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW.getIEntitiesFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities cut(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,boolean KeepOriginal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false),
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(136,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW.getIEntitiesFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox getBoundingBox() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(140,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IBoundingBoxJCW.getIBoundingBoxFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWires getWires() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(144,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWires rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWiresJCW.getIWiresFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean getBackFaceCulling() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(148,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBackFaceCulling(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(152,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 coverWire(boolean KeepOriginal,String resName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant(resName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 coverWire(boolean KeepOriginal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(KeepOriginal,false),
      new com.inzoom.comjni.Variant((String)"",false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(156,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 reverse() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(160,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW.getIEntity2FromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
