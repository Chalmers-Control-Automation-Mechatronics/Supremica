package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Edge
public class Edge extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEdge2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEdge2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x092DBA3E,(short)0xE918,(short)0x11D3,new char[]{0xA1,0xDD,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static Edge getEdgeFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Edge(comPtr,bAddRef); }
  public static Edge getEdgeFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Edge(comPtr); }
  public static Edge getEdgeFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Edge(unk); }
  public static Edge convertComPtrToEdge(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Edge(comPtr,true,releaseComPtr); }
  protected Edge(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Edge(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Edge(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Edge(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
