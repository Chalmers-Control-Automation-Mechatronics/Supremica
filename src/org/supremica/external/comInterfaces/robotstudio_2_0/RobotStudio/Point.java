package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Point
public class Point extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPointJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPoint {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x64D98F20,(short)0xB200,(short)0x11D3,new char[]{0xBF,0x6E,0x00,0xC0,0x4F,0x68,0xDF,0x5A});
  public static Point getPointFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Point(comPtr,bAddRef); }
  public static Point getPointFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Point(comPtr); }
  public static Point getPointFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Point(unk); }
  public static Point convertComPtrToPoint(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Point(comPtr,true,releaseComPtr); }
  protected Point(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Point(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Point(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Point(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
