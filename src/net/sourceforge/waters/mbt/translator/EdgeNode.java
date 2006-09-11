package net.sourceforge.waters.mbt.translator;

public class EdgeNode {

	public String mBegin;

	public String mEnd;

	// Constructor
	public EdgeNode(String begin, String end) {
		this.mBegin = begin;
		this.mEnd = end;
	}
	
	public String getBegin(){
		return mBegin;
		
	}
	
	public String getEnd(){
		
		return mEnd;
	}
}
