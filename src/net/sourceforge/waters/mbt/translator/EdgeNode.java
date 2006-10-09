package net.sourceforge.waters.mbt.translator;

public class EdgeNode {

	public String mBegin;

	public String mEnd;

	public String mGuard;

	public String mAction;

	// Constructor
	public EdgeNode(String begin, String end, String guard, String action) {
		this.mBegin = begin;
		this.mEnd = end;
		this.mGuard = guard;
		this.mAction = action;
	}

	public String getBegin() {
		return mBegin;

	}

	public String getEnd() {

		return mEnd;
	}

	public String getGuard() {

		return mGuard;
	}

	public String getAction() {

		return mAction;
	}
}
