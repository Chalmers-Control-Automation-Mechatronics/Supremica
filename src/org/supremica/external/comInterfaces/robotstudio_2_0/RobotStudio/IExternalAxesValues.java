package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// interface IExternalAxesValues Declaration
public interface IExternalAxesValues extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x67124F4D,(short)0xF03B,(short)0x11D3,new char[]{0x80,0xEA,0x00,0xC0,0x4F,0x68,0x8A,0x8C});
  public float getEax_a() throws com.inzoom.comjni.ComJniException;
  public void setEax_a(float pVal) throws com.inzoom.comjni.ComJniException;
  public float getEax_b() throws com.inzoom.comjni.ComJniException;
  public void setEax_b(float pVal) throws com.inzoom.comjni.ComJniException;
  public float getEax_c() throws com.inzoom.comjni.ComJniException;
  public void setEax_c(float pVal) throws com.inzoom.comjni.ComJniException;
  public float getEax_d() throws com.inzoom.comjni.ComJniException;
  public void setEax_d(float pVal) throws com.inzoom.comjni.ComJniException;
  public float getEax_e() throws com.inzoom.comjni.ComJniException;
  public void setEax_e(float pVal) throws com.inzoom.comjni.ComJniException;
  public float getEax_f() throws com.inzoom.comjni.ComJniException;
  public void setEax_f(float pVal) throws com.inzoom.comjni.ComJniException;
  public int _QueryQuantity(int DispID,boolean[] IsValid) throws com.inzoom.comjni.ComJniException;
}
