package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Faces
public class Faces extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IFacesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IFaces {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x51DF56E8,(short)0x87AB,(short)0x11D3,new char[]{0x8B,0xA0,0x00,0xC0,0x4F,0x68,0xDF,0x58});
  public static Faces getFacesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Faces(comPtr,bAddRef); }
  public static Faces getFacesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Faces(comPtr); }
  public static Faces getFacesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Faces(unk); }
  public static Faces convertComPtrToFaces(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Faces(comPtr,true,releaseComPtr); }
  protected Faces(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Faces(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Faces(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Faces(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
