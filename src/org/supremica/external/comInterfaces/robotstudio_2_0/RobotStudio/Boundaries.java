package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Boundaries
public class Boundaries extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IBoundariesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundaries {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA48,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Boundaries getBoundariesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Boundaries(comPtr,bAddRef); }
  public static Boundaries getBoundariesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Boundaries(comPtr); }
  public static Boundaries getBoundariesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Boundaries(unk); }
  public static Boundaries convertComPtrToBoundaries(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Boundaries(comPtr,true,releaseComPtr); }
  protected Boundaries(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Boundaries(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Boundaries(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Boundaries(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
