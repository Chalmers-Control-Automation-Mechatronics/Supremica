package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Station
public class Station extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IStation2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStation2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x282D0CC6,(short)0x0771,(short)0x11D3,new char[]{0xAC,0x7A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Station getStationFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Station(comPtr,bAddRef); }
  public static Station getStationFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Station(comPtr); }
  public static Station getStationFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Station(unk); }
  public static Station convertComPtrToStation(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Station(comPtr,true,releaseComPtr); }
  protected Station(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Station(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Station(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Station(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addDStationEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DStationEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DStationEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DStationEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeDStationEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DStationEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DStationEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DStationEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
