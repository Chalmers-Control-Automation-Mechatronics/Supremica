package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface DAppEvents Declaration
public class DAppEventsAdapter implements DAppEvents  {
  public void selectionChanged()   {
  }
  public void quit()   {
  }
  public int stationBeforeOpen(String Path,boolean[] Cancel)   {
    return 0;
  }
  public int stationAfterOpen(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station)   {
    return 0;
  }
  public int stationBeforeSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station,boolean[] Cancel)   {
    return 0;
  }
  public int stationAfterSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.Station Station)   {
    return 0;
  }
  public int libraryBeforeOpen(String FileName,boolean[] Cancel)   {
    return 0;
  }
  public int libraryAfterOpen(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject)   {
    return 0;
  }
  public int libraryBeforeSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject,boolean[] Cancel)   {
    return 0;
  }
  public int libraryAfterSave(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject RsObject)   {
    return 0;
  }
  public int started()   {
    return 0;
  }
}
