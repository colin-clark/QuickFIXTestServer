package com.cep.darkstar.rulebot;

public interface CallBack {
	void methodToCallBack(String aString);
}

// facilitate inter-class communication
class CallBackImpl implements CallBack {
	@Override
	public void methodToCallBack(String aString) {
		System.out.println("DarkStar::"+aString);
	}
}