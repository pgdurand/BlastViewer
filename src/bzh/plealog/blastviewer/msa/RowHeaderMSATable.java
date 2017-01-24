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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * This is the table displaying the row header of the MSA.
 * 
 * @author Patrick G. Durand
 */
public class RowHeaderMSATable extends JTable implements ListSelectionListener {
  private static final long  serialVersionUID    = -468755652481189335L;
  private static final Color QUERY_CELL_BK_COLOR = new Color(184, 207, 229);
  private int                _oldSel             = -1;

  public RowHeaderMSATable(TableModel dm) {
    super(dm);
  }

  public TableCellRenderer getCellRenderer(int row, int column) {
    TableCellRenderer tcr;

    tcr = super.getCellRenderer(row, column);
    if (tcr instanceof JLabel) {
      JLabel lbl;

      lbl = (JLabel) tcr;
      lbl.setHorizontalAlignment(SwingConstants.CENTER);
      lbl.setForeground(Color.BLACK);
      lbl.setBackground(QUERY_CELL_BK_COLOR);
    }
    return tcr;
  }

  public void valueChanged(ListSelectionEvent e) {
    int sel;
    super.valueChanged(e);
    if (e.getValueIsAdjusting())
      return;

    sel = this.getSelectedRow();
    if (sel < 0 || sel == _oldSel)
      return;
    /*
     * if (_msaTable == null) return;
     */

    _oldSel = sel;
  }
}