package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface IMsoDispCagNotifySink Implementation
public class IMsoDispCagNotifySinkJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink getIMsoDispCagNotifySinkFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMsoDispCagNotifySinkJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink getIMsoDispCagNotifySinkFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMsoDispCagNotifySinkJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink getIMsoDispCagNotifySinkFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IMsoDispCagNotifySinkJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink convertComPtrToIMsoDispCagNotifySink(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IMsoDispCagNotifySinkJCW(comPtr,true,releaseComPtr); }
  protected IMsoDispCagNotifySinkJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IMsoDispCagNotifySinkJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink.IID); }
  protected IMsoDispCagNotifySinkJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected IMsoDispCagNotifySinkJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink.IID); }
  protected IMsoDispCagNotifySinkJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected IMsoDispCagNotifySinkJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink.IID,releaseComPtr);}
  protected IMsoDispCagNotifySinkJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void insertClip(com.inzoom.comjni.IUnknown pClipMoniker,com.inzoom.comjni.IUnknown pItemMoniker) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pClipMoniker,false),
      new com.inzoom.comjni.Variant(pItemMoniker,false)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void windowIsClosing() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.IMsoDispCagNotifySink.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
