package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass CollisionObjects
public class CollisionObjects extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.ICollisionObjectsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ICollisionObjects {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xFE75F2F7,(short)0xE5E8,(short)0x11D3,new char[]{0x80,0xEC,0x00,0xC0,0x4F,0x60,0xF7,0x91});
  public static CollisionObjects getCollisionObjectsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionObjects(comPtr,bAddRef); }
  public static CollisionObjects getCollisionObjectsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionObjects(comPtr); }
  public static CollisionObjects getCollisionObjectsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new CollisionObjects(unk); }
  public static CollisionObjects convertComPtrToCollisionObjects(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new CollisionObjects(comPtr,true,releaseComPtr); }
  protected CollisionObjects(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected CollisionObjects(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected CollisionObjects(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected CollisionObjects(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
