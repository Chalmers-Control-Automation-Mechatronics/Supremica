package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Orientation
public class Orientation extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IOrientationJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IOrientation {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x84F85ADD,(short)0x263D,(short)0x11D4,new char[]{0xAD,0x96,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Orientation getOrientationFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Orientation(comPtr,bAddRef); }
  public static Orientation getOrientationFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Orientation(comPtr); }
  public static Orientation getOrientationFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Orientation(unk); }
  public static Orientation convertComPtrToOrientation(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Orientation(comPtr,true,releaseComPtr); }
  protected Orientation(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Orientation(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Orientation(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Orientation(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Orientation(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IOrientation.IID,Context),false);
  }
  public Orientation() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IOrientation.IID),false);
  }
}
