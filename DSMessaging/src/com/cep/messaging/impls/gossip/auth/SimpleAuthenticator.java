package com.cep.messaging.impls.gossip.auth;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Properties;

import com.cep.messaging.impls.gossip.thrift.AuthenticationException;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.FileUtils;
import com.cep.messaging.util.exception.ConfigurationException;

public class SimpleAuthenticator implements IAuthenticator
{
    public final static String PASSWD_FILENAME_PROPERTY        = "passwd.properties";
    public final static String PMODE_PROPERTY                  = "passwd.mode";
    public static final String USERNAME_KEY                    = "username";
    public static final String PASSWORD_KEY                    = "password";

    public enum PasswordMode
    {
        PLAIN, MD5,
    };

    public AuthenticatedUser defaultUser()
    {
        // users must log in
        return null;
    }

    public AuthenticatedUser authenticate(Map<? extends CharSequence,? extends CharSequence> credentials) throws AuthenticationException
    {
        String pmode_plain = System.getProperty(PMODE_PROPERTY);
        PasswordMode mode = PasswordMode.PLAIN;

        if (null != pmode_plain)
        {
            try
            {
                mode = PasswordMode.valueOf(pmode_plain);
            }
            catch (Exception e)
            {
                // this is not worth a StringBuffer
                String mode_values = "";
                for (PasswordMode pm : PasswordMode.values())
                    mode_values += "'" + pm + "', ";

                mode_values += "or leave it unspecified.";
                throw new AuthenticationException("The requested password check mode '" + pmode_plain + "' is not a valid mode.  Possible values are " + mode_values);
            }
        }

        String pfilename = System.getProperty(PASSWD_FILENAME_PROPERTY);

        String username = null;
        CharSequence user = credentials.get(USERNAME_KEY);
        if (null == user) 
            throw new AuthenticationException("Authentication request was missing the required key '" + USERNAME_KEY + "'");
        else
            username = user.toString();

        String password = null;
        CharSequence pass = credentials.get(PASSWORD_KEY);
        if (null == pass) 
            throw new AuthenticationException("Authentication request was missing the required key '" + PASSWORD_KEY + "'");
        else
            password = pass.toString();

        boolean authenticated = false;

        InputStream in = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream(pfilename));
            Properties props = new Properties();
            props.load(in);

            // note we keep the message here and for the wrong password exactly the same to prevent attackers from guessing what users are valid
            if (null == props.getProperty(username)) throw new AuthenticationException(authenticationErrorMessage(mode, username));
            switch (mode)
            {
                case PLAIN:
                    authenticated = password.equals(props.getProperty(username));
                    break;
                case MD5:
                    authenticated = MessageDigest.isEqual(GossipUtilities.threadLocalMD5Digest().digest(password.getBytes()), GossipUtilities.hexToBytes(props.getProperty(username)));
                    break;
                default:
                    throw new RuntimeException("Unknown PasswordMode " + mode);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Authentication table file given by property " + PASSWD_FILENAME_PROPERTY + " could not be opened: " + e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unexpected authentication problem", e);
        }
        finally
        {
            FileUtils.closeQuietly(in);
        }

        if (!authenticated) throw new AuthenticationException(authenticationErrorMessage(mode, username));

        return new AuthenticatedUser(username);
    }

    public void validateConfiguration() throws ConfigurationException
    {
        String pfilename = System.getProperty(SimpleAuthenticator.PASSWD_FILENAME_PROPERTY);
        if (pfilename == null)
        {
            throw new ConfigurationException("When using " + this.getClass().getCanonicalName() + " " + 
                    SimpleAuthenticator.PASSWD_FILENAME_PROPERTY + " properties must be defined.");	
        }
    }

    static String authenticationErrorMessage(PasswordMode mode, String username)
    {
        return String.format("Given password in password mode %s could not be validated for user %s", mode, username);
    }
}
