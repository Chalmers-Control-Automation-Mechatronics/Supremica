package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface ICommandBarButtonEvents Implementation
public class ICommandBarButtonEventsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents getICommandBarButtonEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarButtonEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents getICommandBarButtonEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarButtonEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents getICommandBarButtonEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarButtonEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents convertComPtrToICommandBarButtonEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarButtonEventsJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarButtonEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarButtonEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents.IID); }
  protected ICommandBarButtonEventsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarButtonEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents.IID); }
  protected ICommandBarButtonEventsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarButtonEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents.IID,releaseComPtr);}
  protected ICommandBarButtonEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void click(org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarButton Ctrl,boolean[] CancelDefault) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Ctrl,false),
      new com.inzoom.comjni.Variant(CancelDefault[0],true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarButtonEvents.IID);
    CancelDefault[0] = _v[1].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
