package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE;

// coclass Properties
public class Properties extends org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw._PropertiesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Properties {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0x0002E18B,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public static Properties getPropertiesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Properties(comPtr,bAddRef); }
  public static Properties getPropertiesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Properties(comPtr); }
  public static Properties getPropertiesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new Properties(unk); }
  public static Properties convertComPtrToProperties(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new Properties(comPtr,true,releaseComPtr); }
  protected Properties(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected Properties(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected Properties(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected Properties(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
  public Properties(short Context) throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Properties.IID,Context),false);
  }
  public Properties() throws com.inzoom.comjni.ComJniException{ 
    super(com.inzoom.comjni.ComLib.coCreateInstance(ClassID,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._Properties.IID),false);
  }
}
