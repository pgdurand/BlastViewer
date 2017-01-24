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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bzh.plealog.bioinfo.api.data.sequence.stat.ConsensusModel;

/**
 * This class handles a JSlider that can be used to fix the value used to
 * compute the consensus.
 * 
 * @author Patrick G. Durand
 */
public class ConsensusSlider extends JPanel {
  private static final long serialVersionUID = 1913652841387800463L;
  private JSlider _cSlider;
  private JLabel  _lblCurValue;
  private JLabel  _lblMinValue;
  private JLabel  _lblMaxValue;
  private JTable  _dataTable;

  public ConsensusSlider(JTable dataTable) {
    super();
    JPanel panel;
    FontMetrics fm;
    Dimension dim;

    panel = new JPanel(new BorderLayout());
    _dataTable = dataTable;
    this.setLayout(new BorderLayout());
    _cSlider = new JSlider();
    _cSlider.addChangeListener(new ConsensusEventSlider(this));
    _lblCurValue = new JLabel(String.valueOf(_cSlider.getValue()));
    _lblMinValue = new JLabel("   " + String.valueOf(_cSlider.getMinimum()));
    _lblMaxValue = new JLabel(String.valueOf(_cSlider.getMaximum()));
    fm = _lblCurValue.getFontMetrics(_lblCurValue.getFont());
    dim = new Dimension(fm.stringWidth("xxxxx"), fm.getHeight() + 2);
    _lblCurValue.setPreferredSize(dim);
    _lblCurValue.setMinimumSize(dim);
    _lblCurValue.setMaximumSize(dim);
    _lblCurValue.setHorizontalAlignment(SwingConstants.CENTER);
    panel.add(_lblMinValue, BorderLayout.WEST);
    panel.add(_cSlider, BorderLayout.CENTER);
    panel.add(_lblMaxValue, BorderLayout.EAST);
    this.add(_lblCurValue, BorderLayout.WEST);
    this.add(panel, BorderLayout.CENTER);
  }

  public void setEnabled(boolean b) {
    _cSlider.setEnabled(b);
    _lblCurValue.setEnabled(b);
    _lblMinValue.setEnabled(b);
    _lblMaxValue.setEnabled(b);
  }

  public void updateModel(int min, int max, int val) {
    _cSlider.setMinimum(min);
    _cSlider.setMaximum(max);
    _cSlider.setValue(val);
    _lblMinValue.setText("   " + String.valueOf(min));
    _lblMaxValue.setText(String.valueOf(max));
  }

  public int getValue() {
    return _cSlider.getValue();
  }

  public void updateTableHeader(int consLimit) {
    ConsensusModel cModel;

    _lblCurValue.setText(String.valueOf(consLimit));
    cModel = (ConsensusModel) _dataTable.getModel();
    cModel.computeConsensus(consLimit);
  }

  /**
   * Handles events coming from the consensus JSlider.
   */
  class ConsensusEventSlider implements ChangeListener {
    private ConsensusSlider _cSlider;

    public ConsensusEventSlider(ConsensusSlider cSlider) {
      _cSlider = cSlider;
    }

    public void stateChanged(ChangeEvent event) {
      JSlider slider;
      int consValue;

      slider = (JSlider) event.getSource();
      consValue = slider.getValue();
      _cSlider.updateTableHeader(consValue);
    }
  }
}