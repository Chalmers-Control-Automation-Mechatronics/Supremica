package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass CollisionSets
public class CollisionSets extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ICollisionSetsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionSets {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xFE75F33F,(short)0xE5E8,(short)0x11D3,new char[]{0x80,0xEC,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public static CollisionSets getCollisionSetsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionSets(comPtr,bAddRef); }
  public static CollisionSets getCollisionSetsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionSets(comPtr); }
  public static CollisionSets getCollisionSetsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CollisionSets(unk); }
  public static CollisionSets convertComPtrToCollisionSets(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionSets(comPtr,true,releaseComPtr); }
  protected CollisionSets(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CollisionSets(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CollisionSets(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CollisionSets(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
