package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// coclass RSE
public class RSE extends org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.jcw.IRSEJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSE {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xF8D4B88C,(short)0xE7A2,(short)0x11D3,new char[]{0x80,0xD5,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public static RSE getRSEFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RSE(comPtr,bAddRef); }
  public static RSE getRSEFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RSE(comPtr); }
  public static RSE getRSEFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new RSE(unk); }
  public static RSE convertComPtrToRSE(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RSE(comPtr,true,releaseComPtr); }
  protected RSE(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected RSE(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected RSE(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected RSE(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public RSE(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSE.IID,Context),false);
  }
  public RSE() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.IRSE.IID),false);
  }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_IRSEEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._IRSEEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._IRSEEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._IRSEEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_IRSEEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT._IRSEEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._IRSEEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT.impl._IRSEEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
