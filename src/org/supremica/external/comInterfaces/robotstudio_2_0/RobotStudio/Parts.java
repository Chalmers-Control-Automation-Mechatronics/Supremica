package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Parts
public class Parts extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IPartsJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IParts {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xBAF25569,(short)0x56DD,(short)0x11D3,new char[]{0xAC,0xAE,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Parts getPartsFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Parts(comPtr,bAddRef); }
  public static Parts getPartsFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Parts(comPtr); }
  public static Parts getPartsFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Parts(unk); }
  public static Parts convertComPtrToParts(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Parts(comPtr,true,releaseComPtr); }
  protected Parts(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Parts(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Parts(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Parts(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
