package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Vertex
public class Vertex extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IVertexJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IVertex {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA40,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Vertex getVertexFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Vertex(comPtr,bAddRef); }
  public static Vertex getVertexFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Vertex(comPtr); }
  public static Vertex getVertexFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Vertex(unk); }
  public static Vertex convertComPtrToVertex(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Vertex(comPtr,true,releaseComPtr); }
  protected Vertex(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Vertex(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Vertex(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Vertex(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
