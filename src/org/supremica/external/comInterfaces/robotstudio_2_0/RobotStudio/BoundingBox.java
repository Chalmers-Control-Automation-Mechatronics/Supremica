package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass BoundingBox
public class BoundingBox extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IBoundingBoxJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x7F15B67A,(short)0x06FE,(short)0x11D4,new char[]{0xA1,0xE6,0x00,0xC0,0x4F,0x68,0xDF,0x5B});
  public static BoundingBox getBoundingBoxFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new BoundingBox(comPtr,bAddRef); }
  public static BoundingBox getBoundingBoxFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new BoundingBox(comPtr); }
  public static BoundingBox getBoundingBoxFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new BoundingBox(unk); }
  public static BoundingBox convertComPtrToBoundingBox(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new BoundingBox(comPtr,true,releaseComPtr); }
  protected BoundingBox(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected BoundingBox(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected BoundingBox(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected BoundingBox(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public BoundingBox(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox.IID,Context),false);
  }
  public BoundingBox() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IBoundingBox.IID),false);
  }
}
