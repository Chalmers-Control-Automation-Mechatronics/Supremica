package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass CommandBarButton
public class CommandBarButton extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.ICommandBarButtonJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDA13E9E0,(short)0xDED4,(short)0x11D3,new char[]{0x80,0xD2,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static CommandBarButton getCommandBarButtonFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarButton(comPtr,bAddRef); }
  public static CommandBarButton getCommandBarButtonFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarButton(comPtr); }
  public static CommandBarButton getCommandBarButtonFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CommandBarButton(unk); }
  public static CommandBarButton convertComPtrToCommandBarButton(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CommandBarButton(comPtr,true,releaseComPtr); }
  protected CommandBarButton(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CommandBarButton(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CommandBarButton(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CommandBarButton(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public CommandBarButton(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton.IID,Context),false);
  }
  public CommandBarButton() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.ICommandBarButton.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_ICommandBarButtonEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarButtonEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarButtonEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarButtonEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_ICommandBarButtonEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._ICommandBarButtonEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarButtonEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._ICommandBarButtonEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
