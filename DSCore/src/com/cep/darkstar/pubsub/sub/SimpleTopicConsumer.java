//   The contents of this file are subject to the Mozilla Public License
//   Version 1.1 (the "License"); you may not use this file except in
//   compliance with the License. You may obtain a copy of the License at
//   http://www.mozilla.org/MPL/
//
//   Software distributed under the License is distributed on an "AS IS"
//   basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//   License for the specific language governing rights and limitations
//   under the License.
//
//   The Original Code is RabbitMQ.
//
//   The Initial Developers of the Original Code are LShift Ltd,
//   Cohesive Financial Technologies LLC, and Rabbit Technologies Ltd.
//
//   Portions created before 22-Nov-2008 00:00:00 GMT by LShift Ltd,
//   Cohesive Financial Technologies LLC, or Rabbit Technologies Ltd
//   are Copyright (C) 2007-2008 LShift Ltd, Cohesive Financial
//   Technologies LLC, and Rabbit Technologies Ltd.
//
//   Portions created by LShift Ltd are Copyright (C) 2007-2010 LShift
//   Ltd. Portions created by Cohesive Financial Technologies LLC are
//   Copyright (C) 2007-2010 Cohesive Financial Technologies
//   LLC. Portions created by Rabbit Technologies Ltd are Copyright
//   (C) 2007-2010 Rabbit Technologies Ltd.
//
//   All Rights Reserved.
//
//   Contributor(s): ______________________________________.
//

package com.cep.darkstar.pubsub.sub;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class SimpleTopicConsumer {
	static Logger logger = Logger.getLogger("com.cep.darkstar.offramp.SimpleTopicConsumer");
    public static void main(String[] args) {
        try {
            if (args.length < 1 || args.length > 5) {
                System.err.print("Usage: SimpleTopicConsumer brokerhostname [brokerport\n" +
                                 "                                           [topicpattern\n" +
                                 "                                            [exchange\n" +
                                 "                                             [queue]]]]\n" +
                                 "where\n" +
                                 " - topicpattern defaults to \"#\",\n" +
                                 " - exchange to \"amq.topic\", and\n" +
                                 " - queue to a private, autodelete queue\n");
                System.exit(1);
            }
    		// initialize log4j
    		PropertyConfigurator.configure("log4j.properties");
    		
        	long end = 0;
        	long begin = 0;
        	int i = 0;
            String hostName = (args.length > 0) ? args[0] : "localhost";
            int portNumber = (args.length > 1) ? Integer.parseInt(args[1]) : 5672;
            String topicPattern = (args.length > 2) ? args[2] : "#";

            SubscribeTopic listen = new SubscribeTopic.Builder().hostName(hostName).portNumber(portNumber).topic(topicPattern).build();
			begin = System.currentTimeMillis();
            while (true) {
            	i =  i + 1;
            	System.out.println(new String(listen.nextDelivery()));
                if ((i % 50000) == 0) {
    				end = System.currentTimeMillis();
    				logger.info("50,000 messages sent in "+Long.toString((end-begin)/1000)+" seconds");
    				begin = end;
    			}
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            System.exit(1);
        }
    }
}
