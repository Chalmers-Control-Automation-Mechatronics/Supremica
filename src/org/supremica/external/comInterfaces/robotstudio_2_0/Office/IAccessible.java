package org.supremica.external.comInterfaces.robotstudio_2_0.Office;

// interface IAccessible Declaration
public interface IAccessible extends com.inzoom.comjni.IDispatch {
  public static com.inzoom.util.Guid IID = new com.inzoom.util.Guid(0x618736E0,(short)0x3C3D,(short)0x11CF,new char[]{0x81,0x0C,0x00,0xAA,0x00,0x38,0x9B,0x71});
  public com.inzoom.comjni.IDispatch getAccParent() throws com.inzoom.comjni.ComJniException;
  public int getAccChildCount() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.IDispatch getAccChild(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public String getAccName(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public String getAccName() throws com.inzoom.comjni.ComJniException;
  public String getAccValue(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public String getAccValue() throws com.inzoom.comjni.ComJniException;
  public String getAccDescription(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public String getAccDescription() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getAccRole(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getAccRole() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getAccState(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getAccState() throws com.inzoom.comjni.ComJniException;
  public String getAccHelp(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public String getAccHelp() throws com.inzoom.comjni.ComJniException;
  public int getAccHelpTopic(String[] pszHelpFile,com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public int getAccHelpTopic(String[] pszHelpFile) throws com.inzoom.comjni.ComJniException;
  public String getAccKeyboardShortcut(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public String getAccKeyboardShortcut() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getAccFocus() throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant getAccSelection() throws com.inzoom.comjni.ComJniException;
  public String getAccDefaultAction(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public String getAccDefaultAction() throws com.inzoom.comjni.ComJniException;
  public void accSelect(int flagsSelect,com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public void accSelect(int flagsSelect) throws com.inzoom.comjni.ComJniException;
  public void accLocation(int[] pxLeft,int[] pyTop,int[] pcxWidth,int[] pcyHeight,com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public void accLocation(int[] pxLeft,int[] pyTop,int[] pcxWidth,int[] pcyHeight) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant accNavigate(int navDir,com.inzoom.comjni.Variant varStart) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant accNavigate(int navDir) throws com.inzoom.comjni.ComJniException;
  public com.inzoom.comjni.Variant accHitTest(int xLeft,int yTop) throws com.inzoom.comjni.ComJniException;
  public void accDoDefaultAction(com.inzoom.comjni.Variant varChild) throws com.inzoom.comjni.ComJniException;
  public void accDoDefaultAction() throws com.inzoom.comjni.ComJniException;
  public void setAccName(com.inzoom.comjni.Variant varChild,String pszName) throws com.inzoom.comjni.ComJniException;
  public void setAccValue(com.inzoom.comjni.Variant varChild,String pszValue) throws com.inzoom.comjni.ComJniException;
}
