package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Entities
public class Entities extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IEntitiesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IEntities {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x82273A17,(short)0x59FA,(short)0x11D3,new char[]{0xAC,0xB2,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Entities getEntitiesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Entities(comPtr,bAddRef); }
  public static Entities getEntitiesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Entities(comPtr); }
  public static Entities getEntitiesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Entities(unk); }
  public static Entities convertComPtrToEntities(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Entities(comPtr,true,releaseComPtr); }
  protected Entities(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Entities(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Entities(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Entities(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
