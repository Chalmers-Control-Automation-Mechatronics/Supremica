package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass CommandBar
public class CommandBar extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA13E9DA,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBar getCommandBarFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBar(comPtr,bAddRef); }
  public static CommandBar getCommandBarFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBar(comPtr); }
  public static CommandBar getCommandBarFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBar(unk); }
  public static CommandBar convertComPtrToCommandBar(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBar(comPtr,true,releaseComPtr); }
  protected CommandBar(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBar(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBar(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBar(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBar(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID,Context),false);
  }
  public CommandBar() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBar.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_ICommandBarEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_ICommandBarEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
