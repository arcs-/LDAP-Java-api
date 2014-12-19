package biz.stillhart.profileManagement.utils;

import javax.naming.*;
import javax.naming.directory.*;
import java.util.ArrayList;
import java.util.Properties;

/**
The MIT License (MIT)

Copyright (c) 2014 Patrick Stillhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
public class LDAPConnection {

    private static final String PREFIX = "LDAP -> ";

    private String server;
    private int port;
    private String userDN;
    private String loginDN;
    private String password;

    private boolean connected;
    private InitialDirContext connection;

    /**
     * Creates a new connection on default port (389)
     *
     * @param server   the servers ip / domain
     * @param userDN   the base DN (e.g. 'cn=schueler,cn=users,ou=school,dc=openiam,dc=com')
     * @param loginDN  admin logon (e.g. 'cn=Manager,dc=openiam,dc=com')
     * @param password admin password
     *
     * @throws NamingException Can't connect to DB
     */
    public LDAPConnection(String server, String userDN, String loginDN, String password) throws NamingException{
        reconnect(server, 389, userDN, loginDN, password);
    }

    /**
     * Creates a new connection on custom port
     *
     * @param server   the servers ip / domain
     * @param port     a custom port
     * @param userDN   the base DN (e.g. 'cn=schueler,cn=users,ou=school,dc=openiam,dc=com')
     * @param loginDN  admin logon (e.g. 'cn=Manager,dc=openiam,dc=com')
     * @param password admin password
     *
     * @throws NamingException Can't connect to DB
     */
    public LDAPConnection(String server, int port, String userDN, String loginDN, String password) throws NamingException{
        reconnect(server, port, userDN, loginDN, password);
    }

    /**
     * Closes current connection and creates a new one on default port(389)
     *
     * @param server   the servers ip / domain
     * @param userDN   the base DN (e.g. 'cn=schueler,cn=users,ou=school,dc=openiam,dc=com')
     * @param loginDN  admin logon (e.g. 'cn=Manager,dc=openiam,dc=com')
     * @param password admin password
     *
     * @throws NamingException Can't connect to DB
     */
    public void reconnect(String server, String userDN, String loginDN, String password) throws NamingException{
        reconnect(server, 389, userDN, loginDN, password);
    }

    /**
     * Closes currect connection and creates a new one
     *
     * @param server   the servers ip / domain
     * @param port     a custom port
     * @param userDN   the base DN (e.g. 'cn=schueler,cn=users,ou=school,dc=openiam,dc=com')
     * @param loginDN  admin logon (e.g. 'cn=Manager,dc=openiam,dc=com')
     * @param password admin password
     *
     * @throws NamingException Can't connect to DB
     */
    public void reconnect(String server, int port, String userDN, String loginDN, String password) throws NamingException{
        if (connected) close();

        this.server = server;
        this.port = port;
        this.userDN = userDN;
        this.loginDN = loginDN;
        this.password = password;

        connect();

    }

    private void connect() throws NamingException{
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, "ldap://" + server + ":" + port + "/");
        props.put(Context.SECURITY_CREDENTIALS, password);
        props.put(Context.SECURITY_PRINCIPAL, loginDN);

        connection = new InitialDirContext(props);

        System.out.println(PREFIX + "Authentication Success!");
        connected = true;

    }

    /**
     * Gets an user by a uid
     *
     * @param uid the uid
     * @return a SearchResult -object containing all attributes
     */
    public SearchResult getUser(String uid) {
        String searchFilter = "(&(objectClass=person)(uid=" + uid + "))";

        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        try {
            NamingEnumeration<SearchResult> results = connection.search(userDN, searchFilter, searchControls);

            if (results.hasMoreElements()) {
                SearchResult searchResult = results.nextElement();

                //make sure there is not another item available, there should be only 1 match
                if (results.hasMoreElements()) {
                    System.err.println(PREFIX + "Matched multiple users for the accountName: " + uid);
                    return null;
                }

                return searchResult;
            }

        } catch (NamingException e) {
            System.err.println(PREFIX + "Couldn't find user with uid (" + uid + ") error: " + e.getMessage());
        }

        return null;

    }

    /**
     * Accepts custom filters and returns there result
     * http://www.google.com/support/enterprise/static/gapps/docs/admin/en/gads/admin/ldap.5.4.html
     *
     * @param searchFilter a ldap search filter (e.g. '(objectClass=posixaccount)')
     * @return ArrayList<SearchResult> with all results / null for nothing
     */
    public ArrayList<SearchResult> getResultByCustomFilter(String searchFilter) throws NamingException {
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<SearchResult> results = connection.search(userDN, searchFilter, searchControls);

        if (results.hasMoreElements()) {
            ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
            while (results.hasMore()) {
                searchResults.add(results.next());
            }
            return searchResults;

        }
        return null;

    }

    /**
     * creates a new user
     *
     * @param uid   the uid (same as in the list)
     * @param entry a list with attributes
     * @return true if successful
     */
    public boolean addUser(String uid, Attributes entry) {
        String dn = getDN(uid);
        try {
            connection.createSubcontext(dn, entry);
            System.out.println(PREFIX + "AddUser: added entry " + dn + ".");
            return true;

        } catch (NameAlreadyBoundException e) {
            System.err.println(PREFIX + "AddUser: Entry Already Exists (68)");
            return false;
        } catch (NamingException e) {
            System.err.println(PREFIX + "AddUser: error adding entry." + e.getMessage());
            return false;
        }


    }

    /**
     * Update user attributes
     *
     * @param uid  the uid from user to modify
     * @param mods a list with modifications
     * @return true if successful
     */
    public boolean updateUser(String uid, ModificationItem[] mods) {
        try {
            connection.modifyAttributes(getDN(uid), mods);
            return true;
        } catch (NamingException e) {
            System.err.println(PREFIX + "Update error: " + e.getMessage());
            return false;
        }

    }

    /**
     * Return true if connected
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Deletes an user
     *
     * @param uid the uid
     * @return true if successful
     */
    public boolean deleteUser(String uid) {
        try {
            connection.destroySubcontext(uid);
            return true;
        } catch (NamingException e) {
            System.err.println(PREFIX + "Deleting error: " + e.getMessage());
            return false;
        }

    }

    /**
     * Closes a current LDAP connection
     */
    public void close() {
        connected = false;
        try {
            connection.close();
            System.out.println(PREFIX + "Connection closed!");
        } catch (NamingException e) {
            System.err.println(PREFIX + "Close: failed to close connection: " + e.getMessage());
        }

    }

    private String getDN(String uid) {
        return "uid=" + uid + "," + userDN;
    }
}