package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Joints
public class Joints extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IJointsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoints {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x3CC2B829,(short)0x8AAA,(short)0x11D3,new char[]{0xAC,0xE4,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Joints getJointsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Joints(comPtr,bAddRef); }
  public static Joints getJointsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Joints(comPtr); }
  public static Joints getJointsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Joints(unk); }
  public static Joints convertComPtrToJoints(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Joints(comPtr,true,releaseComPtr); }
  protected Joints(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Joints(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Joints(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Joints(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
