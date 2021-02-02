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
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SRIteration;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * This class implements the action to load a Blast XML results from a file.
 * 
 * @author Patrick G. Durand
 */
public class OpenFileAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public OpenFileAction(String name) {
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
  public OpenFileAction(String name, Icon icon) {
    super(name, icon);
  }

  private class Loader extends Thread {
    private void doAction() {
      //EZFileManager.useOSNativeFileDialog(true);
      File[] fs = EZFileManager.chooseFilesForOpenAction(BVMessages
          .getString("OpenFileAction.lbl"));
      if (fs == null)// user canceled dlg box
        return;

      EZEnvironment.setWaitCursor();

      EZLogger.info(BVMessages
          .getString("OpenFileAction.msg1"));
      SROutput sro, sroMaster=null;
      int notLoadedFiles=0, ncount=0;
      for (File f:fs) {
        ncount++;
        BlastViewerOpener.setHelperMessage(BVMessages
            .getString("OpenFileAction.msg1")+ncount+"/"+fs.length);
        sro = BlastViewerOpener.readBlastFile(f);
        if (sro==null) {
          notLoadedFiles++;
          EZLogger.warn(
              BVMessages.getString("OpenFileAction.err") +
              f.getAbsolutePath());
          continue;
        }
        if (sroMaster == null) {
          sroMaster = sro;
        }
        else {
          Enumeration<SRIteration> sriEnum = sro.enumerateIteration();
          while(sriEnum.hasMoreElements()) {
            sroMaster.addIteration(sriEnum.nextElement());
          }
        }
      }
      
      EZLogger.info(String.format(
          BVMessages.getString("OpenFileAction.msg4"), fs.length));
      
      if (sroMaster!=null) {
        if(notLoadedFiles!=0) {
          EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), 
              BVMessages.getString("OpenFileAction.msg2") );
        }
        BlastViewerOpener.setHelperMessage(BVMessages
            .getString("FetchFromNcbiAction.msg4"));
        
        JComponent viewer = BlastViewerOpener.prepareViewer(sroMaster);
        
        BlastViewerOpener.displayInternalFrame(viewer, fs[0].getName(), null);
        EZLogger.info(BVMessages.getString("OpenFileAction.msg5"));
      }
      else {
        EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), 
            BVMessages.getString("OpenFileAction.msg3") );
      }
      
      System.gc();
    }
    public void run() {
      try {
        doAction();
      } catch (Throwable t) {
        EZLogger.warn(
            BVMessages.getString("OpenFileAction.err") +
            t.toString());
      } finally {
        BlastViewerOpener.cleanHelperMessage();
        EZEnvironment.setDefaultCursor();
      }
    }
  }

  public void actionPerformed(ActionEvent event) {
    new Loader().start();
  }

}
