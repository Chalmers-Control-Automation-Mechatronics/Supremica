package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Part
public class Part extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPart2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBAF2556A,(short)0x56DD,(short)0x11D3,new char[]{0xAC,0xAE,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Part getPartFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Part(comPtr,bAddRef); }
  public static Part getPartFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Part(comPtr); }
  public static Part getPartFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Part(unk); }
  public static Part convertComPtrToPart(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Part(comPtr,true,releaseComPtr); }
  protected Part(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Part(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Part(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Part(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_PartEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._PartEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._PartEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._PartEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_PartEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._PartEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._PartEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._PartEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
