package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass Filter
public class Filter extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRsFilterJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsFilter {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x78A4FEA9,(short)0xA30E,(short)0x11D3,new char[]{0xAD,0x05,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static Filter getFilterFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Filter(comPtr,bAddRef); }
  public static Filter getFilterFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Filter(comPtr); }
  public static Filter getFilterFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Filter(unk); }
  public static Filter convertComPtrToFilter(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Filter(comPtr,true,releaseComPtr); }
  protected Filter(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Filter(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Filter(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Filter(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Filter(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsFilter.IID,Context),false);
  }
  public Filter() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsFilter.IID),false);
  }
}
