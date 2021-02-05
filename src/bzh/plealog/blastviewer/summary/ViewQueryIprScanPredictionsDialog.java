/* Copyright (C) 2021 Patrick G. Durand
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
package bzh.plealog.blastviewer.summary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import bzh.plealog.bioinfo.api.data.feature.FeatureTable;
import bzh.plealog.bioinfo.api.data.sequence.BankSequenceDescriptor;
import bzh.plealog.bioinfo.data.sequence.EmptySequence;
import bzh.plealog.bioinfo.ui.feature.FeatureViewerFactory;
import bzh.plealog.bioinfo.ui.sequence.extended.CombinedAnnotatedSequenceViewer;
import bzh.plealog.bioinfo.util.DAlphabetUtils;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * Dialogue box to display Interprosan predictions.
 * 
 * @author Patrick G. Durand
 */
public class ViewQueryIprScanPredictionsDialog extends JDialog {
  private static final long serialVersionUID = 5928719684421848123L;

  private CombinedAnnotatedSequenceViewer _viewer;
  private JPanel                          _resultPanel;
  private Dimension                       _startingSize = new Dimension(840,480);
  
  /**
   * Constructor.
   * 
   * @param parent parent frame
   * @param queryID query ID shown in dialogue header
   */
  public ViewQueryIprScanPredictionsDialog(Frame parent, String queryID) {
    super(parent);
    setLocationByPlatform(true);
    setPreferredSize(_startingSize);
    setSize(_startingSize);
    setTitle(String.format(
        BVMessages.getString("EditQueryClassificationDialog.this.title"), queryID));

    _resultPanel = new JPanel(new BorderLayout());
    _viewer = new CombinedAnnotatedSequenceViewer(
        null, BVMessages.class.getResourceAsStream("featureWebLink.conf"), 
        false, false,  false, false, false, true, false, false, 
        FeatureViewerFactory.TYPE.COMBO);
    _resultPanel.add(_viewer);
    getContentPane().add(_resultPanel, BorderLayout.CENTER);
    
    JPanel panel = new JPanel();
    getContentPane().add(panel, BorderLayout.SOUTH);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

    JButton btnOk = new JButton(
        BVMessages.getString("EditQueryClassificationDialog.btnOk.text"));
    btnOk.setPreferredSize(new Dimension(65, 23));
    btnOk.setMinimumSize(new Dimension(65, 23));
    btnOk.setMaximumSize(new Dimension(65, 23));
    btnOk.addActionListener(new OkListener());
    panel.add(btnOk);

    this.setModal(true);
    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    // to center
    this.setLocationRelativeTo(null);
  }

  /**
   * Set data.
   * 
   * @param ft feature table
   * @param seqsize size of query sequence
   */
  public void setData(FeatureTable ft, int seqsize) {
    BankSequenceDescriptor sd = new BankSequenceDescriptor(
        ft, null, new EmptySequence(DAlphabetUtils.getIUPAC_DNA_Alphabet(), seqsize));
    _viewer.setData(sd);
  }
  
  private void close() {
    dispose();
  }

  private class OkListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      close();
    }
  }
}
