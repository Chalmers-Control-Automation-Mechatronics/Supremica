package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Mechanism
public class Mechanism extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMechanism2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xE4101649,(short)0x884E,(short)0x11D3,new char[]{0xAC,0xE2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Mechanism getMechanismFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Mechanism(comPtr,bAddRef); }
  public static Mechanism getMechanismFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Mechanism(comPtr); }
  public static Mechanism getMechanismFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Mechanism(unk); }
  public static Mechanism convertComPtrToMechanism(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Mechanism(comPtr,true,releaseComPtr); }
  protected Mechanism(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Mechanism(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Mechanism(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Mechanism(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_MechanismEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._MechanismEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._MechanismEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._MechanismEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_MechanismEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._MechanismEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._MechanismEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._MechanismEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
