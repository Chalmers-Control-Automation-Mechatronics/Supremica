package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface DAppEvents Declaration
public interface DAppEvents  {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0x77A7892B,(short)0x2DED,(short)0x11D3,new char[]{0xAC,0x98,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public void selectionChanged() ;
  public void quit() ;
  public int stationBeforeOpen(String Path,boolean[] Cancel) ;
  public int stationAfterOpen(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station) ;
  public int stationBeforeSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station,boolean[] Cancel) ;
  public int stationAfterSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station) ;
  public int libraryBeforeOpen(String FileName,boolean[] Cancel) ;
  public int libraryAfterOpen(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject) ;
  public int libraryBeforeSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject,boolean[] Cancel) ;
  public int libraryAfterSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject) ;
  public int started() ;
}
