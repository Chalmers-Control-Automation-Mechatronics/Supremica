package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass CommandBarControls
public class CommandBarControls extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarControlsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA13E9EC,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBarControls getCommandBarControlsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControls(comPtr,bAddRef); }
  public static CommandBarControls getCommandBarControlsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControls(comPtr); }
  public static CommandBarControls getCommandBarControlsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarControls(unk); }
  public static CommandBarControls convertComPtrToCommandBarControls(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControls(comPtr,true,releaseComPtr); }
  protected CommandBarControls(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarControls(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarControls(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarControls(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBarControls(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID,Context),false);
  }
  public CommandBarControls() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarControls.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_ICommandBarControlsEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarControlsEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarControlsEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarControlsEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_ICommandBarControlsEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarControlsEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarControlsEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarControlsEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
