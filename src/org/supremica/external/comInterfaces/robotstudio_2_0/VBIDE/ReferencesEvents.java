package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass ReferencesEvents
public class ReferencesEvents extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._ReferencesEventsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E119,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static ReferencesEvents getReferencesEventsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ReferencesEvents(comPtr,bAddRef); }
  public static ReferencesEvents getReferencesEventsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ReferencesEvents(comPtr); }
  public static ReferencesEvents getReferencesEventsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ReferencesEvents(unk); }
  public static ReferencesEvents convertComPtrToReferencesEvents(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ReferencesEvents(comPtr,true,releaseComPtr); }
  protected ReferencesEvents(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ReferencesEvents(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ReferencesEvents(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ReferencesEvents(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public ReferencesEvents(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents.IID,Context),false);
  }
  public ReferencesEvents() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._ReferencesEvents.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_dispReferencesEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispReferencesEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferencesEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferencesEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_dispReferencesEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispReferencesEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferencesEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferencesEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
