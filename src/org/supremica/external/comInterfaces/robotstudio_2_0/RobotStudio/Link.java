package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Link
public class Link extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ILinkJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILink {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x4D33EBDA,(short)0xC1BA,(short)0x11D3,new char[]{0x80,0xD0,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static Link getLinkFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Link(comPtr,bAddRef); }
  public static Link getLinkFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Link(comPtr); }
  public static Link getLinkFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Link(unk); }
  public static Link convertComPtrToLink(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Link(comPtr,true,releaseComPtr); }
  protected Link(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Link(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Link(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Link(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
