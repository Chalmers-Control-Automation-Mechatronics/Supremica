package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Simulation
public class Simulation extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ISimulationJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISimulation {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x63AB1A07,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public static Simulation getSimulationFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Simulation(comPtr,bAddRef); }
  public static Simulation getSimulationFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Simulation(comPtr); }
  public static Simulation getSimulationFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Simulation(unk); }
  public static Simulation convertComPtrToSimulation(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Simulation(comPtr,true,releaseComPtr); }
  protected Simulation(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Simulation(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Simulation(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Simulation(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addDSimulationEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DSimulationEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DSimulationEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DSimulationEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeDSimulationEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DSimulationEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DSimulationEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DSimulationEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
