package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass RobotConfiguration
public class RobotConfiguration extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IRobotConfigurationJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRobotConfiguration {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x67124F4C,(short)0xF03B,(short)0x11D3,new char[]{0x80,0xEA,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public static RobotConfiguration getRobotConfigurationFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RobotConfiguration(comPtr,bAddRef); }
  public static RobotConfiguration getRobotConfigurationFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RobotConfiguration(comPtr); }
  public static RobotConfiguration getRobotConfigurationFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new RobotConfiguration(unk); }
  public static RobotConfiguration convertComPtrToRobotConfiguration(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new RobotConfiguration(comPtr,true,releaseComPtr); }
  protected RobotConfiguration(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected RobotConfiguration(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected RobotConfiguration(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected RobotConfiguration(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public RobotConfiguration(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRobotConfiguration.IID,Context),false);
  }
  public RobotConfiguration() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRobotConfiguration.IID),false);
  }
}
