package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass TargetBase
public class TargetBase extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRSExtTargetJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRSExtTarget {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x57C72870,(short)0xA355,(short)0x11D3,new char[]{0x80,0xBB,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static TargetBase getTargetBaseFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetBase(comPtr,bAddRef); }
  public static TargetBase getTargetBaseFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetBase(comPtr); }
  public static TargetBase getTargetBaseFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new TargetBase(unk); }
  public static TargetBase convertComPtrToTargetBase(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new TargetBase(comPtr,true,releaseComPtr); }
  protected TargetBase(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected TargetBase(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected TargetBase(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected TargetBase(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public TargetBase(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRSExtTarget.IID,Context),false);
  }
  public TargetBase() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRSExtTarget.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_IRSExtTargetEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IRSExtTargetEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IRSExtTargetEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IRSExtTargetEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_IRSExtTargetEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._IRSExtTargetEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IRSExtTargetEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._IRSExtTargetEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
