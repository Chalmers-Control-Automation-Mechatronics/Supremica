package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass DataTypes
public class DataTypes extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IDataTypesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IDataTypes {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDC70805E,(short)0xAE39,(short)0x11D4,new char[]{0xAE,0x3A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static DataTypes getDataTypesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataTypes(comPtr,bAddRef); }
  public static DataTypes getDataTypesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataTypes(comPtr); }
  public static DataTypes getDataTypesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new DataTypes(unk); }
  public static DataTypes convertComPtrToDataTypes(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataTypes(comPtr,true,releaseComPtr); }
  protected DataTypes(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected DataTypes(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected DataTypes(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected DataTypes(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
