package org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw;

// interface _CommandBarButton Implementation
public class _CommandBarButtonJCW extends org.supremica.external.comInterfaces.robotstudio_2_0.Office.jcw.CommandBarControlJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton get_CommandBarButtonFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarButtonJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton get_CommandBarButtonFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarButtonJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton get_CommandBarButtonFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _CommandBarButtonJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton convertComPtrTo_CommandBarButton(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CommandBarButtonJCW(comPtr,true,releaseComPtr); }
  protected _CommandBarButtonJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _CommandBarButtonJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID); }
  protected _CommandBarButtonJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _CommandBarButtonJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID); }
  protected _CommandBarButtonJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _CommandBarButtonJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID,releaseComPtr);}
  protected _CommandBarButtonJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public boolean getBuiltInFace() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(332,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    boolean rv = _v[0].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setBuiltInFace(boolean pvarfBuiltIn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pvarfBuiltIn,false)
    };
    vtblCall(336,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void copyFace() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(340,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getFaceId() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,com.inzoom.comjni.enum.VarType.INT,true)
    };
    vtblCall(344,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setFaceId(int pid) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pid,com.inzoom.comjni.enum.VarType.INT,false)
    };
    vtblCall(348,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void pasteFace() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(352,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getShortcutText() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(356,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setShortcutText(String pbstrText) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrText,false)
    };
    vtblCall(360,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getState() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(364,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setState(int pstate) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pstate,false)
    };
    vtblCall(368,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getStyle() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(372,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setStyle(int pstyle) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pstyle,false)
    };
    vtblCall(376,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getHyperlinkType() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(380,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setHyperlinkType(int phlType) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(phlType,false)
    };
    vtblCall(384,_v,org.supremica.external.comInterfaces.robotstudio_2_0.Office._CommandBarButton.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
}
