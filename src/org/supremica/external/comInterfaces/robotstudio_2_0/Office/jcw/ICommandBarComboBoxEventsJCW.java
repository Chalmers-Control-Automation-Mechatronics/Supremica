package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface ICommandBarComboBoxEvents Implementation
public class ICommandBarComboBoxEventsJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents getICommandBarComboBoxEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarComboBoxEventsJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents getICommandBarComboBoxEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarComboBoxEventsJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents getICommandBarComboBoxEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ICommandBarComboBoxEventsJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents convertComPtrToICommandBarComboBoxEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ICommandBarComboBoxEventsJCW(comPtr,true,releaseComPtr); }
  protected ICommandBarComboBoxEventsJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ICommandBarComboBoxEventsJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents.IID); }
  protected ICommandBarComboBoxEventsJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected ICommandBarComboBoxEventsJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents.IID); }
  protected ICommandBarComboBoxEventsJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected ICommandBarComboBoxEventsJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents.IID,releaseComPtr);}
  protected ICommandBarComboBoxEventsJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public void change(org.supremica.external.comInterfaces.robotstudio_2_0.Office.CommandBarComboBox Ctrl) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)Ctrl,false)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office.ICommandBarComboBoxEvents.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
