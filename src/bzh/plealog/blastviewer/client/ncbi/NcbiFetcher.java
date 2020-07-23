/* Copyright (C) 2003-2020 Patrick G. Durand
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
import java.io.IOException;
import java.text.MessageFormat;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.io.SRLoader;
import bzh.plealog.bioinfo.io.searchresult.SerializerSystemFactory;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.blastviewer.config.directory.DirManager;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * Utility class.
 * 
 * Handle NCBI data retrieval using a separate thread.
 */
public class NcbiFetcher {
  private static String chooseFile(String rid) {
    try {
      DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
      return dmgr.getBlastDataPath() + rid + ".xml";
    } catch (IOException e) {
      EZLogger.warn(BVMessages.getString("FetchFromNcbiAction.err2") + e);
    }
    return null;
  }
  public static void fetchAndShow(String rid) {

    QBlastRetriever qRet;
    File tmpFile, resFile;
    String resFileStr;
    boolean bRet;

    // connect to the NCBI
    BlastViewerOpener.setHelperMessage(BVMessages
        .getString("FetchFromNcbiAction.msg1"));
    qRet = new QBlastRetriever();
    EZEnvironment.setWaitCursor();
    bRet = qRet.getBlastResult(rid);
    EZEnvironment.setDefaultCursor();
    // error ?
    if (bRet == false) {
      String msg = BVMessages.getString("FetchFromNcbiAction.err3"); 
      msg = MessageFormat.format(
          msg, 
          new Object[]{rid, qRet.getErrorMsg()});
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), msg);
    } else {
      // ok, then ask the user for a file to save the Blast data
      BlastViewerOpener.setHelperMessage(BVMessages
          .getString("FetchFromNcbiAction.msg2"));
      tmpFile = qRet.getResultFile();
      resFileStr = chooseFile(rid);
      if (resFileStr != null) {
        resFile = new File(resFileStr);
        try {
          EZFileUtils.copyFile(tmpFile, resFile);
          EZLogger.info(BVMessages.getString("FetchFromNcbiAction.msg3")
              + resFileStr);
          // setup an NCBI Blast Loader (XML)
          BlastViewerOpener.setHelperMessage(BVMessages
              .getString("FetchFromNcbiAction.msg4"));
          SRLoader ncbiBlastLoader = SerializerSystemFactory
              .getLoaderInstance(SerializerSystemFactory.NCBI_LOADER2);
          // load data from file
          SROutput so = ncbiBlastLoader.load(resFile);
          // display the viewer
          BlastViewerOpener.displayInternalFrame(BlastViewerOpener.prepareViewer(so), resFile.getName(),
              null);
        } catch (IOException e) {
          EZLogger.warn(BVMessages.getString("FetchFromNcbiAction.err1")
              + ": " + e);
          EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(),
              BVMessages.getString("FetchFromNcbiAction.err1") + ".");
        }
      }
      // discard the temporary file
      tmpFile.delete();
    }
    BlastViewerOpener.cleanHelperMessage();
  }

}
