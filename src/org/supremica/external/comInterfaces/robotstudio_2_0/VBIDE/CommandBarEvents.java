package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass CommandBarEvents
public class CommandBarEvents extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._CommandBarControlEventsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CommandBarControlEvents {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E132,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static CommandBarEvents getCommandBarEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarEvents(comPtr,bAddRef); }
  public static CommandBarEvents getCommandBarEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarEvents(comPtr); }
  public static CommandBarEvents getCommandBarEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarEvents(unk); }
  public static CommandBarEvents convertComPtrToCommandBarEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarEvents(comPtr,true,releaseComPtr); }
  protected CommandBarEvents(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarEvents(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarEvents(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarEvents(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBarEvents(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CommandBarControlEvents.IID,Context),false);
  }
  public CommandBarEvents() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CommandBarControlEvents.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_dispCommandBarControlEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispCommandBarControlEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispCommandBarControlEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispCommandBarControlEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_dispCommandBarControlEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispCommandBarControlEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispCommandBarControlEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispCommandBarControlEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
