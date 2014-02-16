package com.cep.darkstar.onramp.appia.test.stub;

import com.javtech.appia.ef.Plugin;
import com.javtech.appia.ef.PluginPoint;
import com.javtech.appia.ef.session.InboundPostValidationPlugin;
import com.javtech.appia.ef.session.SessionPluginFilter;
import com.javtech.javatoolkit.message.MessageObject;

public class InboundPostValidationPluginImpl implements InboundPostValidationPlugin {

	@Override
	public SessionPluginFilter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInterested(PluginPoint arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Plugin newInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processInboundMessagePostValidation(MessageObject arg0,
			CallContext arg1) {
		// TODO Auto-generated method stub
		
	}

}
