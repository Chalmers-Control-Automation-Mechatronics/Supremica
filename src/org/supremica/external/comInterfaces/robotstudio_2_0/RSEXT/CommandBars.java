package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass CommandBars
public class CommandBars extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA13E9E9,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBars getCommandBarsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBars(comPtr,bAddRef); }
  public static CommandBars getCommandBarsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBars(comPtr); }
  public static CommandBars getCommandBarsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBars(unk); }
  public static CommandBars convertComPtrToCommandBars(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBars(comPtr,true,releaseComPtr); }
  protected CommandBars(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBars(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBars(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBars(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBars(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID,Context),false);
  }
  public CommandBars() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBars.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_ICommandBarsEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarsEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarsEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarsEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_ICommandBarsEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarsEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarsEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarsEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
