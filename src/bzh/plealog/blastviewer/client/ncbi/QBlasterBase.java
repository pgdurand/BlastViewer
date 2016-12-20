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
package bzh.plealog.blastviewer.client.ncbi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.log.EZLogger;

public class QBlasterBase {
  private String _errorMsg;

  /**
   * Analyze a file containing some data retrieved from the NCBI Blast server.
   * Actually this method looks for the section QBlastInfo and retrieves the
   * data available there. It gives the status of a query running on the server
   * side.
   * 
   * @param file
   *          the file to analyze
   * @return a key/value pairs table, i.e. query status. More on that is
   *         available from the NCBI Blast server documentation.
   */
  protected Map<String, String> analyseOutput(String qName, File file,
      boolean logInfo, boolean logError) {
    Hashtable<String, String> qBlastInfo;
    BufferedReader br = null;
    String line, key, value;
    boolean readInfo = false;
    int pos;

    qBlastInfo = new Hashtable<>();
    try {
      if (file.length() < 30) {
        qBlastInfo.put("LENGTH", "NOT_VALID");
      } else {
        br = new BufferedReader(new FileReader(file));
        while ((line = br.readLine()) != null) {
          if (line.indexOf("QBlastInfoBegin") != -1) {
            readInfo = true;
          }
          if (logInfo && readInfo) {
            EZLogger.info(line);
          }
          if (line.indexOf("QBlastInfoEnd") != -1) {
            break;
          }
          pos = line.indexOf("ERROR:");
          if (pos != -1) {
            value = line.substring(pos + 6).trim();
            pos = value.indexOf('<');
            if (pos != -1)
              qBlastInfo.put("ERROR", value.substring(0, pos));
            else
              qBlastInfo.put("ERROR", value);
          }
          pos = line.indexOf("INFO:");
          if (pos != -1) {
            value = line.substring(pos + 5).trim();
            pos = value.indexOf('<');
            if (pos != -1)
              qBlastInfo.put("INFO", value.substring(0, pos));
            else
              qBlastInfo.put("INFO", value);
          }
          if (readInfo) {
            pos = line.indexOf('=');
            if (pos != -1) {
              key = line.substring(0, pos).trim();
              value = line.substring(pos + 1).trim();
              qBlastInfo.put(key, value);
            }
          }
        }
      }
    } catch (Exception ex) {
      _errorMsg = BVMessages.getString("QBlaster.analyseFileError");
      if (logError) {
        EZLogger.warn(qName + ": " + _errorMsg + ": " + ex.toString());
      }
    } finally {
      IOUtils.closeQuietly(br);
    }
    return (qBlastInfo);
  }

  public String getErrorMsg() {
    return _errorMsg;
  }
}
