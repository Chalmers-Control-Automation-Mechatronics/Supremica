package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface DStationEvents Declaration
public class DStationEventsAdapter implements DStationEvents  {
  public void open()   {
  }
  public void close()   {
  }
  public void pick(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.PickData Data)   {
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
}
