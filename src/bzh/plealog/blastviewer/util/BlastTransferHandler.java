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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

/**
 * This class handles drag and drop operations to open files automatically on
 * the Desktop MDI panel.
 * 
 * @author Patrick G. Durand
 */
public class BlastTransferHandler extends TransferHandler {

  private static final long serialVersionUID = -1115977216221345030L;
  private DataFlavor        _fileFlavor;

  /**
   * Constructor.
   */
  public BlastTransferHandler() {
    _fileFlavor = DataFlavor.javaFileListFlavor;
  }

  /**
   * @see TransferHandler#importData(JComponent, TransferSupport)
   * */
  public boolean importData(JComponent c, Transferable t) {
    final List<?> lFiles;

    if (!canImport(c, t.getTransferDataFlavors())) {
      return false;
    }
    if (hasFileFlavor(t.getTransferDataFlavors())) {
      try {
        lFiles = (List<?>) t.getTransferData(_fileFlavor);
        EZEnvironment.setWaitCursor();
        //new Fetcher(lFiles).start();
        new FileLoadRunner(lFiles.toArray(new File[0])).start();
      } catch (Exception e) {
        EZLogger.warn("DragNDrop error: " + e);
      }
    }
    return true;
  }

  /**
   * @see TransferHandler#canImport(JComponent, DataFlavor[])
   * */
  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    return hasFileFlavor(flavors);
  }

  /**
   * Check that we have a javaFileListFlavor.
   * 
   */
  private boolean hasFileFlavor(DataFlavor[] flavors) {
    for (int i = 0; i < flavors.length; i++) {
      if (_fileFlavor.equals(flavors[i])) {
        return true;
      }
    }
    return false;
  }

}
