package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// coclass CommandBarButton
public class CommandBarButton extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw._CommandBarButtonJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x55F88891,(short)0x7708,(short)0x11D1,new char[]{0xAC,0xEB,0x00,0x60,0x08,0x96,0x1D,0xA5});
  public static CommandBarButton getCommandBarButtonFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarButton(comPtr,bAddRef); }
  public static CommandBarButton getCommandBarButtonFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarButton(comPtr); }
  public static CommandBarButton getCommandBarButtonFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarButton(unk); }
  public static CommandBarButton convertComPtrToCommandBarButton(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarButton(comPtr,true,releaseComPtr); }
  protected CommandBarButton(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarButton(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarButton(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarButton(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_CommandBarButtonEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButtonEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarButtonEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarButtonEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_CommandBarButtonEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButtonEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarButtonEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.Office.impl._CommandBarButtonEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
