/* Copyright (C) 2003-2016 Patrick G. Durand
 *
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import com.plealog.genericapp.api.log.EZLogger;

/**
 * A very basic HTTPclient.
 * 
 * @author Patrick G. Durand
 */
public class HTTPBasicEngine {
  public static String TMP_FILE_PREFIX = "http";
  public static String TMP_FILE_SUFIX = ".tmp";

  public static int CONNECT_TIMEOUT = 5000; // 5 seconds
  public static int SOCKET_TIMEOUT = 60000; // 1 minute
  
  private static void closeConnection(InputStream ins) {
    if (ins != null) {
      try {
        ins.close();
      } catch (IOException e) {
      }
    }
  }

  public static File doGet(String url) {
    return doGet(url, null);
  }
  /**
   * Do a HTTP GET using the provided url.
   * 
   * @param url
   *          the URL. HTTP and HTTPS are supported.
   * 
   * @param header_attrs
   *          attributes to set in header connection
   * @return a file containing the result. Returned file is set to deleteOnExit,
   *         so you do not have to worry about deleting it.
   * 
   * @throws HTTPEngineException
   *           if something wrong occurs.
   */
  // Tutorial:
  // http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
  public static File doGet(String url, Map<String, String> header_attrs) {
    InputStream ins = null;
    byte[] buffer = new byte[4096];
    int n = -1;

    // this is a very, very basic implementation to handle HTTP Get transactions
    // using URL APIs (e.g. NCBI eUtils, Ensembl, etc.). May need optimization
    // for more powerful needs...
    // Possible upgrade: use Jersey to deal with web services?

    // 1. prepare a temporary file to receive answer
    File answerFile = null;

    EZLogger.debug(url);
    try {
      answerFile = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFIX);
      answerFile.deleteOnExit();
      EZLogger.debug(answerFile.getAbsolutePath());
    } catch (IOException e) {
      EZLogger.warn(e.toString());
      throw new HTTPEngineException("Failed to create response file", url, HTTPEngineException.HTTPEX_TMP_FILE_ERROR);
    }

    // 2. run the HTTP GET method
    try (OutputStream output = new FileOutputStream(answerFile)) {
      // open connection to the remote server
      URL myurl = new URL(url);
      HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
      if (header_attrs!=null){
        Iterator<String> attrIter = header_attrs.keySet().iterator();
        String key;
        while(attrIter.hasNext()){
          key = attrIter.next();
          //con.setRequestProperty("Accept", "text/xml");
          con.setRequestProperty(key, header_attrs.get(key));
        }
      }
      con.setConnectTimeout(CONNECT_TIMEOUT);
      con.setReadTimeout(SOCKET_TIMEOUT);
      
      // ensembl and ebi provides additional header fields.
      // see
      // http://www.ebi.ac.uk/Tools/webservices/services/eb-eye_rest#additional_information_in_http_response_header
      // https://github.com/Ensembl/ensembl-rest/wiki/HTTP-Response-Codes
      // some of these header values can be used to adapt connection to remote
      // server. For now, we just monitor them... TODO: use them!
      EZLogger.debug(con.getHeaderFields().toString());

      // response code is checked before opening input stream
      if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new HTTPEngineException("Failed to connect to server", url, con.getResponseCode());
      }

      // 200 OK: read server answer
      ins = con.getInputStream();
      while ((n = ins.read(buffer)) != -1) {
        output.write(buffer, 0, n);
      }
      output.flush();
    } catch (HTTPEngineException hee) {
      throw hee;
    } catch (SocketTimeoutException ste) {
      throw new HTTPEngineException("Server does not answer (time out)", url, HTTPEngineException.HTTPEX_TIMEOUT);
    } catch (Exception e) {
      // we Log the HTTP or IO error since message is usually out of concern
      // for the end user. However, a log trace is always useful.
      EZLogger.warn(e.toString());
      // then raises a "generic" exception
      throw new HTTPEngineException("Unable to write in response file", url, HTTPEngineException.HTTPEX_WRITE_FILE_ERROR);
    }
    finally {
      // 3. close HTTP connection
      closeConnection(ins);
    }
    // 4. return answer
    return answerFile;
  }
  
  /**
   * Figures out whether or not a particular web server is available.
   */
  public static boolean isServerAvailable(String url) {
    try {
      URL myurl = new URL(url);
      HttpURLConnection httpConn = (HttpURLConnection)myurl.openConnection();
      httpConn.setInstanceFollowRedirects(false);
      httpConn.setRequestMethod("HEAD");
      httpConn.setConnectTimeout(CONNECT_TIMEOUT);
      httpConn.connect();
      EZLogger.debug(httpConn.getHeaderFields().toString());
    } catch (Exception e) {
      EZLogger.warn(e.toString());
      return false;
    }
    //if we hit this line, server is available
    return true;
  }
}
