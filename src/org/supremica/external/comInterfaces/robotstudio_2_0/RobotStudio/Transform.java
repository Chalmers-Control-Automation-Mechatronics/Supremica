package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Transform
public class Transform extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ITransformJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x7EB93CA7,(short)0x218D,(short)0x11D4,new char[]{0xAD,0x90,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Transform getTransformFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Transform(comPtr,bAddRef); }
  public static Transform getTransformFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Transform(comPtr); }
  public static Transform getTransformFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Transform(unk); }
  public static Transform convertComPtrToTransform(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Transform(comPtr,true,releaseComPtr); }
  protected Transform(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Transform(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Transform(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Transform(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Transform(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform.IID,Context),false);
  }
  public Transform() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform.IID),false);
  }
}
