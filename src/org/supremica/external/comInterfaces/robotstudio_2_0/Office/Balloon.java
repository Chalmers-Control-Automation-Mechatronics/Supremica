package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface Balloon Declaration
public interface Balloon extends org.supremica.external.comInterfaces.robotstudio_2_0.Office._IMsoDispObj {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x000C0324,(short)0x0000,(short)0x0000,new char[]{0xC0,0x00,0x00,0x00,0x00,0x00,0x00,0x46});
  public com.inzoom.comjni.IDispatch getParent() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getCheckboxes() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getLabels() throws com.inzoom.comjni.ComJniException;
  public void setBalloonType(int pbty) throws com.inzoom.comjni.ComJniException;
  public int getBalloonType() throws com.inzoom.comjni.ComJniException;
  public void setIcon(int picn) throws com.inzoom.comjni.ComJniException;
  public int getIcon() throws com.inzoom.comjni.ComJniException;
  public void setHeading(String pbstr) throws com.inzoom.comjni.ComJniException;
  public String getHeading() throws com.inzoom.comjni.ComJniException;
  public void setText(String pbstr) throws com.inzoom.comjni.ComJniException;
  public String getText() throws com.inzoom.comjni.ComJniException;
  public void setMode(int pmd) throws com.inzoom.comjni.ComJniException;
  public int getMode() throws com.inzoom.comjni.ComJniException;
  public void setAnimation(int pfca) throws com.inzoom.comjni.ComJniException;
  public int getAnimation() throws com.inzoom.comjni.ComJniException;
  public void setButton(int psbs) throws com.inzoom.comjni.ComJniException;
  public int getButton() throws com.inzoom.comjni.ComJniException;
  public void setCallback(String pbstr) throws com.inzoom.comjni.ComJniException;
  public String getCallback() throws com.inzoom.comjni.ComJniException;
  public void setPrivate(int plPrivate) throws com.inzoom.comjni.ComJniException;
  public int getPrivate() throws com.inzoom.comjni.ComJniException;
  public void setAvoidRectangle(int Left,int Top,int Right,int Bottom) throws com.inzoom.comjni.ComJniException;
  public String getName() throws com.inzoom.comjni.ComJniException;
  public int show() throws com.inzoom.comjni.ComJniException;
  public void close() throws com.inzoom.comjni.ComJniException;
}
