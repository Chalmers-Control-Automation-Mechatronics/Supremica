package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Shells
public class Shells extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IShellsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IShells {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA46,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Shells getShellsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Shells(comPtr,bAddRef); }
  public static Shells getShellsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Shells(comPtr); }
  public static Shells getShellsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Shells(unk); }
  public static Shells convertComPtrToShells(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Shells(comPtr,true,releaseComPtr); }
  protected Shells(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Shells(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Shells(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Shells(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
