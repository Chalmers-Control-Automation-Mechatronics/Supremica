package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Path
public class Path extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPath2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPath2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xEEC147AD,(short)0x5164,(short)0x11D3,new char[]{0xAC,0xA6,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Path getPathFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Path(comPtr,bAddRef); }
  public static Path getPathFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Path(comPtr); }
  public static Path getPathFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Path(unk); }
  public static Path convertComPtrToPath(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Path(comPtr,true,releaseComPtr); }
  protected Path(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Path(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Path(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Path(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
