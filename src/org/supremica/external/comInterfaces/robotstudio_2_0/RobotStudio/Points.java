package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Points
public class Points extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPointsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPoints {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x64D98F1E,(short)0xB200,(short)0x11D3,new char[]{0xBF,0x6E,0x00,0xC0,0x4F,0x68,0xDF,0x5A});
  public static Points getPointsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Points(comPtr,bAddRef); }
  public static Points getPointsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Points(comPtr); }
  public static Points getPointsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Points(unk); }
  public static Points convertComPtrToPoints(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Points(comPtr,true,releaseComPtr); }
  protected Points(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Points(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Points(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Points(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
