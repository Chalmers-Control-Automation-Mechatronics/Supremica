package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Entity
public class Entity extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntity2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntity2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x82273A16,(short)0x59FA,(short)0x11D3,new char[]{0xAC,0xB2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Entity getEntityFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Entity(comPtr,bAddRef); }
  public static Entity getEntityFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Entity(comPtr); }
  public static Entity getEntityFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Entity(unk); }
  public static Entity convertComPtrToEntity(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Entity(comPtr,true,releaseComPtr); }
  protected Entity(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Entity(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Entity(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Entity(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  java.util.Vector eventHandlers = new java.util.Vector();
  public void add_EntityEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._EntityEvents listener) throws com.inzoom.comjni.ComJniException {
    org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._EntityEventsHandlerImpl eHandler = new org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._EntityEventsHandlerImpl(listener);
    eHandler.advise(this,listener.DIID);
    eventHandlers.addElement(eHandler);
  }
  public void remove_EntityEventsListener(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio._EntityEvents listener) throws com.inzoom.comjni.ComJniException {
    for(int i = 0; i < eventHandlers.size(); i++){
      if(((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._EntityEventsHandlerImpl)eventHandlers.elementAt(i)).listener == listener){
        ((org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.impl._EntityEventsHandlerImpl)eventHandlers.elementAt(i)).unAdvise(this);
        eventHandlers.removeElementAt(i);
        return;
      }
    }
    throw new IllegalArgumentException();
  }
}
