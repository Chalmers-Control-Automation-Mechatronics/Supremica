package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Controllers
public class Controllers extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IControllersJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IControllers {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x63AB1B19,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public static Controllers getControllersFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Controllers(comPtr,bAddRef); }
  public static Controllers getControllersFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Controllers(comPtr); }
  public static Controllers getControllersFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Controllers(unk); }
  public static Controllers convertComPtrToControllers(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Controllers(comPtr,true,releaseComPtr); }
  protected Controllers(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Controllers(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Controllers(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Controllers(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
