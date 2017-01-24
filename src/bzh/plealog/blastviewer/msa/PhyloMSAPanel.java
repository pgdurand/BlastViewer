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
import java.awt.event.ActionEvent;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bzh.plealog.bioinfo.api.data.sequence.DAlphabet;
import bzh.plealog.bioinfo.data.sequence.ExportableMSA;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListSupport;
import bzh.plealog.blastviewer.phylo.HspPhyloPanel;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * This class is responsible for displaying the graphical multiple sequence
 * alignment and the phylogenetic tree of a Blast output.
 * 
 * @author Patrick G. Durand
 */
public class PhyloMSAPanel extends MSAPanel {
  private static final long serialVersionUID   = 5875654457852498154L;
  boolean                   _treeComputed;
  private CreateTreeAction  _treeCreator;
  private HspPhyloPanel     _phyloViewer;
  JTabbedPane               _jtp;
  private JLabel            _phyloMsgLbl;

  // A phylogenetic tree won't be computed if MSA is greater than:
  // (performance issue related to Epos)
  private static final int  ROWS_SIZE_LIMIT    = 500;
  private static final int  COLUMNS_SIZE_LIMIT = 5000;

  /**
   * Constructor.
   */
  public PhyloMSAPanel() {
    super();
  }

  protected void buildGUI() {// will be called by constructor.super()
    JPanel panelPhylo = new JPanel();
    _phyloMsgLbl = new JLabel();
    _phyloViewer = new HspPhyloPanel();
    panelPhylo.setLayout(new BorderLayout());
    panelPhylo.add(_phyloMsgLbl, BorderLayout.NORTH);
    panelPhylo.add(_phyloViewer, BorderLayout.CENTER);

    _jtp = new JTabbedPane();
    _jtp.setFocusable(false);
    _jtp.add(BVMessages.getString("BlastHitMSA.tab1"), prepareMSAPanel());
    _jtp.add(BVMessages.getString("BlastHitMSA.tab2"), panelPhylo);
    _jtp.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        JTabbedPane pane = (JTabbedPane) evt.getSource();
        // _scroll visible status can be used to figure out if a MSA is
        // displayed!
        if (!_scroll.isVisible())
          return;
        // when the user selects the Tree tab, and if no Tree is displayed, then
        // a Tree is created just before showing the Tree viewer
        if (pane.getSelectedIndex() == 1 /* && _phyloViewer.getTree()==null */) {
          buildPhyloTree(MsaUtils.getExportableMSA((MSATableModel) _msaTable
              .getModel(), _msaTable.getSelectionModel(), _msaTable
              .getColumnModel().getSelectionModel(),
              (RowHeaderMSATableModel) _rowHeaderMsaTable.getModel(), true));
        }
      }
    });

    this.setLayout(new BorderLayout());
    this.add(_jtp, BorderLayout.CENTER);

  }

  /**
   * Register to the central notification system handling selection of hits on
   * the various viewers.
   */
  public void registerHitListSupport(BlastHitListSupport us) {
    super.registerHitListSupport(us);
    _phyloViewer.registerHitListSupport(us);
  }

  protected void resetViewer() {
    super.resetViewer();
    _phyloViewer.clear();
    _phyloMsgLbl.setText("");
    _phyloMsgLbl.setVisible(false);
  }

  protected void computeData() {
    // compute MSA (parent class is MSAPanel)
    super.computeData();

    // compute Tree...
    _treeComputed = false;
    int selTab = _jtp.getSelectedIndex();
    // ... only if PhyoPanel is displayed. This is to avoid computing Tree
    // while viewer is not displayed
    if (selTab == 1 && _scroll.isVisible()) {
      buildPhyloTree(MsaUtils.getExportableMSA(
          (MSATableModel) _msaTable.getModel(), _msaTable.getSelectionModel(),
          _msaTable.getColumnModel().getSelectionModel(),
          (RowHeaderMSATableModel) _rowHeaderMsaTable.getModel(), true));
    }
  }

  protected void activateOptionCommands(boolean val) {
    super.activateOptionCommands(val);
    _treeCreator.setEnabled(val);
  }

  protected void addMSAToolbarCommand(JToolBar tBar) {
    ImageIcon icon;
    JButton btn;

    // Create a phylogenetic tree
    icon = EZEnvironment.getImageIcon("tree.png");
    if (icon != null) {
      _treeCreator = new CreateTreeAction("", icon);
    } else {
      _treeCreator = new CreateTreeAction(
          BVMessages.getString("BlastHitMSA.phylo.tree.btn"));
    }
    _treeCreator.setMsaTable(_msaTable);
    _treeCreator.setRowHeaderMSATable(_rowHeaderMsaTable);
    btn = tBar.add(_treeCreator);
    btn.setToolTipText(BVMessages.getString("BlastHitMSA.phylo.tree.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("BlastHitMSA.phylo.tree.btn"));
    tBar.addSeparator();

  }

  /**
   * Utility method used to compute a phylogenetic tree from the MSA.
   * 
   * @param msa
   *          the data used to compute the Tree.
   */
  private boolean buildPhyloTree(ExportableMSA msa) {
    String[] headers, seqs;
    boolean isProteic;

    if (_treeComputed)
      return false;
    headers = MsaUtils.getHeaders(msa, false);
    if (headers.length < 3) {
      EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(),
          BVMessages.getString("BlastHitMSA.tree.err1"));
      return false;
    }
    if ((msa.toColumn() - msa.fromColumn() + 1) > COLUMNS_SIZE_LIMIT) {
      String msg = BVMessages.getString("BlastHitMSA.compute.err2");
      _phyloViewer.setMSAInfo(MessageFormat.format(msg,
          new Object[] { COLUMNS_SIZE_LIMIT }));
      return false;
    }
    if ((msa.toRow() - msa.fromRow() + 1) > ROWS_SIZE_LIMIT) {
      String msg = BVMessages.getString("BlastHitMSA.compute.err3");
      _phyloViewer.setMSAInfo(MessageFormat.format(msg,
          new Object[] { ROWS_SIZE_LIMIT }));
      return false;
    }
    _phyloMsgLbl.setText("");
    _phyloMsgLbl.setVisible(false);
    seqs = MsaUtils.getSequences(msa, false, '.');
    isProteic = (msa.getSequence(0).getSequence().getAlphabet().getType() == DAlphabet.PROTEIN_ALPHABET);
    _phyloViewer
        .setMap(((RowHeaderMSATableModel) _rowHeaderMsaTable.getModel())
            .getBhhMap());
    _phyloViewer.setMSAInfo(seqs.length, msa.fromColumn() + 1,
        msa.toColumn() + 1);
    _phyloViewer.setData(headers, seqs, isProteic);
    _treeComputed = true;
    return true;
  }

  /**
   * Action to create a Tree from either user selection or entire MSA.
   * */
  private class CreateTreeAction extends AbstractAction {

    private static final long serialVersionUID = -1894254490529520399L;
    private MSATable          msaTable;
    private RowHeaderMSATable headerMsaTable;

    /**
     * Create a Phylogenetic Tree from the current MSA.
     * 
     * @param name
     *          the name of the action.
     * @param blastHitMSA
     *          TODO
     */
    private CreateTreeAction(String name) {
      super(name);
    }

    /**
     * Create a Phylogenetic Tree from the current MSA.
     * 
     * @param name
     *          the name of the action.
     * @param icon
     *          the icon of the action.
     * @param blastHitMSA
     *          TODO
     */
    private CreateTreeAction(String name, Icon icon) {
      super(name, icon);
    }

    public void setMsaTable(MSATable mtbl) {
      msaTable = mtbl;
    }

    public void setRowHeaderMSATable(RowHeaderMSATable mtbl) {
      headerMsaTable = mtbl;
    }

    public void actionPerformed(ActionEvent event) {
      _treeComputed = false;
      ExportableMSA msa = MsaUtils.getExportableMSA((MSATableModel) msaTable
          .getModel(), msaTable.getSelectionModel(), msaTable.getColumnModel()
          .getSelectionModel(), (RowHeaderMSATableModel) headerMsaTable
          .getModel(), msaTable.getSelectionModel().isSelectionEmpty());

      if (buildPhyloTree(msa)) {
        _jtp.setSelectedIndex(1);
      }
    }
  }

}
