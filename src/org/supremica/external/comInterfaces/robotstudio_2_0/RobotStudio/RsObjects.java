package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass RsObjects
public class RsObjects extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsObjectsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsObjects {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8A96F012,(short)0xE542,(short)0x11D3,new char[]{0x80,0xE6,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static RsObjects getRsObjectsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsObjects(comPtr,bAddRef); }
  public static RsObjects getRsObjectsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsObjects(comPtr); }
  public static RsObjects getRsObjectsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new RsObjects(unk); }
  public static RsObjects convertComPtrToRsObjects(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsObjects(comPtr,true,releaseComPtr); }
  protected RsObjects(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected RsObjects(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected RsObjects(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected RsObjects(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
