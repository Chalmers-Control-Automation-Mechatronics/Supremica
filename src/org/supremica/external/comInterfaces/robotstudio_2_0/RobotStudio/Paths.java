package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Paths
public class Paths extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPathsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPaths {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xEEC147AE,(short)0x5164,(short)0x11D3,new char[]{0xAC,0xA6,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Paths getPathsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Paths(comPtr,bAddRef); }
  public static Paths getPathsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Paths(comPtr); }
  public static Paths getPathsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Paths(unk); }
  public static Paths convertComPtrToPaths(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Paths(comPtr,true,releaseComPtr); }
  protected Paths(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Paths(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Paths(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Paths(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
