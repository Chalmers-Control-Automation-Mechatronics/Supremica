package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass CollisionSet
public class CollisionSet extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ICollisionSetJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionSet {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8422D9CC,(short)0xE530,(short)0x11D3,new char[]{0x80,0xEA,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public static CollisionSet getCollisionSetFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionSet(comPtr,bAddRef); }
  public static CollisionSet getCollisionSetFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionSet(comPtr); }
  public static CollisionSet getCollisionSetFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CollisionSet(unk); }
  public static CollisionSet convertComPtrToCollisionSet(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionSet(comPtr,true,releaseComPtr); }
  protected CollisionSet(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CollisionSet(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CollisionSet(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CollisionSet(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
