package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ControllerEventSink
public class ControllerEventSink extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IControllerEventSinkJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IControllerEventSink {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xCF19EBAF,(short)0xA80D,(short)0x11D3,new char[]{0x80,0xB8,0x00,0xC0,0x4F,0x60,0xFA,0xB6});
  public static ControllerEventSink getControllerEventSinkFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ControllerEventSink(comPtr,bAddRef); }
  public static ControllerEventSink getControllerEventSinkFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ControllerEventSink(comPtr); }
  public static ControllerEventSink getControllerEventSinkFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ControllerEventSink(unk); }
  public static ControllerEventSink convertComPtrToControllerEventSink(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ControllerEventSink(comPtr,true,releaseComPtr); }
  protected ControllerEventSink(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ControllerEventSink(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ControllerEventSink(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ControllerEventSink(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
