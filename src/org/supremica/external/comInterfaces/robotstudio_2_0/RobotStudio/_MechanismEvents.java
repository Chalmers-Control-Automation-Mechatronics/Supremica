package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface _MechanismEvents Declaration
public interface _MechanismEvents  {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0x9E2ACE71,(short)0xCA7B,(short)0x11D3,new char[]{0x9A,0xCD,0x00,0xC0,0x4F,0x68,0xDF,0x56});
  public int changed(int ChangeType) ;
  public int collisionEnd(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject CollidingObject) ;
  public int collisionStart(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject CollidingObject) ;
  public int selected() ;
  public int tick(float SystemTime) ;
  public int unSelected() ;
  public int targetReached() ;
  public int iOChange(String Signal,int[] NewValue) ;
  public int jointLimit() ;
  public int singularity(int ErrorNumber,String SingularityMessage) ;
  public int toolChanged() ;
  public int workObjectChanged() ;
  public int controllerError(int ErrorNumber,String ErrorMessage) ;
  public void open() ;
  public int beforeControllerStarted() ;
  public int afterControllerStarted() ;
  public int afterControllerShutdown() ;
}
