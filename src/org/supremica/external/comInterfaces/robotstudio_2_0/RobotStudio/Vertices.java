package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Vertices
public class Vertices extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IVerticesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IVertices {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x33553BF8,(short)0xE92F,(short)0x11D3,new char[]{0xA1,0xDF,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Vertices getVerticesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Vertices(comPtr,bAddRef); }
  public static Vertices getVerticesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Vertices(comPtr); }
  public static Vertices getVerticesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Vertices(unk); }
  public static Vertices convertComPtrToVertices(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Vertices(comPtr,true,releaseComPtr); }
  protected Vertices(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Vertices(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Vertices(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Vertices(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
