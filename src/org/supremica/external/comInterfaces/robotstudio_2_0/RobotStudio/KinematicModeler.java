package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass KinematicModeler
public class KinematicModeler extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IKinModelerJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xE0F5F2E9,(short)0xA72A,(short)0x11D3,new char[]{0x80,0xBA,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static KinematicModeler getKinematicModelerFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new KinematicModeler(comPtr,bAddRef); }
  public static KinematicModeler getKinematicModelerFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new KinematicModeler(comPtr); }
  public static KinematicModeler getKinematicModelerFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new KinematicModeler(unk); }
  public static KinematicModeler convertComPtrToKinematicModeler(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new KinematicModeler(comPtr,true,releaseComPtr); }
  protected KinematicModeler(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected KinematicModeler(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected KinematicModeler(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected KinematicModeler(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public KinematicModeler(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID,Context),false);
  }
  public KinematicModeler() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IKinModeler.IID),false);
  }
}
