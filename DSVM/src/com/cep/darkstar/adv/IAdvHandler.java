package com.cep.darkstar.adv;

import com.cep.darkstar.node.CEPEngine;

public interface IAdvHandler extends Runnable {
	
	public void setParams(String url, String user, String pwd);
	public void generateEpl(CEPEngine cepEngine);

}
