package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// coclass DataTypeInstances
public class DataTypeInstances extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.jcw.IDataTypeInstancesJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IDataTypeInstances {
  public static com.inzoom.util.Guid ClassID = new com.inzoom.util.Guid(0xDC708062,(short)0xAE39,(short)0x11D4,new char[]{0xAE,0x3A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public static DataTypeInstances getDataTypeInstancesFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataTypeInstances(comPtr,bAddRef); }
  public static DataTypeInstances getDataTypeInstancesFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataTypeInstances(comPtr); }
  public static DataTypeInstances getDataTypeInstancesFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new DataTypeInstances(unk); }
  public static DataTypeInstances convertComPtrToDataTypeInstances(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new DataTypeInstances(comPtr,true,releaseComPtr); }
  protected DataTypeInstances(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected DataTypeInstances(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr); }
  protected DataTypeInstances(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk); }
  protected DataTypeInstances(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException {  super(comPtr,useQI,releaseComPtr); }
}
