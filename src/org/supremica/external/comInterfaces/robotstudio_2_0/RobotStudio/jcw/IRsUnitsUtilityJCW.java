package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IRsUnitsUtility Implementation
public class IRsUnitsUtilityJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility getIRsUnitsUtilityFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRsUnitsUtilityJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility getIRsUnitsUtilityFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRsUnitsUtilityJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility getIRsUnitsUtilityFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IRsUnitsUtilityJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility convertComPtrToIRsUnitsUtility(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IRsUnitsUtilityJCW(comPtr,true,releaseComPtr); }
  protected IRsUnitsUtilityJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IRsUnitsUtilityJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID); }
  protected IRsUnitsUtilityJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IRsUnitsUtilityJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID); }
  protected IRsUnitsUtilityJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IRsUnitsUtilityJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID,releaseComPtr);}
  protected IRsUnitsUtilityJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public double unitToAPI(int Quantity,double UnitValue) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Quantity,false),
      new com.inzoom.comjni.Variant(UnitValue,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID);
    double rv = _v[2].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public double aPIToUnit(int Quantity,double APIValue) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Quantity,false),
      new com.inzoom.comjni.Variant(APIValue,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant((double)0,com.inzoom.comjni.enum.VarType.R8,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID);
    double rv = _v[2].getDouble();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform uCSToWCS(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform UCS) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)UCS,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITransformJCW.getITransformFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform wCSToUCS(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform WCS) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)WCS,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITransformJCW.getITransformFromComPtr(_v[1].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
