package org.supremica.external.comInterfaces.robotstudio_2_0.RobotStudio;

// Dispinterface _IViewEvents Declaration
public interface _IViewEvents  {
  public static com.inzoom.util.Guid DIID = new com.inzoom.util.Guid(0xA88F09F9,(short)0xE2C5,(short)0x11D3,new char[]{0x80,0xC3,0x00,0xC0,0x4F,0x60,0xF7,0x93});
  public void click() ;
  public void dblClick() ;
  public void keyDown(int Char,int RepCnt,int Flags) ;
  public void keyUp(int Char,int RepCnt,int Flags) ;
  public void lButtonDblClk(int Flags,int x,int y) ;
  public void lButtonDown(int Flags,int x,int y) ;
  public void lButtonUp(int Flags,int x,int y) ;
  public void mButtonDblClk(int Flags,int x,int y) ;
  public void mButtonDown(int Flags,int x,int y) ;
  public void mButtonUp(int Flags,int x,int y) ;
  public void rButtonDblClk(int Flags,int x,int y) ;
  public void rButtonDown(int Flags,int x,int y) ;
  public void rButtonUp(int Flags,int x,int y) ;
  public void mouseMove(int nFlags,int x,int y) ;
  public void gotFocus() ;
  public void lostFocus() ;
  public int unload(boolean[] Cancel) ;
}
