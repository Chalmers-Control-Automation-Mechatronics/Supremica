package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass View
public class View extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IViewJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IView {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xA88F09F8,(short)0xE2C5,(short)0x11D3,new char[]{0x80,0xC3,0x00,0xC0,0x4F,0x60,0xF7,0x93});
  public static View getViewFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new View(comPtr,bAddRef); }
  public static View getViewFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new View(comPtr); }
  public static View getViewFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new View(unk); }
  public static View convertComPtrToView(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new View(comPtr,true,releaseComPtr); }
  protected View(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected View(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected View(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected View(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_IViewEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IViewEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IViewEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IViewEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_IViewEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IViewEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IViewEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IViewEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
