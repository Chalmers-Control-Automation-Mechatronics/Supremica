package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IUserOptions2 Implementation
public class IUserOptions2JCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IUserOptionsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2 {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2 getIUserOptions2FromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IUserOptions2JCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2 getIUserOptions2FromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IUserOptions2JCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2 getIUserOptions2FromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IUserOptions2JCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2 convertComPtrToIUserOptions2(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IUserOptions2JCW(comPtr,true,releaseComPtr); }
  protected IUserOptions2JCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IUserOptions2JCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID); }
  protected IUserOptions2JCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IUserOptions2JCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID); }
  protected IUserOptions2JCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IUserOptions2JCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID,releaseComPtr);}
  protected IUserOptions2JCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public boolean getShowSyncDialog() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(304,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setShowSyncDialog(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(308,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getQuickRefreshSystem() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(312,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setQuickRefreshSystem(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(316,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public boolean getQuickRefreshProgram() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(320,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setQuickRefreshProgram(boolean pVal) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pVal,false)
    };
    vtblCall(324,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IUserOptions2.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
