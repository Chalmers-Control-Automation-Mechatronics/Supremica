package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass RsUnitsUtility
public class RsUnitsUtility extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsUnitsUtilityJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x4A458DBB,(short)0xF657,(short)0x11D3,new char[]{0xAD,0x5F,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static RsUnitsUtility getRsUnitsUtilityFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsUnitsUtility(comPtr,bAddRef); }
  public static RsUnitsUtility getRsUnitsUtilityFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsUnitsUtility(comPtr); }
  public static RsUnitsUtility getRsUnitsUtilityFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new RsUnitsUtility(unk); }
  public static RsUnitsUtility convertComPtrToRsUnitsUtility(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RsUnitsUtility(comPtr,true,releaseComPtr); }
  protected RsUnitsUtility(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected RsUnitsUtility(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected RsUnitsUtility(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected RsUnitsUtility(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public RsUnitsUtility(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID,Context),false);
  }
  public RsUnitsUtility() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsUnitsUtility.IID),false);
  }
}
