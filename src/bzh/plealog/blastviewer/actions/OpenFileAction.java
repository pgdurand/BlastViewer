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

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.io.SRLoader;
import bzh.plealog.bioinfo.io.searchresult.SerializerSystemFactory;
import bzh.plealog.blastviewer.BlastViewerPanel;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

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

  private SROutput readBlastFile(File f) {
    // setup an NCBI Blast Loader (XML)
    SRLoader ncbiBlastLoader = SerializerSystemFactory
        .getLoaderInstance(SerializerSystemFactory.NCBI_LOADER);
    return ncbiBlastLoader.load(f);
  }

  private void doAction() {
    File f = EZFileManager.chooseFileForOpenAction(BVMessages
        .getString("OpenFileAction.lbl"));
    if (f == null)// user canceled dlg box
      return;

    EZEnvironment.setWaitCursor();

    BlastViewerPanel viewer = new BlastViewerPanel();
    viewer.setContent(readBlastFile(f));

    BlastViewerOpener.displayInternalFrame(viewer, f.getName(), null);
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
