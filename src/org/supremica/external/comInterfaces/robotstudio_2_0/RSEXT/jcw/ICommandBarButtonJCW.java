package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw;

// interface ICommandBarButton Implementation
public class ICommandBarButtonJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton getICommandBarButtonFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarButtonJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton getICommandBarButtonFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarButtonJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton getICommandBarButtonFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarButtonJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton convertComPtrToICommandBarButton(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarButtonJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarButtonJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarButtonJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton.IID); }
  protected ICommandBarButtonJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarButtonJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton.IID); }
  protected ICommandBarButtonJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarButtonJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton.IID,releaseComPtr);}
  protected ICommandBarButtonJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public int getState() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setState(int pState) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pState,false)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
