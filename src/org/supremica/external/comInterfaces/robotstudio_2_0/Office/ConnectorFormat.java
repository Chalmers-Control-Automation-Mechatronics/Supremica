package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface ConnectorFormat Declaration
public interface ConnectorFormat extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0313,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public void beginConnect(org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape ConnectedShape,int ConnectionSite) throws com.inzoom.comjni.ComJniException;
  public void beginDisconnect() throws com.inzoom.comjni.ComJniException;
  public void endConnect(org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape ConnectedShape,int ConnectionSite) throws com.inzoom.comjni.ComJniException;
  public void endDisconnect() throws com.inzoom.comjni.ComJniException;
  public int getBeginConnected() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getBeginConnectedShape() throws com.inzoom.comjni.ComJniException;
  public int getBeginConnectionSite() throws com.inzoom.comjni.ComJniException;
  public int getEndConnected() throws com.inzoom.comjni.ComJniException;
  public org.supremica.external.comInterfaces.robotstudio_2_0.Office.Shape getEndConnectedShape() throws com.inzoom.comjni.ComJniException;
  public int getEndConnectionSite() throws com.inzoom.comjni.ComJniException;
  public int getType() throws com.inzoom.comjni.ComJniException;
  public void setType(int Type) throws com.inzoom.comjni.ComJniException;
}
