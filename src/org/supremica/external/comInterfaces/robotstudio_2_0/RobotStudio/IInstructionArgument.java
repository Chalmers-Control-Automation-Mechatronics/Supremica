package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IInstructionArgument Declaration
public interface IInstructionArgument extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xCE7D776D,(short)0x9C61,(short)0x11D5,new char[]{0xBC,0xA6,0x00,0xD0,0xB7,0xE6,0x41,0x75});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public void setName(String pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstruction getParent() throws com.inzoom.comjni.ComJniException;
  public void setParent(org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IActionInstruction ppDisp) throws com.inzoom.comjni.ComJniException;
  public String getDataType() throws com.inzoom.comjni.ComJniException;
  public void setDataType(String pVal) throws com.inzoom.comjni.ComJniException;
  public String getDefaultValue() throws com.inzoom.comjni.ComJniException;
  public void setDefaultValue(String pVal) throws com.inzoom.comjni.ComJniException;
  public boolean getOptional() throws com.inzoom.comjni.ComJniException;
  public void setOptional(boolean pVal) throws com.inzoom.comjni.ComJniException;
  public int getMode() throws com.inzoom.comjni.ComJniException;
  public void setMode(int pVal) throws com.inzoom.comjni.ComJniException;
}
