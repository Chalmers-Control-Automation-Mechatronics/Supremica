package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IMechanism2 Declaration
public interface IMechanism2 extends org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xA02973AE,(short)0x85A8,(short)0x11D5,new char[]{0xBC,0xCA,0x00,0xD0,0xB7,0xE6,0x41,0x76});
  public String getLibraryName() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection getSolutions(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 Target,com.inzoom.comjni.Variant Cfx) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IRsCollection getSolutions(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITarget2 Target) throws com.inzoom.comjni.ComJniException;
}
