package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Controller
public class Controller extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IControllerJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IController {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x63AB1A13,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public static Controller getControllerFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Controller(comPtr,bAddRef); }
  public static Controller getControllerFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Controller(comPtr); }
  public static Controller getControllerFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Controller(unk); }
  public static Controller convertComPtrToController(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Controller(comPtr,true,releaseComPtr); }
  protected Controller(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Controller(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Controller(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Controller(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addDControllerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DControllerEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DControllerEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DControllerEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeDControllerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DControllerEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DControllerEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DControllerEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
