package org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw;

// interface _CodeModule Implementation
public class _CodeModuleJCW extends com.inzoom.comjni.jcw.IDispatchJCW implements org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule, com.inzoom.comjni.IDispatch {
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule get_CodeModuleFromComPtr(int comPtr,boolean bAddRef) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CodeModuleJCW(comPtr,bAddRef); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule get_CodeModuleFromComPtr(int comPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CodeModuleJCW(comPtr); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule get_CodeModuleFromUnknown(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException { return unk == null ? null : new _CodeModuleJCW(unk); }
  public static org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule convertComPtrTo_CodeModule(int comPtr,boolean releaseComPtr) throws com.inzoom.comjni.ComJniException { return comPtr == 0 ? null : new _CodeModuleJCW(comPtr,true,releaseComPtr); }
  protected _CodeModuleJCW(int comPtr,boolean bAddRef)throws com.inzoom.comjni.ComJniException { super(comPtr,bAddRef); }
  protected _CodeModuleJCW(int comPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID); }
  protected _CodeModuleJCW(int comPtr,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(comPtr,guid); }
  protected _CodeModuleJCW(com.inzoom.comjni.IUnknown unk) throws com.inzoom.comjni.ComJniException{ super(unk,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID); }
  protected _CodeModuleJCW(com.inzoom.comjni.IUnknown unk,com.inzoom.util.Guid guid) throws com.inzoom.comjni.ComJniException { super(unk,guid); }
  protected _CodeModuleJCW(int comPtr,boolean useQI,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID,releaseComPtr);}
  protected _CodeModuleJCW(int comPtr,com.inzoom.util.Guid iid,boolean releaseComPtr)throws com.inzoom.comjni.ComJniException { super(comPtr,iid,releaseComPtr); }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent getParent() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(28,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBComponent.getVBComponentFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE getVBE() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(32,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.VBE rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.jcw.VBEJCW.getVBEFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getName() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(36,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    String rv = _v[0].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void setName(String pbstrName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(pbstrName,false)
    };
    vtblCall(40,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addFromString(String String) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(String,false)
    };
    vtblCall(44,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void addFromFile(String FileName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(FileName,false)
    };
    vtblCall(48,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public String getLines(int StartLine,int Count) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(StartLine,false),
      new com.inzoom.comjni.Variant(Count,false),
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(52,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    String rv = _v[2].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCountOfLines() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(56,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public void insertLines(int Line,String String) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Line,false),
      new com.inzoom.comjni.Variant(String,false)
    };
    vtblCall(60,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void deleteLines(int StartLine,int Count) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(StartLine,false),
      new com.inzoom.comjni.Variant(Count,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void deleteLines(int StartLine) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(StartLine,false),
      new com.inzoom.comjni.Variant((int)1,com.inzoom.comjni.enum.VarType.I4,false)
    };
    vtblCall(64,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public void replaceLine(int Line,String String) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Line,false),
      new com.inzoom.comjni.Variant(String,false)
    };
    vtblCall(68,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    for(int i = 0; i < _v.length; i++) _v[i].release();
  }
  public int getProcStartLine(String ProcName,int ProcKind) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ProcName,false),
      new com.inzoom.comjni.Variant(ProcKind,false),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(72,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    int rv = _v[2].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getProcCountLines(String ProcName,int ProcKind) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ProcName,false),
      new com.inzoom.comjni.Variant(ProcKind,false),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(76,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    int rv = _v[2].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getProcBodyLine(String ProcName,int ProcKind) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(ProcName,false),
      new com.inzoom.comjni.Variant(ProcKind,false),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(80,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    int rv = _v[2].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public String getProcOfLine(int Line,int[] ProcKind) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Line,false),
      new com.inzoom.comjni.Variant(ProcKind[0],true),
      new com.inzoom.comjni.Variant((String)null,true)
    };
    vtblCall(84,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    ProcKind[0] = _v[1].getInt();
    String rv = _v[2].getString();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int getCountOfDeclarationLines() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(88,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    int rv = _v[0].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public int createEventProc(String EventName,String ObjectName) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(EventName,false),
      new com.inzoom.comjni.Variant(ObjectName,false),
      new com.inzoom.comjni.Variant((int)0,true)
    };
    vtblCall(92,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    int rv = _v[2].getInt();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn,boolean WholeWord,boolean MatchCase,boolean PatternSearch) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Target,false),
      new com.inzoom.comjni.Variant(StartLine[0],true),
      new com.inzoom.comjni.Variant(StartColumn[0],true),
      new com.inzoom.comjni.Variant(EndLine[0],true),
      new com.inzoom.comjni.Variant(EndColumn[0],true),
      new com.inzoom.comjni.Variant(WholeWord,false),
      new com.inzoom.comjni.Variant(MatchCase,false),
      new com.inzoom.comjni.Variant(PatternSearch,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    StartLine[0] = _v[1].getInt();
    StartColumn[0] = _v[2].getInt();
    EndLine[0] = _v[3].getInt();
    EndColumn[0] = _v[4].getInt();
    boolean rv = _v[8].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn,boolean WholeWord,boolean MatchCase) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Target,false),
      new com.inzoom.comjni.Variant(StartLine[0],true),
      new com.inzoom.comjni.Variant(StartColumn[0],true),
      new com.inzoom.comjni.Variant(EndLine[0],true),
      new com.inzoom.comjni.Variant(EndColumn[0],true),
      new com.inzoom.comjni.Variant(WholeWord,false),
      new com.inzoom.comjni.Variant(MatchCase,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    StartLine[0] = _v[1].getInt();
    StartColumn[0] = _v[2].getInt();
    EndLine[0] = _v[3].getInt();
    EndColumn[0] = _v[4].getInt();
    boolean rv = _v[8].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn,boolean WholeWord) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Target,false),
      new com.inzoom.comjni.Variant(StartLine[0],true),
      new com.inzoom.comjni.Variant(StartColumn[0],true),
      new com.inzoom.comjni.Variant(EndLine[0],true),
      new com.inzoom.comjni.Variant(EndColumn[0],true),
      new com.inzoom.comjni.Variant(WholeWord,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    StartLine[0] = _v[1].getInt();
    StartColumn[0] = _v[2].getInt();
    EndLine[0] = _v[3].getInt();
    EndColumn[0] = _v[4].getInt();
    boolean rv = _v[8].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public boolean find(String Target,int[] StartLine,int[] StartColumn,int[] EndLine,int[] EndColumn) throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant(Target,false),
      new com.inzoom.comjni.Variant(StartLine[0],true),
      new com.inzoom.comjni.Variant(StartColumn[0],true),
      new com.inzoom.comjni.Variant(EndLine[0],true),
      new com.inzoom.comjni.Variant(EndColumn[0],true),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,false),
      new com.inzoom.comjni.Variant((boolean)false,true)
    };
    vtblCall(96,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    StartLine[0] = _v[1].getInt();
    StartColumn[0] = _v[2].getInt();
    EndLine[0] = _v[3].getInt();
    EndColumn[0] = _v[4].getInt();
    boolean rv = _v[8].getBoolean();
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
  public org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane getCodePane() throws com.inzoom.comjni.ComJniException {
    com.inzoom.comjni.Variant[] _v = new com.inzoom.comjni.Variant[]{
      new com.inzoom.comjni.Variant((com.inzoom.comjni.IDispatch)null,true)
    };
    vtblCall(100,_v,org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE._CodeModule.IID);
    org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane rv = org.supremica.external.comInterfaces.robotstudio_2_0.VBIDE.CodePane.getCodePaneFromComPtr(_v[0].detachUnknownPtr(),false);
    for(int i = 0; i < _v.length; i++) _v[i].release();
    return rv;
  }
}
