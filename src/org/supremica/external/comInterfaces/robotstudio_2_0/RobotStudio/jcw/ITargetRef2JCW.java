package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface ITargetRef2 Implementation
public class ITargetRef2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITargetRefJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 getITargetRef2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ITargetRef2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 getITargetRef2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ITargetRef2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 getITargetRef2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ITargetRef2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2 convertComPtrToITargetRef2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ITargetRef2JCW(comPtr,true,releaseComPtr); }
  protected ITargetRef2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ITargetRef2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2.IID); }
  protected ITargetRef2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ITargetRef2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2.IID); }
  protected ITargetRef2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ITargetRef2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2.IID,releaseComPtr);}
  protected ITargetRef2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void setPartialColor(com.inzoom.comjni.Variant RGBA,double Start,double End) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      createVTblVArg(RGBA),
      new com.inzoom.comjni.Variant(Start,com.inzoom.comjni.enum.VarType.R8,false),
      new com.inzoom.comjni.Variant(End,com.inzoom.comjni.enum.VarType.R8,false)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetRef2.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
