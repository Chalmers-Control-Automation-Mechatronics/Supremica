package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// Dispinterface _dispVBProjectsEvents Implementation
public class _dispVBProjectsEventsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBProjectsEvents, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBProjectsEvents get_dispVBProjectsEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _dispVBProjectsEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBProjectsEvents get_dispVBProjectsEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _dispVBProjectsEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBProjectsEvents get_dispVBProjectsEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _dispVBProjectsEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispVBProjectsEvents convertComPtrTo_dispVBProjectsEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _dispVBProjectsEventsJCW(comPtr,true,releaseComPtr); }
  protected _dispVBProjectsEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _dispVBProjectsEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,com.inzoom.comjni.IDispatch.IID); }
  protected _dispVBProjectsEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,com.inzoom.comjni.IDispatch.IID); }
  protected _dispVBProjectsEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,com.inzoom.comjni.IDispatch.IID,releaseComPtr); }
  protected _dispVBProjectsEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void itemAdded(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBProject,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(1,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemRemoved(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBProject,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(2,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemRenamed(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject,String OldName) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(OldName,false),
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBProject,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(3,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void itemActivated(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBProject VBProject) throws com.inzoom.comjni.ComJniException  {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)VBProject,false)
    };
    com.inzoom.comjni.Variant _vrv = invoke(4,1024,1,_v,null);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
