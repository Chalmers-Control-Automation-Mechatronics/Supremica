package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ABBS4Controller
public class ABBS4Controller extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IABBS4Controller2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IABBS4Controller2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8A22343C,(short)0x9288,(short)0x11D3,new char[]{0xAC,0xEF,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static ABBS4Controller getABBS4ControllerFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Controller(comPtr,bAddRef); }
  public static ABBS4Controller getABBS4ControllerFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Controller(comPtr); }
  public static ABBS4Controller getABBS4ControllerFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ABBS4Controller(unk); }
  public static ABBS4Controller convertComPtrToABBS4Controller(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ABBS4Controller(comPtr,true,releaseComPtr); }
  protected ABBS4Controller(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ABBS4Controller(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ABBS4Controller(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ABBS4Controller(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addDABBS4ControllerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DABBS4ControllerEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DABBS4ControllerEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DABBS4ControllerEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeDABBS4ControllerEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DABBS4ControllerEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DABBS4ControllerEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DABBS4ControllerEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
