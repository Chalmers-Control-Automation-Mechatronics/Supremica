package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass References
public class References extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._ReferencesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._References {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E17C,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static References getReferencesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new References(comPtr,bAddRef); }
  public static References getReferencesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new References(comPtr); }
  public static References getReferencesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new References(unk); }
  public static References convertComPtrToReferences(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new References(comPtr,true,releaseComPtr); }
  protected References(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected References(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected References(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected References(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public References(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._References.IID,Context),false);
  }
  public References() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._References.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_dispReferences_EventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispReferences_Events listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferences_EventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferences_EventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_dispReferences_EventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._dispReferences_Events listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferences_EventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.impl._dispReferences_EventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
