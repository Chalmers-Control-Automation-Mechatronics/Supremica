package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _ReferencesEvents Implementation
public class _ReferencesEventsJCW extends com.inzoom.comjni.jcw.IUnknownJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents, com.inzoom.comjni.IUnknown {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents get_ReferencesEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _ReferencesEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents get_ReferencesEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _ReferencesEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents get_ReferencesEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _ReferencesEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents convertComPtrTo_ReferencesEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _ReferencesEventsJCW(comPtr,true,releaseComPtr); }
  protected _ReferencesEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _ReferencesEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents.IID); }
  protected _ReferencesEventsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _ReferencesEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents.IID); }
  protected _ReferencesEventsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _ReferencesEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents.IID,releaseComPtr);}
  protected _ReferencesEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
}
