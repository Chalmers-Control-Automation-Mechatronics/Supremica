package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Links
public class Links extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ILinksJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILinks {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x4D33EBD9,(short)0xC1BA,(short)0x11D3,new char[]{0x80,0xD0,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static Links getLinksFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Links(comPtr,bAddRef); }
  public static Links getLinksFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Links(comPtr); }
  public static Links getLinksFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Links(unk); }
  public static Links convertComPtrToLinks(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Links(comPtr,true,releaseComPtr); }
  protected Links(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Links(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Links(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Links(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
