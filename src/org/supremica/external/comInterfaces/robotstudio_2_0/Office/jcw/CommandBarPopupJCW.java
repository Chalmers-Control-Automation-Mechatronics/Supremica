package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface CommandBarPopup Implementation
public class CommandBarPopupJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup getCommandBarPopupFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarPopupJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup getCommandBarPopupFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarPopupJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup getCommandBarPopupFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarPopupJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup convertComPtrToCommandBarPopup(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarPopupJCW(comPtr,true,releaseComPtr); }
  protected CommandBarPopupJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarPopupJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup.IID); }
  protected CommandBarPopupJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected CommandBarPopupJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup.IID); }
  protected CommandBarPopupJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected CommandBarPopupJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup.IID,releaseComPtr);}
  protected CommandBarPopupJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar getCommandBar() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(332,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBar rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarJCW.getCommandBarFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls getControls() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(336,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarControls rv = org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlsJCW.getCommandBarControlsFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getOLEMenuGroup() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(340,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setOLEMenuGroup(int pomg) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pomg,false)
    };
    vtblCall(344,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarPopup.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
