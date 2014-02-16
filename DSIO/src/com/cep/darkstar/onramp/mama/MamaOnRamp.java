package com.cep.darkstar.onramp.mama;

import com.cep.darkstar.events.MamaNBBO;
import com.espertech.esper.client.EPServiceProvider;


public class MamaOnRamp extends Thread {
	private final EPServiceProvider cepEngine;
	/**
	 * @param args
	 */
	public MamaOnRamp(EPServiceProvider cepEngine) {
		this.cepEngine = cepEngine;
	}
	
	public static void main(String[] args) {
	}

	@Override
	public void run() {
		// just for testing
		// wrap wombat nbbo code here
		while(true) {
			cepEngine.getEPRuntime().sendEvent(new MamaNBBO("IBM",1,2,100,100,1234));
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
