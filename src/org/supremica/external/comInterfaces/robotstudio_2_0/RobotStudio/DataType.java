package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass DataType
public class DataType extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IDataTypeJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IDataType {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDC708060,(short)0xAE39,(short)0x11D4,new char[]{0xAE,0x3A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static DataType getDataTypeFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataType(comPtr,bAddRef); }
  public static DataType getDataTypeFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataType(comPtr); }
  public static DataType getDataTypeFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new DataType(unk); }
  public static DataType convertComPtrToDataType(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataType(comPtr,true,releaseComPtr); }
  protected DataType(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected DataType(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected DataType(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected DataType(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
