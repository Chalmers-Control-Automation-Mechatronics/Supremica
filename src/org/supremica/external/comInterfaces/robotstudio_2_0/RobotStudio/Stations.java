package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Stations
public class Stations extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IStationsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IStations {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x282D0CC9,(short)0x0771,(short)0x11D3,new char[]{0xAC,0x7A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Stations getStationsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Stations(comPtr,bAddRef); }
  public static Stations getStationsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Stations(comPtr); }
  public static Stations getStationsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Stations(unk); }
  public static Stations convertComPtrToStations(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Stations(comPtr,true,releaseComPtr); }
  protected Stations(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Stations(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Stations(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Stations(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
