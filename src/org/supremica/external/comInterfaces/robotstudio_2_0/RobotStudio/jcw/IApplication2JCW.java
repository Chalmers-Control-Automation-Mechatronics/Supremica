package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IApplication2 Implementation
public class IApplication2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplicationJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2 getIApplication2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IApplication2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2 getIApplication2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IApplication2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2 getIApplication2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IApplication2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2 convertComPtrToIApplication2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IApplication2JCW(comPtr,true,releaseComPtr); }
  protected IApplication2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IApplication2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2.IID); }
  protected IApplication2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IApplication2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2.IID); }
  protected IApplication2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IApplication2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2.IID,releaseComPtr);}
  protected IApplication2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public boolean importProcessTemplateFile(String File) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(File,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(176,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication2.IID);
    boolean rv = _v[1].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
