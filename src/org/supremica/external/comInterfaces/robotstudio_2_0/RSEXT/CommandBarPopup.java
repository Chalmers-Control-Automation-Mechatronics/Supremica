package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass CommandBarPopup
public class CommandBarPopup extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarPopupJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarPopup {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA13E9E3,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBarPopup getCommandBarPopupFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarPopup(comPtr,bAddRef); }
  public static CommandBarPopup getCommandBarPopupFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarPopup(comPtr); }
  public static CommandBarPopup getCommandBarPopupFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarPopup(unk); }
  public static CommandBarPopup convertComPtrToCommandBarPopup(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarPopup(comPtr,true,releaseComPtr); }
  protected CommandBarPopup(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarPopup(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarPopup(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarPopup(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBarPopup(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarPopup.IID,Context),false);
  }
  public CommandBarPopup() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarPopup.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_ICommandBarPopupEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarPopupEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarPopupEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarPopupEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_ICommandBarPopupEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarPopupEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarPopupEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarPopupEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
