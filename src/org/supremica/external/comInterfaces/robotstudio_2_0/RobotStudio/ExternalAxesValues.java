package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass ExternalAxesValues
public class ExternalAxesValues extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IExternalAxesValuesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x67124F4E,(short)0xF03B,(short)0x11D3,new char[]{0x80,0xEA,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static ExternalAxesValues getExternalAxesValuesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ExternalAxesValues(comPtr,bAddRef); }
  public static ExternalAxesValues getExternalAxesValuesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ExternalAxesValues(comPtr); }
  public static ExternalAxesValues getExternalAxesValuesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new ExternalAxesValues(unk); }
  public static ExternalAxesValues convertComPtrToExternalAxesValues(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new ExternalAxesValues(comPtr,true,releaseComPtr); }
  protected ExternalAxesValues(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected ExternalAxesValues(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected ExternalAxesValues(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected ExternalAxesValues(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public ExternalAxesValues(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues.IID,Context),false);
  }
  public ExternalAxesValues() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IExternalAxesValues.IID),false);
  }
}
