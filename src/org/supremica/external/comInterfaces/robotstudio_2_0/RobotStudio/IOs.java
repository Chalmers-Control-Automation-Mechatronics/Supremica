package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass IOs
public class IOs extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IIOsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IIOs {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x484EA46E,(short)0xEF59,(short)0x11D3,new char[]{0x80,0xF6,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public static IOs getIOsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IOs(comPtr,bAddRef); }
  public static IOs getIOsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IOs(comPtr); }
  public static IOs getIOsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new IOs(unk); }
  public static IOs convertComPtrToIOs(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new IOs(comPtr,true,releaseComPtr); }
  protected IOs(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected IOs(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected IOs(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected IOs(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
