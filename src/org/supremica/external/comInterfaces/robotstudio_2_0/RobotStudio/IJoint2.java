package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IJoint2 Declaration
public interface IJoint2 extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0xE8F6EA3A,(short)0x2E87,(short)0x11D5,new char[]{0xBC,0x81,0x00,0xD0,0xB7,0xE6,0x41,0x75});
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
  public void setJointValue(double pVal) throws com.inzoom.comjni.ComJniException;
}
