package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Assembly
public class Assembly extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IAssemblyJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAssembly {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x39413693,(short)0x7D48,(short)0x11D3,new char[]{0xAC,0xD5,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Assembly getAssemblyFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Assembly(comPtr,bAddRef); }
  public static Assembly getAssemblyFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Assembly(comPtr); }
  public static Assembly getAssemblyFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Assembly(unk); }
  public static Assembly convertComPtrToAssembly(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Assembly(comPtr,true,releaseComPtr); }
  protected Assembly(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Assembly(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Assembly(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Assembly(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_AssemblyEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._AssemblyEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._AssemblyEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._AssemblyEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_AssemblyEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._AssemblyEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._AssemblyEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._AssemblyEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
