package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass IO
public class IO extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IIOJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IIO {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x484EA471,(short)0xEF59,(short)0x11D3,new char[]{0x80,0xF6,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public static IO getIOFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IO(comPtr,bAddRef); }
  public static IO getIOFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IO(comPtr); }
  public static IO getIOFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IO(unk); }
  public static IO convertComPtrToIO(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IO(comPtr,true,releaseComPtr); }
  protected IO(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IO(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected IO(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected IO(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void addDIOEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DIOEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DIOEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DIOEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void removeDIOEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.DIOEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DIOEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl.DIOEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
