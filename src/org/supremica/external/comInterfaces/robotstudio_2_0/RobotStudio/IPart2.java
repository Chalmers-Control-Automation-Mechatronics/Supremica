package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IPart2 Declaration
public interface IPart2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPart {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8B3471C4,(short)0x84CC,(short)0x11D5,new char[]{0xBC,0xC9,0x00,0xD0,0xB7,0xE6,0x41,0x76});
  public String getLibraryName() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getNormalAtPoint(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IPosition Position) throws com.inzoom.comjni.ComJniException;
}
