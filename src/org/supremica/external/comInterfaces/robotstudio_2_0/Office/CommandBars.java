package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// coclass CommandBars
public class CommandBars extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._CommandBarsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBars {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x55F88893,(short)0x7708,(short)0x11D1,new char[]{0xAC,0xEB,0x00,0x60,0x08,0x96,0x1D,0xA5});
  public static CommandBars getCommandBarsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBars(comPtr,bAddRef); }
  public static CommandBars getCommandBarsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBars(comPtr); }
  public static CommandBars getCommandBarsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBars(unk); }
  public static CommandBars convertComPtrToCommandBars(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBars(comPtr,true,releaseComPtr); }
  protected CommandBars(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBars(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBars(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBars(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_CommandBarsEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarsEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarsEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarsEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_CommandBarsEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarsEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarsEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarsEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
