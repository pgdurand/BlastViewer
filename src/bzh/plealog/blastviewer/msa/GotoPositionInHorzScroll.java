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
package bzh.plealog.blastviewer.msa;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * This class handles a control to allow the user to move within a horizontal
 * scroll bar.
 * 
 * @author Patrick G. Durand
 */
public class GotoPositionInHorzScroll extends JPanel {
  private static final long serialVersionUID = -4488151331599131257L;
  private JButton                             btn_;
  private JTextField                          pos_;
  private JTextField                          range_;
  private JScrollPane                         scroller_;
  private int                                 maxPos_;
  private ArrayList<GotoPositionInHorzScroll> synchroCompoList_;

  private static final String                 DEFAULT_RANGE = "0..0";

  public GotoPositionInHorzScroll() {
    Dimension dim;
    JPanel pnl, pnl2;

    synchroCompoList_ = new ArrayList<GotoPositionInHorzScroll>();
    btn_ = new JButton("Go");
    pos_ = new JTextField("");
    pos_.addKeyListener(new MyKeyListener());
    pos_.setEditable(true);
    range_ = new JTextField(DEFAULT_RANGE);
    range_.setEditable(false);
    // range_.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    range_.setOpaque(false);
    dim = range_.getPreferredSize();
    dim.width = 3 * dim.width / 2;
    pos_.setPreferredSize(dim);
    pos_.setMinimumSize(dim);
    pos_.setMaximumSize(dim);
    pnl = new JPanel(new BorderLayout());
    pnl.add(pos_, BorderLayout.CENTER);
    pnl.add(btn_, BorderLayout.EAST);

    pnl2 = new JPanel(new BorderLayout());
    pnl2.add(pnl, BorderLayout.CENTER);
    pnl2.add(range_, BorderLayout.EAST);
    this.setLayout(new BorderLayout());
    this.add(pnl2, BorderLayout.WEST);

    btn_.addActionListener(new GotoPositionInTableActListener());
  }

  /**
   * Sets the scroll to is under the control of this component.
   */
  public void registerScroller(JScrollPane sPanel) {
    scroller_ = sPanel;
  }

  /**
   * Adds another GotoPositionInHorzScroll to this one for synchronization
   * purpose. When position is modified in this component, then all others are
   * then updated accordingly.
   */
  public void addSynchronizedCompo(GotoPositionInHorzScroll compo) {
    synchroCompoList_.add(compo);
  }

  public void removeSynchronizedCompo(GotoPositionInHorzScroll compo) {
    synchroCompoList_.remove(compo);
  }

  /**
   * Sets the maximum position. This position usually corresponds to the maximum
   * value of the Component contolled by the Scroller. As an example if the
   * Scroller contains a JTable, then maxPos in the number of columns of the
   * JTable.
   */
  public void setMaxPosition(int maxPos) {
    maxPos_ = maxPos;
    if (maxPos > 0) {
      range_.setText("1.." + maxPos);
    } else {
      range_.setText(DEFAULT_RANGE);
    }
  }

  /**
   * Sets a position.
   */
  public void setPosition(int pos) {
    JScrollBar bar;
    int decal;

    if (scroller_ == null || maxPos_ == 0)
      return;
    bar = scroller_.getHorizontalScrollBar();
    if (pos < 1) {
      pos = 1;
    } else if (pos > maxPos_) {
      pos = maxPos_;
    }
    pos_.setText(String.valueOf(pos));
    decal = bar.getMaximum() / maxPos_ * (pos - 1);
    bar.setValue(decal);
  }

  public void setEnabled(boolean b) {
    btn_.setEnabled(b);
    pos_.setEnabled(b);
    range_.setEnabled(b);
  }

  private class GotoPositionInTableActListener implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      int curVal;
      try {
        curVal = Integer.valueOf(pos_.getText()).intValue();
      } catch (NumberFormatException e) {
        return;
      }
      setPosition(curVal);

      for (GotoPositionInHorzScroll c : synchroCompoList_) {
        c.setPosition(curVal);
      }
    }
  }

  private class MyKeyListener extends KeyAdapter {
    public void keyReleased(KeyEvent e) {
      super.keyReleased(e);
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        btn_.doClick();
      }
    }
  }
}