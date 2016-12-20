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

import java.io.File;
import java.text.MessageFormat;
import java.util.Map;

import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.HTTPBasicEngine;
import bzh.plealog.blastviewer.util.HTTPEngineException;

/**
 * This class implements a basic system to fetch a Blast results from the NCBI
 * given a Request Identifier.
 * 
 * @author Patrick G. Durand
 */
public class QBlastRetriever {
  private String                    _errorMsg;
  private File                      _resFile;

  public static final MessageFormat QBLAST_URL = new MessageFormat(
      "https://blast.ncbi.nlm.nih.gov/Blast.cgi?RESULTS_FILE=on&RID={0}&FORMAT_TYPE=XML&FORMAT_OBJECT=Alignment&CMD=Get"
      );
  // Note: old address was:
  // "https://www.ncbi.nlm.nih.gov/blast/Blast.cgi?RID={0}&FORMAT_TYPE=XML&CMD=Get&ALIGNMENT_TYPE=Pairwise&FORMAT_OBJECT=Alignment"
  // To get XML 2, use: FORMAT_TYPE=XML2_S
  private static final String       QUERY_NAME = "QBlastRetriever";

  /**
   * Call this method after a call to getBlastResult to get back some error
   * message if any.
   * 
   * @return an error message or null if no error was reported.
   */
  public String getErrorMsg() {
    return _errorMsg;
  }

  /**
   * Call this method after a call to getBlastResult to get back the result
   * file.
   * 
   * @return a result file or null if an error was reported.
   */
  public File getResultFile() {
    return _resFile;
  }

  public boolean getBlastResult(String rid) {
    QBlasterBase qb;
    Map<String, String> qBlastInfo;
    String status;
    String url;
    File tmpFile;

    url = QBLAST_URL.format(new Object[] { rid.trim() });
    try {
      tmpFile = HTTPBasicEngine.doGet(url);
    } catch (HTTPEngineException ex) {
      _errorMsg = ex.getMessage();
      return false;
    }
    qb = new QBlasterBase();
    qBlastInfo = qb.analyseOutput(QUERY_NAME, tmpFile, false, false);
    if (qBlastInfo.size() == 0) {
      // ok
      _resFile = tmpFile;
      return true;
    } else {
      // status different than WAITING: Blast error!
      status = (String) qBlastInfo.get("Status");
      if (status != null && !status.equals("WAITING")) {
        // we return an ERROR param, while NCBI not
        _errorMsg = (String) qBlastInfo.get("ERROR");
        if (_errorMsg == null)
          _errorMsg = status;
      } else {
        _errorMsg = BVMessages.getString("QBlastRetriever.err");
      }
      tmpFile.delete();
      return false;
    }
  }

}
