package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// coclass CommandBarComboBox
public class CommandBarComboBox extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._CommandBarComboBoxJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBox {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x55F88897,(short)0x7708,(short)0x11D1,new char[]{0xAC,0xEB,0x00,0x60,0x08,0x96,0x1D,0xA5});
  public static CommandBarComboBox getCommandBarComboBoxFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarComboBox(comPtr,bAddRef); }
  public static CommandBarComboBox getCommandBarComboBoxFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarComboBox(comPtr); }
  public static CommandBarComboBox getCommandBarComboBoxFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarComboBox(unk); }
  public static CommandBarComboBox convertComPtrToCommandBarComboBox(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarComboBox(comPtr,true,releaseComPtr); }
  protected CommandBarComboBox(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarComboBox(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarComboBox(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarComboBox(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_CommandBarComboBoxEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBoxEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarComboBoxEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarComboBoxEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_CommandBarComboBoxEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarComboBoxEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarComboBoxEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarComboBoxEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
