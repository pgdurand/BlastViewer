/* Copyright (C) 2008-2017 Patrick G. Durand
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
package bzh.plealog.blastviewer.msa.consensus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.data.sequence.DSequence;
import bzh.plealog.bioinfo.api.data.sequence.DSymbol;
import bzh.plealog.bioinfo.api.data.sequence.stat.ConsensusCell;
import bzh.plealog.bioinfo.api.data.sequence.stat.ConsensusModel;

/**
 * This is the renderer used to display table column&apos;s header for JTable
 * displaying a consensus sequence. This class has been written to produce
 * header with a modified height: to do so, one has to have in mind that JTable
 * determines header column&apos;s height using the cell renderer. So this class
 * implements getPreferredSize() in order to produce the correct height for the
 * JTable&apos;s header.
 * 
 * @author Patrick G. Durand
 */
public class ConsensusCellRenderer extends DefaultTableCellRenderer {
  private static final long serialVersionUID = 4139284228372452945L;
  private Dimension         preferredSize_;
  private ConsensusCell     curCell_;
  private DSequence         refSequence_;
  private FontMetrics       fm_;

  public ConsensusCellRenderer(FontMetrics fm) {
    fm_ = fm;
    /*
     * 20 for width is ok: the width of a cell is not determined by the
     * preferred size of this renderer... JTable uses column value for that! On
     * the other hand JTable uses height given by the header renderer to figure
     * out what is the height oh the table header.
     */
    preferredSize_ = new Dimension(20, 2 * fm.getHeight());
    this.setHorizontalAlignment(SwingConstants.CENTER);
    this.setVerticalAlignment(SwingConstants.CENTER);
    this.setToolTipText("First row: query position | Second row: query letter | Third row: consensus");
  }

  public void setRefSequence(DSequence seq) {
    refSequence_ = seq;
    if (seq != null)
      preferredSize_.height = 3 * fm_.getHeight();
    else
      preferredSize_.height = 2 * fm_.getHeight();
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column) {
    ConsensusModel cModel;
    ConsensusCell cell;
    ConsensusCell[] headerCells;

    if (table.getModel() instanceof ConsensusModel) {
      cModel = (ConsensusModel) table.getModel();
      headerCells = cModel.getConsensusCells();
      if (headerCells != null) {
        cell = headerCells[column];
        curCell_ = cell;
      } else {
        curCell_ = null;
      }
      this.setText("");
    } else {
      this.setText(value.toString());
      curCell_ = null;
    }
    if (EZEnvironment.getOSType() == EZEnvironment.MAC_OS) {
      setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      setBackground(UIManager.getColor("Panel.background"));
    } else {
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    }
    return this;
  }

  public void paintComponent(Graphics g) {
    Rectangle bounds;
    String str;
    FontMetrics fm;
    Insets insets;
    DSymbol symbol;
    List<DSymbol> cons;
    int cx, cy, i, size, x, delta;

    super.paintComponent(g);

    if (curCell_ == null)
      return;
    if (refSequence_ == null)
      delta = 2;
    else
      delta = 3;
    fm = this.getFontMetrics(this.getFont());
    bounds = this.getBounds();
    insets = this.getInsets();
    cy = bounds.height - (insets.bottom + insets.top);
    str = curCell_.getColumnPos();
    cx = fm.stringWidth(str);
    g.drawString(str, (bounds.width - cx) / 2, cy / delta);
    if (refSequence_ != null) {
      symbol = refSequence_.getSymbol(curCell_.getColumnPos_i() - 1);
      if (symbol != null) {
        if (symbol.getGraphics() != null) {
          g.setColor(symbol.getGraphics().getTextColor());
        } else {
          g.setColor(Color.BLACK);
        }
        str = symbol.toString();
        cx = fm.stringWidth(str);
        g.drawString(str, (bounds.width - cx) / 2, 2 * cy / 3);
      }

    }
    cons = curCell_.getConsensus();
    size = cons.size();
    cx = 0;
    for (i = 0; i < size; i++) {
      symbol = cons.get(i);
      cx += fm.charWidth(symbol.getChar());
    }
    x = (bounds.width - cx) / 2;
    for (i = 0; i < size; i++) {
      symbol = cons.get(i);
      if (symbol != null) {
        if (symbol.getGraphics() != null) {
          g.setColor(symbol.getGraphics().getTextColor());
        } else {
          g.setColor(Color.BLACK);
        }
        g.drawString(symbol.toString(), x, cy);
        x += fm.charWidth(symbol.getChar());
      }
    }
  }

  public Dimension getPreferredSize() {
    return preferredSize_;
  }
}