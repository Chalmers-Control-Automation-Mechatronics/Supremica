package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IPseProxyAdm Implementation
public class IPseProxyAdmJCW extends com.inzoom.comjni.jcw.IUnknownJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm, com.inzoom.comjni.IUnknown {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm getIPseProxyAdmFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseProxyAdmJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm getIPseProxyAdmFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseProxyAdmJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm getIPseProxyAdmFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPseProxyAdmJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm convertComPtrToIPseProxyAdm(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseProxyAdmJCW(comPtr,true,releaseComPtr); }
  protected IPseProxyAdmJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPseProxyAdmJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm.IID); }
  protected IPseProxyAdmJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPseProxyAdmJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm.IID); }
  protected IPseProxyAdmJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPseProxyAdmJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm.IID,releaseComPtr);}
  protected IPseProxyAdmJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void attach(int pNode) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pNode,false)
    };
    vtblCall(12,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int detach() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(16,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setApp(int pApp) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pApp,false)
    };
    vtblCall(20,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getPtr_() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(24,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyAdm.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
