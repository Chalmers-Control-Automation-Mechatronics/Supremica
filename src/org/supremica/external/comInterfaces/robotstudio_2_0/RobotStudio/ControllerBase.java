package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ControllerBase
public class ControllerBase extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IExtControllerJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExtController {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x63AB1A5C,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public static ControllerBase getControllerBaseFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ControllerBase(comPtr,bAddRef); }
  public static ControllerBase getControllerBaseFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ControllerBase(comPtr); }
  public static ControllerBase getControllerBaseFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ControllerBase(unk); }
  public static ControllerBase convertComPtrToControllerBase(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ControllerBase(comPtr,true,releaseComPtr); }
  protected ControllerBase(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ControllerBase(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ControllerBase(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ControllerBase(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public ControllerBase(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExtController.IID,Context),false);
  }
  public ControllerBase() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExtController.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addDExtControllerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DExtControllerEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DExtControllerEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DExtControllerEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeDExtControllerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DExtControllerEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DExtControllerEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DExtControllerEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
