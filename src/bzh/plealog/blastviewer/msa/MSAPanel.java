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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.SRRequestInfo;
import bzh.plealog.bioinfo.api.data.sequence.DAlphabet;
import bzh.plealog.bioinfo.api.data.sequence.DSequence;
import bzh.plealog.bioinfo.api.data.sequence.stat.PositionSpecificMatrix;
import bzh.plealog.bioinfo.data.sequence.DRulerModelImplem;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHSP;
import bzh.plealog.bioinfo.ui.blast.core.BlastIteration;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListEvent;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListListener;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListSupport;
import bzh.plealog.bioinfo.ui.blast.event.BlastIterationListEvent;
import bzh.plealog.bioinfo.ui.blast.event.BlastIterationListListener;
import bzh.plealog.bioinfo.ui.logo.LogoCellRenderer;
import bzh.plealog.bioinfo.ui.logo.LogoPanel;
import bzh.plealog.bioinfo.ui.sequence.basic.DRulerViewer;
import bzh.plealog.bioinfo.ui.util.BasicSelectTableAction;
import bzh.plealog.blastviewer.msa.actions.CopySelectionToClipBoardAction;
import bzh.plealog.blastviewer.msa.actions.ExportMSAAction;
import bzh.plealog.blastviewer.msa.consensus.ConsensusCellRenderer;
import bzh.plealog.blastviewer.msa.consensus.ConsensusSlider;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.ui.common.ContextMenuElement;
import com.plealog.genericapp.ui.common.ContextMenuManager;
import com.plealog.genericapp.ui.common.ImageManagerAction;

/**
 * This class is responsible for displaying the graphical multiple sequence
 * alignment of a Blast output. This MSA is created using the query sequence as
 * the anchor.
 * 
 * @author Patrick G. Durand
 */
