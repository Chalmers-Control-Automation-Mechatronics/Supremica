package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Coedges
public class Coedges extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ICoedgesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICoedges {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA42,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Coedges getCoedgesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Coedges(comPtr,bAddRef); }
  public static Coedges getCoedgesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Coedges(comPtr); }
  public static Coedges getCoedgesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Coedges(unk); }
  public static Coedges convertComPtrToCoedges(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Coedges(comPtr,true,releaseComPtr); }
  protected Coedges(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Coedges(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Coedges(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Coedges(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
