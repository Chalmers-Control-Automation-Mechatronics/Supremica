package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Shell
public class Shell extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IShellJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IShell {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA38,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Shell getShellFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Shell(comPtr,bAddRef); }
  public static Shell getShellFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Shell(comPtr); }
  public static Shell getShellFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Shell(unk); }
  public static Shell convertComPtrToShell(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Shell(comPtr,true,releaseComPtr); }
  protected Shell(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Shell(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Shell(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Shell(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
