package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Target
public class Target extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITarget2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xB241506F,(short)0x4E26,(short)0x11D3,new char[]{0xAC,0xA2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Target getTargetFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Target(comPtr,bAddRef); }
  public static Target getTargetFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Target(comPtr); }
  public static Target getTargetFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Target(unk); }
  public static Target convertComPtrToTarget(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Target(comPtr,true,releaseComPtr); }
  protected Target(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Target(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Target(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Target(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addITargetEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.ITargetEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.ITargetEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeITargetEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITargetEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.ITargetEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.ITargetEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
