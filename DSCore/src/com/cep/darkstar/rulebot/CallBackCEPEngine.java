package com.cep.darkstar.rulebot;

public interface CallBackCEPEngine {

	
	public interface _CallBackCEPEngine {
		void start();
		void stop();
		void submitQuery(String aString);
	}

	// facilitate inter-class communication
	class CEPEngine implements _CallBackCEPEngine {
		@Override
		public void stop() {
		}

		@Override
		public void start() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void submitQuery(String aString) {
			// TODO Auto-generated method stub
			
		}
	}
}
