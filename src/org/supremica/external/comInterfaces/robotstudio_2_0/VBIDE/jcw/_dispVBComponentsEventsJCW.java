package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// Dispinterface _dispVBComponentsEvents Implementation
public class _dispVBComponentsEventsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBComponentsEvents, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBComponentsEvents get_dispVBComponentsEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _dispVBComponentsEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBComponentsEvents get_dispVBComponentsEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _dispVBComponentsEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBComponentsEvents get_dispVBComponentsEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _dispVBComponentsEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBComponentsEvents convertComPtrTo_dispVBComponentsEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _dispVBComponentsEventsJCW(comPtr,true,releaseComPtr); }
  protected _dispVBComponentsEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _dispVBComponentsEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,com.inzoom.comjni.IDispatch.IID); }
  protected _dispVBComponentsEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,com.inzoom.comjni.IDispatch.IID); }
  protected _dispVBComponentsEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,com.inzoom.comjni.IDispatch.IID,releaseComPtr); }
  protected _dispVBComponentsEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void itemAdded(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent VBComponent) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBComponent,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(1,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemRemoved(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent VBComponent) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBComponent,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(2,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemRenamed(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent VBComponent,String OldName) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(OldName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBComponent,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(3,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemSelected(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent VBComponent) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBComponent,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(4,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemActivated(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent VBComponent) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBComponent,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(5,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemReloaded(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent VBComponent) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBComponent,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(6,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
