package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Joint
public class Joint extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IJoint2JCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IJoint2 {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x3CC2B825,(short)0x8AAA,(short)0x11D3,new char[]{0xAC,0xE4,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Joint getJointFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Joint(comPtr,bAddRef); }
  public static Joint getJointFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Joint(comPtr); }
  public static Joint getJointFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Joint(unk); }
  public static Joint convertComPtrToJoint(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Joint(comPtr,true,releaseComPtr); }
  protected Joint(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Joint(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Joint(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Joint(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
