package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _VBComponentsEvents Implementation
public class _VBComponentsEventsJCW extends com.inzoom.comjni.jcw.IUnknownJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents, com.inzoom.comjni.IUnknown {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents get_VBComponentsEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBComponentsEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents get_VBComponentsEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBComponentsEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents get_VBComponentsEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _VBComponentsEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents convertComPtrTo_VBComponentsEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBComponentsEventsJCW(comPtr,true,releaseComPtr); }
  protected _VBComponentsEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _VBComponentsEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents.IID); }
  protected _VBComponentsEventsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _VBComponentsEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents.IID); }
  protected _VBComponentsEventsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _VBComponentsEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponentsEvents.IID,releaseComPtr);}
  protected _VBComponentsEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
}
