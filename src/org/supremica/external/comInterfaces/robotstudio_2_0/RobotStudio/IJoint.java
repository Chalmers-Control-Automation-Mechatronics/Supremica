package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IJoint Declaration
public interface IJoint extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x3CC2B826,(short)0x8AAA,(short)0x11D3,new char[]{0xAC,0xE4,0x00,0xC0,0x4F,0x68,0xB9,0x87});
  public String getName() throws com.inzoom.comjni.ComJniException;
  public double getJointValue() throws com.inzoom.comjni.ComJniException;
  public int getLimitType() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IAttributes getAttributes() throws com.inzoom.comjni.ComJniException;
  public boolean getActive() throws com.inzoom.comjni.ComJniException;
  public int getIndex() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ITransform getJointAxis() throws com.inzoom.comjni.ComJniException;
  public double getLowerLimit() throws com.inzoom.comjni.ComJniException;
  public void setLowerLimit(double pVal) throws com.inzoom.comjni.ComJniException;
  public double getUpperLimit() throws com.inzoom.comjni.ComJniException;
  public void setUpperLimit(double pVal) throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.ILink getChildLink() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IApplication3 getApplication() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio.IMechanism2 getParent() throws com.inzoom.comjni.ComJniException;
  public int _QueryQuantity(int DispID,boolean[] IsValid) throws com.inzoom.comjni.ComJniException;
}
