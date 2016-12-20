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
package bzh.plealog.blastviewer.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.io.SRLoader;
import bzh.plealog.bioinfo.io.searchresult.SerializerSystemFactory;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.blastviewer.BlastViewerPanel;
import bzh.plealog.blastviewer.client.ncbi.QBlastRetriever;
import bzh.plealog.blastviewer.config.directory.DirManager;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

/**
 * This class implements the action used to fetch a Blast results directly from
 * the NCBI.
 * 
 * @author Patrick G. Durand
 */
public class FetchFromNcbiAction extends AbstractAction {
  private static final long serialVersionUID = -2654059939053088591L;

  private boolean           _fetcherLocked   = false;

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public FetchFromNcbiAction(String name) {
    super(name);
  }

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public FetchFromNcbiAction(String name, Icon icon) {
    super(name, icon);
  }

  public void actionPerformed(ActionEvent event) {
    String rid;
    if (_fetcherLocked)
      return;
    rid = JOptionPane.showInputDialog(EZEnvironment.getParentFrame(),
        BVMessages.getString("FetchFromNcbiAction.lbl"),
        EZApplicationBranding.getAppName(), JOptionPane.QUESTION_MESSAGE);
    if (rid == null || rid.length() == 0)
      return;
    new Fetcher(rid).start();
  }

  /**
   * Handle the data retrieval using a separate thread.
   */
  private class Fetcher extends Thread {
    private String _rid;

    public Fetcher(String rid) {
      _rid = rid;
    }

    private String chooseFile() {
      try {
        DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
        return dmgr.getBlastDataPath() + _rid + ".xml";
      } catch (IOException e) {
        EZLogger.warn(BVMessages.getString("FetchFromNcbiAction.err2") + e);
      }
      return null;
    }

    public void run() {
      _fetcherLocked = true;

      QBlastRetriever qRet;
      File tmpFile, resFile;
      String resFileStr;
      boolean bRet;

      // connect to the NCBI
      BlastViewerOpener.setHelperMessage(BVMessages
          .getString("FetchFromNcbiAction.msg1"));
      qRet = new QBlastRetriever();
      EZEnvironment.setWaitCursor();
      bRet = qRet.getBlastResult(_rid);
      EZEnvironment.setDefaultCursor();
      // error ?
      if (bRet == false) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(),
            qRet.getErrorMsg());
      } else {
        // ok, then ask the user for a file to save the Blast data
        BlastViewerOpener.setHelperMessage(BVMessages
            .getString("FetchFromNcbiAction.msg2"));
        tmpFile = qRet.getResultFile();
        resFileStr = chooseFile();
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
                .getLoaderInstance(SerializerSystemFactory.NCBI_LOADER);
            // load data from file
            SROutput so = ncbiBlastLoader.load(resFile);
            // prepare the viewer
            BlastViewerPanel viewer = new BlastViewerPanel();
            viewer.setContent(so);
            // display the viewer
            BlastViewerOpener.displayInternalFrame(viewer, resFile.getName(),
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
      _fetcherLocked = false;
    }
  }
}
