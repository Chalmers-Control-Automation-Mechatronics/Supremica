package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw;

// interface IPseProxyEvents Implementation
public class IPseProxyEventsJCW extends com.inzoom.comjni.jcw.IUnknownJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents, com.inzoom.comjni.IUnknown {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents getIPseProxyEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseProxyEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents getIPseProxyEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseProxyEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents getIPseProxyEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IPseProxyEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents convertComPtrToIPseProxyEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IPseProxyEventsJCW(comPtr,true,releaseComPtr); }
  protected IPseProxyEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IPseProxyEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID); }
  protected IPseProxyEventsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IPseProxyEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID); }
  protected IPseProxyEventsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IPseProxyEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID,releaseComPtr);}
  protected IPseProxyEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void fireSelected() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(12,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void fireUnSelected() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(16,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void fireChanged(int Type) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Type,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(20,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void fireCollisionStart(com.inzoom.comjni.IUnknown Obj) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Obj,false)
    };
    vtblCall(24,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void fireCollisionEnd(com.inzoom.comjni.IUnknown Obj) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Obj,false)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPseProxyEvents.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
