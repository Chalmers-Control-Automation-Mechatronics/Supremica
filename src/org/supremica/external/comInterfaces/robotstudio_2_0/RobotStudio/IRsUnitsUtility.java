package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IRsUnitsUtility Declaration
public interface IRsUnitsUtility extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x4A458DBC,(short)0xF657,(short)0x11D3,new char[]{0xAD,0x5F,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public double unitToAPI(int Quantity,double UnitValue) throws com.inzoom.comjni.ComJniException;
  public double aPIToUnit(int Quantity,double APIValue) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform uCSToWCS(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform UCS) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform wCSToUCS(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform WCS) throws com.inzoom.comjni.ComJniException;
}
