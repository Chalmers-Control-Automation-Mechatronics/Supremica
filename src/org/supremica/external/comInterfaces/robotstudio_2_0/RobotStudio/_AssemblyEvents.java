package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface _AssemblyEvents Declaration
public interface _AssemblyEvents  {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0x49BA66AA,(short)0xBDEF,(short)0x11D3,new char[]{0x80,0xC0,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void changed(int ChangeType) ;
  public void collisionEnd(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject CollidingObject) ;
  public void collisionStart(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.RsObject CollidingObject) ;
  public void selected() ;
  public void unSelected() ;
  public void open() ;
}