public class MSAPanel extends JPanel implements BlastIterationListListener,
    BlastHitListListener {
  private static final long              serialVersionUID = 5875654457852498154L;
  private BlastIteration                 _curIter;
  /** the table displaying the MSA */
  protected MSATable                     _msaTable;
  /** the table displaying the row header for the MSA */
  protected RowHeaderMSATable            _rowHeaderMsaTable;
  /** the scroller of the MSA table */
  protected JScrollPane                  _scroll;
  /** List of listener to the selection on the Table */
  private BlastHitListSupport            _updateSpport;
  /** Slider used to modify consensus */
  private ConsensusSlider                _consensusSlider;
  private DRulerViewer                   _rulerViewer;
  /** control to go at a particular position in the table */
  private GotoPositionInHorzScroll       _positionner;
  /** used to lock selection when user click on the table */
  private boolean                        _bLockSelection;
  private boolean                        _msaComputed;
  private ExportMSAAction                _exporter;
  private CopySelectionToClipBoardAction _cpBoardAction;
  private ConsensusCellRenderer          _consensusRenderer;
  private ImageManagerAction             _imager;
  private Font                           _fnt             = new Font("Arial",
                                                              Font.PLAIN, 12);
  private ContextMenuManager             _contextMnu;
  private int                            _logoCellHeight  = 60;
  private int                            _cellW           = 15;
  private JLabel                         _msaMsgLbl;
  private boolean                        _displayed       = true;

  private static final int               QUERY_SIZE_LIMIT = 32000;

  /**
   * Constructor.
   * 
   * Creates an empty MSA viewer panel. It is worth noting that this viewer is
   * intended to be used as part of the BlastViewer. This is the reason why
   * there is no constructor with a MSA DataModel argument: data is set to this
   * viewer through the call of iterationChanged method.
   * 
   * @see BlastIterationListListener#iterationChanged(BlastIterationListEvent)
   */
  public MSAPanel() {
    super();
    buildGUI();
  }

  /**
   * Prepare the whole UI.
   */
  protected void buildGUI() {
    this.setLayout(new BorderLayout());
    this.add(prepareMSAPanel(), BorderLayout.CENTER);
  }

  /**
   * Prepare and assemble a full-featured viewer. It contains a header
   * displaying Logo Sequence and Consensus, the MSA itself as well as some
   * actions.
   */
  protected JComponent prepareMSAPanel() {
    JPanel panel, ctrlPanel, aliPnl, panelMsa;
    Dimension d;
    JTableHeader corner;

    _positionner = new GotoPositionInHorzScroll();
    /*
     * The following code has been adapted (and highly simplified) from hints
     * provided on http://www.chka.de/swing/table/row-headers/.
     */
    // creates the table displaying the MSA
    _msaTable = new MSATable();
    _consensusSlider = new ConsensusSlider(_msaTable);
    _msaTable.setLogoCellRenderer(new LogoCellRenderer(0, 0));
    _msaTable.setModel(new MSATableModel());
    _msaTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    _msaTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    _msaTable.setColumnSelectionAllowed(true);
    _msaTable.setRowSelectionAllowed(true);
    _msaTable.setCellSelectionEnabled(true);
    _msaTable.addMouseListener(new TableMouseListener());
    _msaTable.getTableHeader().setReorderingAllowed(false);
    _msaTable.setGridColor(Color.WHITE);
    _msaTable.setFont(_fnt);

    _consensusSlider.setEnabled(false);
    _positionner.setEnabled(false);

    intCellWidth();
    _consensusRenderer = new ConsensusCellRenderer(
        _msaTable.getFontMetrics(_msaTable.getFont()));
    _msaTable.setTableHeader(null);

    // creates the table displaying the row headers for HitMSATable
    _rowHeaderMsaTable = new RowHeaderMSATable(new RowHeaderMSATableModel());
    _rowHeaderMsaTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _rowHeaderMsaTable.setColumnSelectionAllowed(false);
    _rowHeaderMsaTable.setRowSelectionAllowed(true);
    _rowHeaderMsaTable.getTableHeader().setDefaultRenderer(
        new ConsensusCellRenderer(_rowHeaderMsaTable
            .getFontMetrics(_rowHeaderMsaTable.getFont())));
    _rowHeaderMsaTable.setGridColor(Color.LIGHT_GRAY);
    _rowHeaderMsaTable.getSelectionModel().addListSelectionListener(
        new BlastHitTableSelectionListener());

    MsaUtils.initActionMap(_msaTable, _rowHeaderMsaTable);

    // sets column width appropriately
    initColumnSizeRowHeaderTable();
    d = _rowHeaderMsaTable.getPreferredScrollableViewportSize();
    d.width = _rowHeaderMsaTable.getPreferredSize().width;
    _rowHeaderMsaTable.setPreferredScrollableViewportSize(d);
    _rowHeaderMsaTable.setRowHeight(_msaTable.getRowHeight());
    // gets the TableHeader from RowHeaderHitMSATable. It will be put on the
    // upper left corner of the scroll pane
    corner = _rowHeaderMsaTable.getTableHeader();
    corner.setReorderingAllowed(false);
    corner.setResizingAllowed(true);

    DRulerModelImplem drm = new DRulerModelImplem(1, 1, 1);
    _rulerViewer = new DRulerViewer(drm, _msaTable.getFontMetrics(
        _msaTable.getFont()).getHeight(), SwingConstants.HORIZONTAL,
        SwingConstants.TOP);

    aliPnl = new JPanel(new BorderLayout());
    aliPnl.add(_msaTable, BorderLayout.CENTER);

    _scroll = new JScrollPane(aliPnl);
    _scroll.setColumnHeaderView(_rulerViewer);
    _scroll.setRowHeaderView(_rowHeaderMsaTable);
    _scroll.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner);
    _scroll.getHorizontalScrollBar().setBlockIncrement(40 * _cellW);
    _scroll.getHorizontalScrollBar().setUnitIncrement(_cellW);
    panel = new JPanel(new BorderLayout());
    panel.add(_scroll, BorderLayout.CENTER);
    panel.addComponentListener(new TableComponentAdapter());

    ctrlPanel = new JPanel(new BorderLayout());
    ctrlPanel.add(createConsensusPanel(), BorderLayout.WEST);
    ctrlPanel.add(getToolbar(), BorderLayout.EAST);
    _positionner.registerScroller(_scroll);

    panelMsa = new JPanel();
    _msaMsgLbl = new JLabel();
    panelMsa.setLayout(new BorderLayout());
    panelMsa.add(_msaMsgLbl, BorderLayout.NORTH);
    panelMsa.add(panel, BorderLayout.CENTER);
    panelMsa.add(ctrlPanel, BorderLayout.SOUTH);

    setContextMenu();
    activateOptionCommands(false);
    _scroll.setVisible(false);

    return panelMsa;
  }

  /**
   * Creates the consensus panel.
   */
  private Component createConsensusPanel() {
    DefaultFormBuilder builder;
    FormLayout layout;

    layout = new FormLayout("right:max(20dlu;p), 2dlu, 100dlu, 10dlu, "
        + "right:max(20dlu;p), 2dlu, 110dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    builder.append(new JLabel(BVMessages.getString("PscmMsaViewer.lbl.cons")),
        _consensusSlider);
    builder.append(new JLabel(BVMessages.getString("PscmMsaViewer.lbl.pos")),
        _positionner);
    return builder.getContainer();
  }

  /**
   * Register to the central notification system handling selection of hits on
   * the various viewers.
   */
  public void registerHitListSupport(BlastHitListSupport us) {
    _updateSpport = us;
  }

  /**
   * Implementation of BlastHitListListener interface. This implementation is
   * intended to listen to notifications from BlastHitList.
   */
  public void hitChanged(BlastHitListEvent e) {
    RowHeaderEntry entry;
    List<BlastHitHSP> hits;
    BlastHitHSP hit, tmpHit;
    int i, size, dy;

    if (e.getSource() == this)
      return;
    hits = e.getHitHsps();
    _bLockSelection = true;
    if (hits == null || hits.isEmpty() || hits.size() > 1) {
      _rowHeaderMsaTable.getSelectionModel().clearSelection();
    } else {
      size = _rowHeaderMsaTable.getModel().getRowCount();
      dy = _scroll.getVerticalScrollBar().getMaximum() / size;
      hit = hits.get(0);
      for (i = 1; i < size; i++) {
        entry = (RowHeaderEntry) _rowHeaderMsaTable.getModel()
            .getValueAt(i, -1);
        tmpHit = entry.getBhh();
        if (tmpHit != null
            && hit.getHit().getHitNum() == tmpHit.getHit().getHitNum()
            && hit.getHspNum() == tmpHit.getHspNum()) {
          _rowHeaderMsaTable.setRowSelectionInterval(i, i);
          _scroll.getVerticalScrollBar().setValue(i * dy);
          break;
        }
      }
    }
    _bLockSelection = false;
  }

  /**
   * Clear the content of the viewer.
   */
  protected void resetViewer() {
    _msaTable.setLogoCellRenderer(new LogoCellRenderer(0, 0));
    _msaTable.setModel(new MSATableModel());
    _consensusSlider.setEnabled(false);
    _positionner.setEnabled(false);
    _rowHeaderMsaTable.setModel(new RowHeaderMSATableModel());
    _consensusRenderer.setRefSequence(null);
    activateOptionCommands(false);
    _scroll.setVisible(false);
    _msaMsgLbl.setText("");
    _msaMsgLbl.setVisible(false);
  }

  /**
   * Implementation of BlastIterationListListener interface. This implementation
   * is intended to listen to notifications from BlastSummary.
   */
  public void iterationChanged(BlastIterationListEvent e) {
    _curIter = (BlastIteration) e.getBlastIteration();
    _msaComputed = false;
    if (_displayed)
      setupDataForViewer();
  }

  /**
   * Prepare data for the viewer.
   */
  protected void computeData() {
    SROutput bo;
    MSATableModel msaTM;
    DAlphabet alph;
    PositionSpecificMatrix matrix;
    DSequence query;
    int size, rows;

    if (_msaComputed)
      return;

    _msaComputed = true;

    query = _curIter.getEntry().getQuery();
    if (query != null) {
      size = query.size();
    } else {
      Object qLength = _curIter.getEntry().getResult().getRequestInfo()
          .getValue(SRRequestInfo.QUERY_LENGTH_DESCRIPTOR_KEY);
      if (qLength != null) {
        size = Integer.valueOf(qLength.toString());
      } else {
        size = 0;
      }
    }
    if (size > QUERY_SIZE_LIMIT) {
      resetViewer();
      String msg = BVMessages.getString("BlastHitMSA.compute.err1");
      _msaMsgLbl.setText(MessageFormat.format(msg,
          new Object[] { QUERY_SIZE_LIMIT }));
      _msaMsgLbl.setVisible(true);
      return;
    }
    _msaMsgLbl.setText("");
    _msaMsgLbl.setVisible(false);
    msaTM = new MSATableModel(_curIter, null);
    _consensusSlider.updateModel(1, msaTM.getSeqAlign() != null ? msaTM
        .getSeqAlign().rows() : 1, 1);
    _positionner.setMaxPosition(msaTM.getSeqAlign() != null ? msaTM
        .getSeqAlign().columns() : 0);
    _rulerViewer.setRulerModel(new DRulerModelImplem(1, msaTM.getSeqAlign()
        .columns(), 1));

    _msaTable.setModel(msaTM);
    _rowHeaderMsaTable.setModel(new RowHeaderMSATableModel(_curIter, msaTM
        .hasConsensus(), msaTM.hasRefSeq()));
    bo = _curIter.getEntry().getResult();
    if (bo.getBlastType() == SROutput.BLASTP
        || bo.getBlastType() == SROutput.SCANPS
        || bo.getBlastType() == SROutput.PSIBLAST
        || bo.getBlastType() == SROutput.BLASTN
        || bo.getBlastType() == SROutput.TBLASTN) {
      _consensusRenderer.setRefSequence(_curIter.getEntry().getQuery());
    } else {
      // to allow the display of translated query within the consensus
      // header, we will need to translate the query on the fly using
      // the genetic code employed during the Blast search!!!
      // this work has to be done... one day!
      _consensusRenderer.setRefSequence(null);
    }
    alph = msaTM.getSeqAlign().getSequence(0).getSequence().getAlphabet();
    if (alph != null) {
      size = alph.size();
    } else {
      size = 0;
    }
    matrix = msaTM.getPositionSpecificMatrix();
    if (matrix != null) {
      rows = matrix.getMaxCounter();
    } else {
      rows = 0;
    }
    LogoCellRenderer logoCellRenderer = new LogoCellRenderer(size, rows);
    logoCellRenderer.setType(LogoPanel.Type.LogoLetter);
    _msaTable.setRowHeight(0, _logoCellHeight);
    _msaTable.setLogoCellRenderer(logoCellRenderer);
    _rowHeaderMsaTable.setRowHeight(0, _logoCellHeight);
    activateOptionCommands(true);
    _consensusSlider.setEnabled(true);
    _positionner.setEnabled(true);

    _scroll.setVisible(true);
    _scroll.getVerticalScrollBar().setValue(0);
    initColumnSize();
  }

  private void setupDataForViewer() {
    if (_curIter == null || _curIter.getIteration().countHit() == 0) {
      resetViewer();
      return;
    }
    EZEnvironment.setWaitCursor();
    try {
      computeData();
    } catch (Exception ex) {// thrown by computeMSA() when no sequences are
                            // available
      EZLogger.warn(ex.toString());
      resetViewer();
    }
    EZEnvironment.setDefaultCursor();
  }

  /**
   * Returns the component allowing a suer to go at a particular position within
   * the MSA.
   */
  public GotoPositionInHorzScroll getPositionner() {
    return _positionner;
  }

  /**
   * Set table cell width according to the Font.
   */
  private void intCellWidth() {
    FontMetrics fm;

    fm = _msaTable.getFontMetrics(_msaTable.getFont());
    _cellW = fm.getHeight();

  }

  /**
   * Initializes columns size for MSA table to default values.
   */
  private void initColumnSize() {
    TableColumnModel tcm;
    TableColumn tc;
    int i, size;

    intCellWidth();
    tcm = _msaTable.getColumnModel();
    size = tcm.getColumnCount();
    for (i = 0; i < size; i++) {
      tc = tcm.getColumn(i);
      tc.setPreferredWidth(_cellW);
      tc.setMinWidth(_cellW);
      tc.setMaxWidth(_cellW);
    }
  }

  /**
   * Initializes columns size for RowHeader MSA table to default values.
   */
  private void initColumnSizeRowHeaderTable() {
    FontMetrics fm;
    TableColumnModel tcm;
    TableColumn tc;
    String header;
    int i, size;

    fm = _rowHeaderMsaTable.getFontMetrics(_rowHeaderMsaTable.getFont());
    tcm = _rowHeaderMsaTable.getColumnModel();
    size = tcm.getColumnCount();
    for (i = 0; i < size; i++) {
      tc = tcm.getColumn(i);
      header = tc.getHeaderValue().toString();
      switch (i) {
        case RowHeaderMSATableModel.HIT:
          tc.setPreferredWidth(2 * fm.stringWidth(header));
          break;
        case RowHeaderMSATableModel.HSP:
          tc.setPreferredWidth(fm.stringWidth(header) + 10);
      }
    }
  }

  protected void activateOptionCommands(boolean val) {
    _exporter.setEnabled(val);
    _imager.setEnabled(val);
    _cpBoardAction.setEnabled(val);
  }

  /**
   * Implementation of AnalyseModule interface.
   */
  public void modHidden() {
    _displayed = false;
  }

  /**
   * Implementation of AnalyseModule interface.
   */
  public void modDisplayed() {
    _displayed = true;
    setupDataForViewer();
  }

  protected void addMSAToolbarCommand(JToolBar tBar) {

  }

  private JToolBar getToolbar() {
    ImageIcon icon;
    JToolBar tBar;
    JButton btn;

    tBar = new JToolBar();
    tBar.setFloatable(false);

    addMSAToolbarCommand(tBar);

    // Save MSF/GCG file
    icon = EZEnvironment.getImageIcon("save.png");
    if (icon != null) {
      _exporter = new ExportMSAAction("", icon);
    } else {
      _exporter = new ExportMSAAction(
          BVMessages.getString("BlastHitMSA.save.msf.btn"));
    }
    _exporter.setMsaTable(_msaTable);
    _exporter.setRowHeaderMSATable(_rowHeaderMsaTable);
    btn = tBar.add(_exporter);
    btn.setToolTipText(BVMessages.getString("BlastHitMSA.save.msf.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("BlastHitMSA.save.msf.btn"));
    // Copy to Clipboard (Fasta)
    icon = EZEnvironment.getImageIcon("copy2clip.png");
    if (icon != null) {
      _cpBoardAction = new CopySelectionToClipBoardAction("", icon);
    } else {
      _cpBoardAction = new CopySelectionToClipBoardAction(
          BVMessages.getString("BlastHitMSA.copy.msf.btn"));
    }
    _cpBoardAction.setMsaTable(_msaTable);
    _cpBoardAction.setRowHeaderMSATable(_rowHeaderMsaTable);
    btn = tBar.add(_cpBoardAction);
    btn.setToolTipText("Copy");
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("BlastHitMSA.copy.msf.btn"));
    // save image table
    icon = EZEnvironment.getImageIcon("imager.png");
    if (icon != null) {
      _imager = new ImageManagerAction("", icon);
    } else {
      _imager = new ImageManagerAction(
          BVMessages.getString("ImageManagerAction.save.btn"));
    }

    _imager.setComponents(new JComponent[] { _msaTable, _rulerViewer,
        _rowHeaderMsaTable });
    btn = tBar.add(_imager);
    btn.setToolTipText(BVMessages.getString("ImageManagerAction.save.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("ImageManagerAction.save.btn"));
    return tBar;
  }

  protected void setContextMenu() {
    ArrayList<ContextMenuElement> actions;
    CopySelectionToClipBoardAction act;
    BasicSelectTableAction act2;

    actions = new ArrayList<ContextMenuElement>();

    act2 = new BasicSelectTableAction("Clear selection",
        BasicSelectTableAction.SelectType.CLEAR);
    act2.setTable(_msaTable);
    actions.add(new ContextMenuElement(act2));
    actions.add(null);

    act = new CopySelectionToClipBoardAction(
        BVMessages.getString("BlastHitMSA.copy.msf.mnu1"));
    act.forceCopyEntireMSA(true);
    act.setMsaTable(_msaTable);
    act.setRowHeaderMSATable(_rowHeaderMsaTable);
    actions.add(new ContextMenuElement(act));

    act = new CopySelectionToClipBoardAction(
        BVMessages.getString("BlastHitMSA.copy.msf.mnu2"));
    act.setMsaTable(_msaTable);
    act.setRowHeaderMSATable(_rowHeaderMsaTable);
    act.forceCopyEntireMSA(false);
    actions.add(new ContextMenuElement(act));

    _contextMnu = new ContextMenuManager(_msaTable, actions);
  }

  private class TableMouseListener extends MouseAdapter {
    public void mouseReleased(MouseEvent e) {
      if (SwingUtilities.isRightMouseButton(e) && _contextMnu != null) {
        _contextMnu.showContextMenu(e.getX(), e.getY());
      }
    }
  }

  /**
   * This class is used to automatically resizes the table when its parent
   * component is itself resizes.
   */
  private class TableComponentAdapter extends ComponentAdapter {
    public void componentResized(ComponentEvent e) {
      initColumnSize();
    }
  }

  private class BlastHitTableSelectionListener implements ListSelectionListener {
    // Listen to the JTable embedded within BlastHitList component
    public void valueChanged(ListSelectionEvent e) {
      ListSelectionModel lsm;
      RowHeaderEntry entry = null;
      ArrayList<BlastHitHSP> hits = null;

      if (e.getValueIsAdjusting())
        return;
      if (_bLockSelection) {
        return;
      }
      lsm = (ListSelectionModel) e.getSource();

      if (!lsm.isSelectionEmpty()
          && (lsm.getMinSelectionIndex() == lsm.getMaxSelectionIndex())) {
        entry = (RowHeaderEntry) _rowHeaderMsaTable.getModel().getValueAt(
            lsm.getMinSelectionIndex(), -1);
        hits = new ArrayList<BlastHitHSP>();
        hits.add(entry.getBhh());
      }

      _updateSpport.fireHitChange(new BlastHitListEvent(MSAPanel.this, hits,
          BlastHitListEvent.HIT_CHANGED));
    }
  }

}
