package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Coedge
public class Coedge extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ICoedgeJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICoedge {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA3C,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Coedge getCoedgeFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Coedge(comPtr,bAddRef); }
  public static Coedge getCoedgeFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Coedge(comPtr); }
  public static Coedge getCoedgeFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Coedge(unk); }
  public static Coedge convertComPtrToCoedge(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Coedge(comPtr,true,releaseComPtr); }
  protected Coedge(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Coedge(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Coedge(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Coedge(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
