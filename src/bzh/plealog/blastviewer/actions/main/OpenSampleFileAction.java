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
package bzh.plealog.blastviewer.actions.main;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.blastviewer.config.directory.DirManager;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

/**
 * This class implements the action to load a Blast XML results from a file.
 * 
 * @author Patrick G. Durand
 */
public class OpenSampleFileAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;

  private static final String NCBI_BLAST_SAMPLE = "p12253-refseq.xml";
  
  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public OpenSampleFileAction(String name) {
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
  public OpenSampleFileAction(String name, Icon icon) {
    super(name, icon);
  }


  private void doAction() {
    String sampleBlastFile = null;
    
    try {
      DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
      sampleBlastFile = dmgr.getBlastDataPath() + NCBI_BLAST_SAMPLE;
    } catch (IOException e) {
      EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
          BVMessages.getString("OpenSampleFileAction.err1"));
      EZLogger.warn(e.toString());
      return;
    }
    
    File f = new File(sampleBlastFile);
    
    if (f.exists() == false){
      InputStream  in = BVMessages.class
            .getResourceAsStream(NCBI_BLAST_SAMPLE);

        try (FileOutputStream fos= new FileOutputStream(f);
            BufferedInputStream bis = new BufferedInputStream(in)) {
          int n;
          byte[] buf = new byte[2048];
          while ((n = bis.read(buf)) != -1) {
            fos.write(buf, 0, n);
          }
          fos.flush();
        } catch (IOException e) {
          EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
              BVMessages.getString("OpenSampleFileAction.err2"));
          EZLogger.warn(e.toString());
          return;
        }
      }

    EZEnvironment.setWaitCursor();

    BlastViewerOpener.displayInternalFrame(
        BlastViewerOpener.prepareViewer(BlastViewerOpener.readBlastFile(f)),
        f.getName(), null);
  }

  public void actionPerformed(ActionEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("OpenFileAction.err")
              + t.toString());
        } finally {
          EZEnvironment.setDefaultCursor();
        }
      }
    });
  }

}
