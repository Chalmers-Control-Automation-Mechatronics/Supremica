package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass MarkUps
public class MarkUps extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMarkUpsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMarkUps {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x91052CFB,(short)0x9F3E,(short)0x11D4,new char[]{0x81,0xB2,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public static MarkUps getMarkUpsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MarkUps(comPtr,bAddRef); }
  public static MarkUps getMarkUpsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MarkUps(comPtr); }
  public static MarkUps getMarkUpsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new MarkUps(unk); }
  public static MarkUps convertComPtrToMarkUps(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MarkUps(comPtr,true,releaseComPtr); }
  protected MarkUps(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected MarkUps(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected MarkUps(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected MarkUps(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
