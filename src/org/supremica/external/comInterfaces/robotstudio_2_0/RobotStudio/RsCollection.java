package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass RsCollection
public class RsCollection extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsCollectionJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x8A96F014,(short)0xE542,(short)0x11D3,new char[]{0x80,0xE6,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static RsCollection getRsCollectionFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsCollection(comPtr,bAddRef); }
  public static RsCollection getRsCollectionFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsCollection(comPtr); }
  public static RsCollection getRsCollectionFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new RsCollection(unk); }
  public static RsCollection convertComPtrToRsCollection(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsCollection(comPtr,true,releaseComPtr); }
  protected RsCollection(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected RsCollection(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected RsCollection(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected RsCollection(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public RsCollection(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection.IID,Context),false);
  }
  public RsCollection() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection.IID),false);
  }
}
