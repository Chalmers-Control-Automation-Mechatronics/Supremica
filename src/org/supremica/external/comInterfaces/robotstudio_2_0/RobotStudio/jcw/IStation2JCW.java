package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IStation2 Implementation
public class IStation2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IStationJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getIStation2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IStation2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getIStation2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IStation2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 getIStation2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IStation2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 convertComPtrToIStation2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IStation2JCW(comPtr,true,releaseComPtr); }
  protected IStation2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IStation2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2.IID); }
  protected IStation2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IStation2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2.IID); }
  protected IStation2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IStation2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2.IID,releaseComPtr);}
  protected IStation2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 importPart(String FileName,boolean Optimize) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant(Optimize,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(248,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPart2JCW.getIPart2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 importPart(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false),
      new com.inzoom.comjni.Variant((boolean)true,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(248,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPart2JCW.getIPart2FromComPtr(_v[2].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstructions getActionInstructions() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(252,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstructions rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IActionInstructionsJCW.getIActionInstructionsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void clearSyncHistory() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(256,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
