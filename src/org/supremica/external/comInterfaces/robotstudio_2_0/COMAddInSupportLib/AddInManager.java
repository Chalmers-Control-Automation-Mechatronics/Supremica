package org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib;

// coclass AddInManager
public class AddInManager extends org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.jcw.IPseAddInManagerJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x5052EF0E,(short)0x5688,(short)0x11D3,new char[]{0x80,0xD2,0x00,0x50,0x04,0x29,0xD1,0x09});
  public static AddInManager getAddInManagerFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AddInManager(comPtr,bAddRef); }
  public static AddInManager getAddInManagerFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AddInManager(comPtr); }
  public static AddInManager getAddInManagerFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new AddInManager(unk); }
  public static AddInManager convertComPtrToAddInManager(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new AddInManager(comPtr,true,releaseComPtr); }
  protected AddInManager(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected AddInManager(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected AddInManager(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected AddInManager(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public AddInManager(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID,Context),false);
  }
  public AddInManager() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.IPseAddInManager.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_IPseAddInManagerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib._IPseAddInManagerEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.impl._IPseAddInManagerEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.impl._IPseAddInManagerEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_IPseAddInManagerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib._IPseAddInManagerEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.impl._IPseAddInManagerEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.COMAddInSupportLib.impl._IPseAddInManagerEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
