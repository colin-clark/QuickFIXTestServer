/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved. 
 * 
 * This file is part of the QuickFIX FIX Engine 
 * 
 * This file may be distributed under the terms of the quickfixengine.org 
 * license as defined by quickfixengine.org and appearing in the file 
 * LICENSE included in the packaging of this file. 
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING 
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 * 
 * See http://www.quickfixengine.org/LICENSE for licensing information. 
 * 
 * Contact ask@quickfixengine.org if any conditions of this licensing 
 * are not clear to you.
 ******************************************************************************/

package quickfix;

import java.sql.Connection;

public class JdbcLogLegacyTest extends JdbcLogTest {

    protected void initializeTableDefinitions(Connection connection) throws ConfigError {
        try {
            JdbcTestSupport.loadSQL(connection,
                    "core/src/main/config/sql/mysql/messages_log_table.sql",
                    new JdbcTestSupport.HypersonicLegacyPreprocessor(null));
            JdbcTestSupport.loadSQL(connection,
                    "core/src/main/config/sql/mysql/event_log_table.sql",
                    new JdbcTestSupport.HypersonicLegacyPreprocessor(null));
        } catch (Exception e) {
            throw new ConfigError(e);
        }
    }

}
