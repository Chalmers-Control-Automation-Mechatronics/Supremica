package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _CodePane Implementation
public class _CodePaneJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane get_CodePaneFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CodePaneJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane get_CodePaneFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CodePaneJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane get_CodePaneFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _CodePaneJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane convertComPtrTo_CodePane(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CodePaneJCW(comPtr,true,releaseComPtr); }
  protected _CodePaneJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _CodePaneJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID); }
  protected _CodePaneJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _CodePaneJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID); }
  protected _CodePaneJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _CodePaneJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID,releaseComPtr);}
  protected _CodePaneJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePanes getCollection() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePanes rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePanes.getCodePanesFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.VBEJCW.getVBEFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window getWindow() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.Window rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.WindowJCW.getWindowFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void getSelection(int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(StartLine[0],true),
      new com.inzoom.comjni.Variant(StartColumn[0],true),
      new com.inzoom.comjni.Variant(EndLine[0],true),
      new com.inzoom.comjni.Variant(EndColumn[0],true)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    StartLine[0] = _v[0].getInt();
    StartColumn[0] = _v[1].getInt();
    EndLine[0] = _v[2].getInt();
    EndColumn[0] = _v[3].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void setSelection(int StartLine,int StartColumn,int EndLine,int EndColumn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(StartLine,false),
      new com.inzoom.comjni.Variant(StartColumn,false),
      new com.inzoom.comjni.Variant(EndLine,false),
      new com.inzoom.comjni.Variant(EndColumn,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getTopLine() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setTopLine(int TopLine) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(TopLine,false)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getCountOfVisibleLines() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodeModule getCodeModule() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodeModule rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodeModule.getCodeModuleFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void show() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getCodePaneView() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodePane.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
