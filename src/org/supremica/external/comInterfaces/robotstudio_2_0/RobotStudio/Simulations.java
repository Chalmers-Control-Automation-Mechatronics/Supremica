package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Simulations
public class Simulations extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ISimulationsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ISimulations {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x63AB1A02,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public static Simulations getSimulationsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Simulations(comPtr,bAddRef); }
  public static Simulations getSimulationsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Simulations(comPtr); }
  public static Simulations getSimulationsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Simulations(unk); }
  public static Simulations convertComPtrToSimulations(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Simulations(comPtr,true,releaseComPtr); }
  protected Simulations(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Simulations(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Simulations(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Simulations(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
