package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IMechanism2 Implementation
public class IMechanism2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMechanismJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 getIMechanism2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMechanism2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 getIMechanism2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMechanism2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 getIMechanism2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IMechanism2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 convertComPtrToIMechanism2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMechanism2JCW(comPtr,true,releaseComPtr); }
  protected IMechanism2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IMechanism2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2.IID); }
  protected IMechanism2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IMechanism2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2.IID); }
  protected IMechanism2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IMechanism2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2.IID,releaseComPtr);}
  protected IMechanism2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getLibraryName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(228,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection getSolutions(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 Target,com.inzoom.comjni.Variant Cfx) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      createVTblVArg(Cfx),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(232,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsCollectionJCW.getIRsCollectionFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection getSolutions(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 Target) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Target,false),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(232,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsCollectionJCW.getIRsCollectionFromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
