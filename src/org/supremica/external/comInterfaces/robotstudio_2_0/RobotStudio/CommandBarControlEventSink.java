package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass CommandBarControlEventSink
public class CommandBarControlEventSink extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ICommandBarControlEventSinkJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICommandBarControlEventSink {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xB32ED571,(short)0x9D90,(short)0x11D3,new char[]{0x80,0xB9,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBarControlEventSink getCommandBarControlEventSinkFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControlEventSink(comPtr,bAddRef); }
  public static CommandBarControlEventSink getCommandBarControlEventSinkFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControlEventSink(comPtr); }
  public static CommandBarControlEventSink getCommandBarControlEventSinkFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarControlEventSink(unk); }
  public static CommandBarControlEventSink convertComPtrToCommandBarControlEventSink(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarControlEventSink(comPtr,true,releaseComPtr); }
  protected CommandBarControlEventSink(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarControlEventSink(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarControlEventSink(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarControlEventSink(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBarControlEventSink(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICommandBarControlEventSink.IID,Context),false);
  }
  public CommandBarControlEventSink() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICommandBarControlEventSink.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_ICommandBarControlEventSinkEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._ICommandBarControlEventSinkEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._ICommandBarControlEventSinkEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._ICommandBarControlEventSinkEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_ICommandBarControlEventSinkEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._ICommandBarControlEventSinkEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._ICommandBarControlEventSinkEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._ICommandBarControlEventSinkEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
