package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface ICommandBarsEvents Implementation
public class ICommandBarsEventsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents getICommandBarsEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarsEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents getICommandBarsEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarsEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents getICommandBarsEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarsEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents convertComPtrToICommandBarsEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarsEventsJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarsEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarsEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents.IID); }
  protected ICommandBarsEventsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarsEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents.IID); }
  protected ICommandBarsEventsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarsEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents.IID,releaseComPtr);}
  protected ICommandBarsEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void onUpdate() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarsEvents.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
