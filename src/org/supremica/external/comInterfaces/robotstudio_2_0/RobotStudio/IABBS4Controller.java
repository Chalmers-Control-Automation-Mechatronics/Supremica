package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IABBS4Controller Declaration
public interface IABBS4Controller extends IController {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x8A22343D,(short)0x9288,(short)0x11D3,new char[]{0xAC,0xEF,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public boolean shutDown() throws com.inzoom.comjni.ComJniException;
  public IMechanism2 getMechanism() throws com.inzoom.comjni.ComJniException;
  public void setMechanism(int pVal) throws com.inzoom.comjni.ComJniException;
  public IABBS4Modules getModules() throws com.inzoom.comjni.ComJniException;
  public void exportProgram(String FileName) throws com.inzoom.comjni.ComJniException;
  public void importProgram(String FileName,boolean Overwrite) throws com.inzoom.comjni.ComJniException;
  public void importProgram(String FileName) throws com.inzoom.comjni.ComJniException;
  public IProcessTypes getProcessTypes() throws com.inzoom.comjni.ComJniException;
  public IDataTypes getDataTypes() throws com.inzoom.comjni.ComJniException;
  public void processAbsAccCalib(String InFileName,String OutFileName,int CalibrationOption) throws com.inzoom.comjni.ComJniException;
  public double getMotionTime() throws com.inzoom.comjni.ComJniException;
}
