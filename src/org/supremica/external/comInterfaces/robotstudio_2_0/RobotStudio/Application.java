package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Application
public class Application extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IApplication3JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x282D0CBE,(short)0x0771,(short)0x11D3,new char[]{0xAC,0x7A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Application getApplicationFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Application(comPtr,bAddRef); }
  public static Application getApplicationFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Application(comPtr); }
  public static Application getApplicationFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Application(unk); }
  public static Application convertComPtrToApplication(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Application(comPtr,true,releaseComPtr); }
  protected Application(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Application(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Application(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Application(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Application(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID,Context),false);
  }
  public Application() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addDAppEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DAppEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DAppEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DAppEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeDAppEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DAppEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DAppEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DAppEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
