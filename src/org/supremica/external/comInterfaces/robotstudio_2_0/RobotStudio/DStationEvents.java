package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface DStationEvents Declaration
public interface DStationEvents  {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0x6193D34F,(short)0x2F7F,(short)0x11D3,new char[]{0xAC,0x9A,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public void open() ;
  public void close() ;
  public void pick(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.PickData Data) ;
  public int libraryBeforeOpen(String FileName,boolean[] Cancel) ;
  public int libraryAfterOpen(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject) ;
  public int libraryBeforeSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject,boolean[] Cancel) ;
  public int libraryAfterSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject) ;
}
