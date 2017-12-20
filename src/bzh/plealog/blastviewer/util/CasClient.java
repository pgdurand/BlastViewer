/* 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/agpl-3.0.txt
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 */
package bzh.plealog.blastviewer.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import com.plealog.genericapp.api.log.EZLogger;

/**
 * This is a CAS authentication client. 
 * 
 * @author Patrick G. Durand
 */
public class CasClient {

  // Adapted from https://wiki.jasig.org/display/casum/restful+api (Groovy example)
  // Todo: http://debuguide.blogspot.fr/2013/01/quick-guide-for-migration-of-commons.html
  
  private static final String SERVICE_TOKEN = "service";
  private static final String TICKET_TOKEN  = "ticket";
  private static final String UNAME_TOKEN   = "username";
  private static final String PSWD_TOKEN    = "password";
  private static final String TMP_FILE_PREFIX = "service-";
  private static final String TMP_FILE_SUFFIX = ".dat";
  
  private static final String OK_CODE_MSG = "200 OK";
  
  private static final String MF0 = "Successful ticket granting request, but no ticket found";
  private static final MessageFormat MF1 = new MessageFormat("Invalid response code ( {0} ) from CAS server");
  private static final MessageFormat MF2 = new MessageFormat("Response (1k): {0}");
  private static final MessageFormat MF3 = new MessageFormat("Response: {0}");
  
  /**
   * Get a ticket granting ticket from CAS server. Step 1.
   * 
   * @param server CAS server URL
   * @param username user name
   * @param password password 
   * 
   * @return a valid ticket granting ticket or null if something wrong occurs.
   * */
  public String getTicketGrantingTicket(String server, String username, String password) {
    HttpClient    client = new HttpClient();
    PostMethod    post   = new PostMethod(server);
    NameValuePair nvp[]  = new NameValuePair[2];
    nvp[0] = new NameValuePair(UNAME_TOKEN, username);
    nvp[1] = new NameValuePair(PSWD_TOKEN, password);
    post.setRequestBody(nvp);
    try {
      client.executeMethod(post);
      String response = post.getResponseBodyAsString();
      switch (post.getStatusCode()) {
      case HttpStatus.SC_CREATED: //201
        Matcher matcher = Pattern.compile(".*action=\".*/(.*?)\".*").matcher(response);
        if (matcher.matches())
          return matcher.group(1);
        EZLogger.warn(MF0);
        EZLogger.debug(MF2.format(new Object[] { response.substring(0, Math.min(1024, response.length()))} ));
        break;
      default:
        EZLogger.warn(MF1.format(new Object[] { post.getStatusCode() }));
        EZLogger.debug(MF3.format(new Object[] { response}));
        break;
      }
    } catch (IOException e) {
      EZLogger.warn(e.getMessage());
    } finally {
      post.releaseConnection();
    }
    return null;
  }

  /**
   * Get a service ticket from CAS server. Step 2.
   * 
   * @param server CAS server URL
   * @param ticketGrantingTicket ticket granting ticket
   * @param service service URL 
   * 
   * @return a valid service ticket or null if something wrong occurs.
   * */
  public String getServiceTicket(String server, String ticketGrantingTicket, String service) {
    if (ticketGrantingTicket == null)
      return null;
    HttpClient    client = new HttpClient();
    PostMethod    post   = new PostMethod(server + "/" + ticketGrantingTicket);
    NameValuePair nvp[]  = new NameValuePair[1];
    nvp[0] = new NameValuePair(SERVICE_TOKEN, service);
    post.setRequestBody(nvp);
    try {
      client.executeMethod(post);
      String response = post.getResponseBodyAsString();
      switch (post.getStatusCode()) {
      case HttpStatus.SC_OK: //200
        return response;
      default:
        EZLogger.warn(MF1.format(new Object[] { post.getStatusCode() }));
        EZLogger.debug(MF2.format(new Object[] { response.substring(0, Math.min(1024, response.length()))} ));
        break;
      }
    } catch (IOException e) {
      EZLogger.warn(e.getMessage());
    } finally {
      post.releaseConnection();
    }
    return null;
  }

