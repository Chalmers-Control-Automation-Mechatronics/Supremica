package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Boundary
public class Boundary extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IBoundaryJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundary {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA3A,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Boundary getBoundaryFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Boundary(comPtr,bAddRef); }
  public static Boundary getBoundaryFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Boundary(comPtr); }
  public static Boundary getBoundaryFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Boundary(unk); }
  public static Boundary convertComPtrToBoundary(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Boundary(comPtr,true,releaseComPtr); }
  protected Boundary(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Boundary(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Boundary(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Boundary(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
