package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass CommandBarComboBox
public class CommandBarComboBox extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarComboBoxJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA13E9E6,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBarComboBox getCommandBarComboBoxFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarComboBox(comPtr,bAddRef); }
  public static CommandBarComboBox getCommandBarComboBoxFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarComboBox(comPtr); }
  public static CommandBarComboBox getCommandBarComboBoxFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarComboBox(unk); }
  public static CommandBarComboBox convertComPtrToCommandBarComboBox(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarComboBox(comPtr,true,releaseComPtr); }
  protected CommandBarComboBox(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarComboBox(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarComboBox(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarComboBox(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBarComboBox(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID,Context),false);
  }
  public CommandBarComboBox() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarComboBox.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_ICommandBarComboBoxEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarComboBoxEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarComboBoxEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarComboBoxEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_ICommandBarComboBoxEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarComboBoxEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarComboBoxEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarComboBoxEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
