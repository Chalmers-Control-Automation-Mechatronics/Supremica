package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IWorkObject2 Implementation
public class IWorkObject2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IWorkObjectJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2 getIWorkObject2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IWorkObject2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2 getIWorkObject2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IWorkObject2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2 getIWorkObject2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IWorkObject2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2 convertComPtrToIWorkObject2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IWorkObject2JCW(comPtr,true,releaseComPtr); }
  protected IWorkObject2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IWorkObject2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2.IID); }
  protected IWorkObject2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IWorkObject2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2.IID); }
  protected IWorkObject2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IWorkObject2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2.IID,releaseComPtr);}
  protected IWorkObject2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void delete() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IWorkObject2.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
