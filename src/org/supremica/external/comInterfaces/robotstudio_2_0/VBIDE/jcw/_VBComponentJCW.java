package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _VBComponent Implementation
public class _VBComponentJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._VBComponent_OldJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent get_VBComponentFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBComponentJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent get_VBComponentFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBComponentJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent get_VBComponentFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _VBComponentJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent convertComPtrTo_VBComponent(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _VBComponentJCW(comPtr,true,releaseComPtr); }
  protected _VBComponentJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _VBComponentJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent.IID); }
  protected _VBComponentJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _VBComponentJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent.IID); }
  protected _VBComponentJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _VBComponentJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent.IID,releaseComPtr);}
  protected _VBComponentJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public String getDesignerID() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._VBComponent.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
