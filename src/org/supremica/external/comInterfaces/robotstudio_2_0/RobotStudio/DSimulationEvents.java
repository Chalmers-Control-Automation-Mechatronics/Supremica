package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface DSimulationEvents Declaration
public interface DSimulationEvents  {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0x63AB1A05,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public void pause() ;
  public void resume() ;
  public void start() ;
  public void stop() ;
  public void error(String ErrorMessage) ;
  public void tick(double Time) ;
}