  /**
   * Invoke a service URL using appropriate service ticket. Step 3a.
   * Method to be used when expecting small answer from service call.
   * 
   * @param service service URL
   * @param serviceTicket a valid service ticket
   * 
   * @return the answer retrieved when calling service URL or null if 
   * something wrong occurs. 
   * */
  public String getServiceCallAsString(String service, String serviceTicket) {
    HttpClient    client = new HttpClient();
    GetMethod     method = new GetMethod(service);
    NameValuePair nvp[]  = new NameValuePair[1];
    client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
    method.setFollowRedirects(true);
    nvp[0] = new NameValuePair(TICKET_TOKEN, serviceTicket);
    method.setQueryString(nvp);
    try {
      client.executeMethod(method);
      String response = method.getResponseBodyAsString();
      switch (method.getStatusCode()) {
      case HttpStatus.SC_OK: //200
        EZLogger.debug(OK_CODE_MSG);
        return response;
      default:
        EZLogger.warn(MF1.format(new Object[] { method.getStatusCode() }));
        EZLogger.debug(MF3.format(new Object[] { response }));
        break;
      }
    } catch (IOException e) {
      EZLogger.warn(e.getMessage());
    } finally {
      method.releaseConnection();
    }
    return null;
  }

  /**
   * Invoke a service URL using appropriate service ticket. Step 3b.
   * Method to be used when expecting large answer from service call.
   * 
   * @param service service URL
   * @param serviceTicket a valid service ticket
   * 
   * @return the answer retrieved when calling service URL or null if 
   * something wrong occurs. 
   * */
  public File getServiceCallAsFile(String service, String serviceTicket) {
    HttpClient    client = new HttpClient();
    GetMethod     method = new GetMethod(service);
    NameValuePair nvp[]  = new NameValuePair[1];
    client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
    method.setFollowRedirects(true);
    nvp[0] = new NameValuePair(TICKET_TOKEN, serviceTicket);
    method.setQueryString(nvp);
    try {
      client.executeMethod(method);
      String response = method.getResponseBodyAsString();
      switch (method.getStatusCode()) {
      case HttpStatus.SC_OK: //200
        EZLogger.debug(OK_CODE_MSG);
        InputStream is = method.getResponseBodyAsStream();
        File output = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX);
        try(FileOutputStream fos = new FileOutputStream(output); 
            BufferedInputStream bais = new BufferedInputStream(is)){
          byte[] buf = new byte[4096];
          int n = bais.read(buf);
          while(n!=-1){
            fos.write(buf, 0, n);
            n = bais.read(buf);
          }
          fos.flush();
        }
        catch(IOException ioex) {
          EZLogger.warn(ioex.getMessage());
          if (output.exists()) {
            output.delete();
          }
          output = null;
        }
        return output;
      default:
        EZLogger.warn(MF1.format(new Object[] { method.getStatusCode() }));
        EZLogger.debug(MF3.format(new Object[] { response }));
        break;
      }
    } catch (IOException e) {
      EZLogger.warn(e.getMessage());
    } finally {
      method.releaseConnection();
    }
    return null;
  }
  /**
   * Close connection from CAS server. Step 4.
   * 
   * @param server CAS server URL
   * @param ticketGrantingTicket ticket granting ticket
   * */
  public void logout(String server, String ticketGrantingTicket) {
    HttpClient   client = new HttpClient();
    DeleteMethod method = new DeleteMethod(server + "/" + ticketGrantingTicket);
    try {
      client.executeMethod(method);
      switch (method.getStatusCode()) {
      case HttpStatus.SC_OK: //200
        break;
      default:
        EZLogger.warn(MF1.format(new Object[] { method.getStatusCode() }));
        EZLogger.debug(MF3.format(new Object[] { method.getResponseBodyAsString() }));
        break;
      }
    } catch (IOException e) {
      EZLogger.warn(e.getMessage());
    } finally {
      method.releaseConnection();
    }
  }
}
