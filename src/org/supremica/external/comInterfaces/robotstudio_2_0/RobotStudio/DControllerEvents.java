package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface DControllerEvents Declaration
public interface DControllerEvents  {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0x63AB1A12,(short)0xE782,(short)0x11D3,new char[]{0x80,0xEF,0x00,0xC0,0x4F,0x60,0xF7,0x8D});
  public void afterTick(double Time) ;
  public void beforeTick(double Time,double[] MaxTime) ;
  public void create() ;
  public void start() ;
  public void stop() ;
}
