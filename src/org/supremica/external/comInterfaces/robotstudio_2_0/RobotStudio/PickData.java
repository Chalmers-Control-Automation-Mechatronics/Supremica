package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass PickData
public class PickData extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPickDataJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPickData {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x6D336C8C,(short)0xF04C,(short)0x11D3,new char[]{0x80,0xF7,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public static PickData getPickDataFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PickData(comPtr,bAddRef); }
  public static PickData getPickDataFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PickData(comPtr); }
  public static PickData getPickDataFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new PickData(unk); }
  public static PickData convertComPtrToPickData(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new PickData(comPtr,true,releaseComPtr); }
  protected PickData(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected PickData(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected PickData(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected PickData(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
