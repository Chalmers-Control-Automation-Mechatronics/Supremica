package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IPath2 Implementation
public class IPath2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getIPath2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPath2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getIPath2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPath2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 getIPath2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPath2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 convertComPtrToIPath2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPath2JCW(comPtr,true,releaseComPtr); }
  protected IPath2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPath2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2.IID); }
  protected IPath2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPath2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2.IID); }
  protected IPath2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPath2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2.IID,releaseComPtr);}
  protected IPath2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstructions getPathInstructions() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstructions rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathInstructionsJCW.getIPathInstructionsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction insertPathInstruction(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject NewItem,int order,com.inzoom.comjni.Variant Index) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)NewItem,false),
      new com.inzoom.comjni.Variant(order,false),
      createVTblVArg(Index),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathInstructionJCW.getIPathInstructionFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction insertPathInstruction(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject NewItem,int order) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)NewItem,false),
      new com.inzoom.comjni.Variant(order,false),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathInstructionJCW.getIPathInstructionFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction insertPathInstruction(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObject NewItem) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant noParam = com.inzoom.comjni.Variant.createNoParam();
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)NewItem,false),
      new com.inzoom.comjni.Variant((int)0,false),
      createVTblVArg(noParam),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPathInstruction rv = org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathInstructionJCW.getIPathInstructionFromComPtr(_v[3].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
