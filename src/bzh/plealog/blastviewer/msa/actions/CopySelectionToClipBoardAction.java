/* Copyright (C) 2006-2017 Patrick G. Durand
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
package bzh.plealog.blastviewer.msa.actions;

import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import bzh.plealog.bioinfo.data.sequence.ExportableMSA;
import bzh.plealog.bioinfo.io.sequence.GCGMsaExport;
import bzh.plealog.blastviewer.msa.MSATable;
import bzh.plealog.blastviewer.msa.MSATableModel;
import bzh.plealog.blastviewer.msa.MsaUtils;
import bzh.plealog.blastviewer.msa.RowHeaderMSATable;
import bzh.plealog.blastviewer.msa.RowHeaderMSATableModel;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.ui.common.ClipBoardTextTransfer;

/**
 * Action to copy an entire MSA or a selected region to the clipboard.
 * 
 * @author Patrick G. Durand
 */
public class CopySelectionToClipBoardAction extends AbstractAction {
  private static final long    serialVersionUID = -938392533894021755L;
  private boolean              forceall;
  private MSATable             msaTable;
  private RowHeaderMSATable headerMsaTable;

  /**
   * Copy MSA to clipboard action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public CopySelectionToClipBoardAction(String name) {
    super(name);
  }

  /**
   * Copy MSA to clipboard action constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public CopySelectionToClipBoardAction(String name, Icon icon) {
    super(name, icon);
  }

  public void forceCopyEntireMSA(boolean force) {
    forceall = force;
  }

  public void setMsaTable(MSATable mtbl) {
    msaTable = mtbl;
  }

  public void setRowHeaderMSATable(RowHeaderMSATable mtbl) {
    headerMsaTable = mtbl;
  }

  public void actionPerformed(ActionEvent event) {
    ExportableMSA msa;
    GCGMsaExport exporter;
    ByteArrayOutputStream baos;
    ClipBoardTextTransfer cbtt;

    msa = MsaUtils.getExportableMSA((MSATableModel) msaTable.getModel(),
        msaTable.getSelectionModel(), msaTable.getColumnModel()
            .getSelectionModel(), (RowHeaderMSATableModel) headerMsaTable
            .getModel(), forceall);

    baos = new ByteArrayOutputStream();
    exporter = new GCGMsaExport(msa, 10, msa.columns(), false);
    if (!exporter.export(baos)) {
      String msg = BVMessages.getString("DDFileTypes.msf.err.msg1");
      EZEnvironment.displayErrorMessage(EZEnvironment.getParentFrame(), msg
          + ".");
      return;
    }
    cbtt = new ClipBoardTextTransfer();
    cbtt.setClipboardContents(baos.toString());
  }
}