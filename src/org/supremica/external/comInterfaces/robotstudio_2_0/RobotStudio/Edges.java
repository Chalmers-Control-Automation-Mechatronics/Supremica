package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Edges
public class Edges extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEdgesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdges {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA44,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Edges getEdgesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Edges(comPtr,bAddRef); }
  public static Edges getEdgesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Edges(comPtr); }
  public static Edges getEdgesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Edges(unk); }
  public static Edges convertComPtrToEdges(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Edges(comPtr,true,releaseComPtr); }
  protected Edges(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Edges(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Edges(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Edges(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
