package org.supremica.external.comInterfaces.robotstudio_2_0.RSEXT;

// interface IRSEInternalAdm Declaration
public interface IRSEInternalAdm extends com.inzoom.comjni.IUnknown {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x96AC7355,(short)0xEA11,(short)0x11D3,new char[]{0x80,0xD7,0x00,0xC0,0x4F,0x68,0xD8,0xB0});
  public void setID(int Id) throws com.inzoom.comjni.ComJniException;
  public int getID() throws com.inzoom.comjni.ComJniException;
  public void setParentID(int Id) throws com.inzoom.comjni.ComJniException;
  public int getParentID() throws com.inzoom.comjni.ComJniException;
  public void setMgr(com.inzoom.comjni.IUnknown pMgr) throws com.inzoom.comjni.ComJniException;
  public void setParent(com.inzoom.comjni.IDispatch pParent) throws com.inzoom.comjni.ComJniException;
  public void getParent(com.inzoom.comjni.IDispatch[] ppParent) throws com.inzoom.comjni.ComJniException;
  public void setChild(com.inzoom.comjni.IUnknown pChild) throws com.inzoom.comjni.ComJniException;
  public void removeChild(com.inzoom.comjni.IUnknown pChild) throws com.inzoom.comjni.ComJniException;
  public void notifyChildren() throws com.inzoom.comjni.ComJniException;
  public void notifyParent() throws com.inzoom.comjni.ComJniException;
}
