package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface _MechanismEvents Declaration
public class _MechanismEventsAdapter implements _MechanismEvents  {
  public int changed(int ChangeType)   {
    return 0;
  }
  public int collisionEnd(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject CollidingObject)   {
    return 0;
  }
  public int collisionStart(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject CollidingObject)   {
    return 0;
  }
  public int selected()   {
    return 0;
  }
  public int tick(float SystemTime)   {
    return 0;
  }
  public int unSelected()   {
    return 0;
  }
  public int targetReached()   {
    return 0;
  }
  public int iOChange(String Signal,int[] NewValue)   {
    return 0;
  }
  public int jointLimit()   {
    return 0;
  }
  public int singularity(int ErrorNumber,String SingularityMessage)   {
    return 0;
  }
  public int toolChanged()   {
    return 0;
  }
  public int workObjectChanged()   {
    return 0;
  }
  public int controllerError(int ErrorNumber,String ErrorMessage)   {
    return 0;
  }
  public void open()   {
  }
  public int beforeControllerStarted()   {
    return 0;
  }
  public int afterControllerStarted()   {
    return 0;
  }
  public int afterControllerShutdown()   {
    return 0;
  }
}
