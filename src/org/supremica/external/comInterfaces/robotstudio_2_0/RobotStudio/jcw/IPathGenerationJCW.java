package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IPathGeneration Implementation
public class IPathGenerationJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration getIPathGenerationFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPathGenerationJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration getIPathGenerationFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPathGenerationJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration getIPathGenerationFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPathGenerationJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration convertComPtrToIPathGeneration(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPathGenerationJCW(comPtr,true,releaseComPtr); }
  protected IPathGenerationJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPathGenerationJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID); }
  protected IPathGenerationJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPathGenerationJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID); }
  protected IPathGenerationJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPathGenerationJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID,releaseComPtr);}
  protected IPathGenerationJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths distributeTargetsOnCurves(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities,double MinimumDistance,double MaximuDeviation,int ApproximationType,int pPriority) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entities,false),
      new com.inzoom.comjni.Variant(MinimumDistance,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(MaximuDeviation,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(ApproximationType,false),
      new com.inzoom.comjni.Variant(pPriority,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathsJCW.getIPathsFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths distributeTargetsOnCurves(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection Entities,double MinimumDistance,double MaximuDeviation,int ApproximationType) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entities,false),
      new com.inzoom.comjni.Variant(MinimumDistance,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(MaximuDeviation,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(ApproximationType,false),
      new com.inzoom.comjni.Variant((int)0,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathsJCW.getIPathsFromComPtr(_v[5].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 createTargetOnCurve(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity Entity,int OffsetType,double Distance,int ReferencePoint) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Entity,false),
      new com.inzoom.comjni.Variant(OffsetType,false),
      new com.inzoom.comjni.Variant(Distance,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(ReferencePoint,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITarget2JCW.getITarget2FromComPtr(_v[4].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public com.inzoom.comjni.Variant createCircularInfo(com.inzoom.comjni.Variant PointPos,com.inzoom.comjni.Variant TangentPos,double cordafault) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(PointPos),
      createVTblVArg(TangentPos),
      new com.inzoom.comjni.Variant(cordafault,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(new com.inzoom.comjni.Variant(),true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathGeneration.IID);
    com.inzoom.comjni.Variant rv = _v[3].getVariant();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
