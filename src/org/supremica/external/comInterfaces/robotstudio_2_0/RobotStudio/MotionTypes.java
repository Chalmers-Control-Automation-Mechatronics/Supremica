package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass MotionTypes
public class MotionTypes extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IMotionTypesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMotionTypes {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xE25ED0B8,(short)0xA8AE,(short)0x11D4,new char[]{0xAE,0x34,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static MotionTypes getMotionTypesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MotionTypes(comPtr,bAddRef); }
  public static MotionTypes getMotionTypesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MotionTypes(comPtr); }
  public static MotionTypes getMotionTypesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new MotionTypes(unk); }
  public static MotionTypes convertComPtrToMotionTypes(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new MotionTypes(comPtr,true,releaseComPtr); }
  protected MotionTypes(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected MotionTypes(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected MotionTypes(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected MotionTypes(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
