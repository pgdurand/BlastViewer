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
package bzh.plealog.blastviewer.msa;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import bzh.plealog.bioinfo.api.data.sequence.DSymbol;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.blast.config.color.ColorPolicyConfig;
import bzh.plealog.bioinfo.ui.logo.LogoCellRenderer;

/**
 * A multiple sequence alignment is displayed using a standard JTable. In this 
 * table, two first rows have special purpose: display a logo sequence and the
 * consensus.
 * 
 * @author Patrick G. Durand
 */
public class MSATable extends JTable {
  private static final long serialVersionUID = -906538065400807739L;
  private ColorPolicyConfig _cpc;
  private LogoCellRenderer  _logoCellRenderer;

  public MSATable() {
    super();
    _cpc = (ColorPolicyConfig) ConfigManager.getConfig(ColorPolicyConfig.NAME);
  }

  public void setLogoCellRenderer(LogoCellRenderer lcr) {
    this._logoCellRenderer = lcr;
  }

  public TableCellRenderer getCellRenderer(int row, int column) {
    TableCellRenderer tcr;
    boolean useRV = false;

    if (row > 0) {
      tcr = super.getCellRenderer(row, column);
      if (tcr instanceof JLabel) {
        JLabel lbl;

        lbl = (JLabel) tcr;
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        DSymbol symbol = (DSymbol) getModel().getValueAt(row, column);
        if (_cpc != null && _cpc.useInverseVideo()) {
          useRV = true;
        }
        if (symbol.getGraphics() != null) {
          lbl.setBackground(useRV ? symbol.getGraphics().getTextColor()
              : Color.WHITE);
          lbl.setForeground(useRV ? Color.WHITE : symbol.getGraphics()
              .getTextColor());
        } else {
          lbl.setForeground(Color.BLACK);
          lbl.setBackground(Color.WHITE);
        }
      }
    } else {
      tcr = this._logoCellRenderer;
    }
    return tcr;
  }

}