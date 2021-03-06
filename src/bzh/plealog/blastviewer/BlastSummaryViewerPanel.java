/* Copyright (C) 2003-2019 Patrick G. Durand
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
package bzh.plealog.blastviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchjob.SJFileSummary;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput.FEATURES_CONTAINER;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.blast.core.BlastEntry;
import bzh.plealog.bioinfo.ui.blast.core.BlastIteration;
import bzh.plealog.bioinfo.ui.blast.core.QueryBaseUI;
import bzh.plealog.bioinfo.ui.blast.hittable.BlastHitTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.bioinfo.ui.util.ProgressTinyDialog;
import bzh.plealog.bioinfo.ui.util.Selection;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;
import bzh.plealog.bioinfo.ui.util.TableSearcherComponent;
import bzh.plealog.bioinfo.ui.util.TableSearcherComponentAPI;
import bzh.plealog.bioinfo.ui.util.TableSearcherComponentAction;
import bzh.plealog.blastviewer.actions.summary.ChooseClassificationAction;
import bzh.plealog.blastviewer.actions.summary.ViewQueryIprScanPredictionsAction;
import bzh.plealog.blastviewer.actions.summary.GlobalFilterAction;
import bzh.plealog.blastviewer.actions.summary.GlobalSaveAction;
import bzh.plealog.blastviewer.actions.summary.ImportIprScanDomainsAction;
import bzh.plealog.blastviewer.actions.summary.OpenBasicViewerAction;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * This is the BlastViewer Main Module used to display the summary of a multi-query BLAST results.
 * 
 * @author Patrick G. Durand
 */
public class BlastSummaryViewerPanel extends JPanel {

  private static final long serialVersionUID = -2405089127382200483L;

  protected SummaryTable _summaryTable;
  protected BlastHitTable _hitListPane;
  private BlastEntry _entry;
  private JLabel _resultStatusTxt;
  private JRadioButton          _rbAllQueries;
  private JRadioButton          _rbMatchQueries;
  private JRadioButton          _rbNoMatchQueries;
  private TableSearcherComponent _searcher;
  private GlobalFilterAction _filterAction;
  private GlobalSaveAction _saveAction;
  private ChooseClassificationAction _classifSelectAction;
  private OpenBasicViewerAction _openBasicViewerAction;
  private ViewQueryIprScanPredictionsAction _viewQueryIPRAction;
  private MyImportIprScanDomainsAction _importIprScan;
  private BlastSummaryViewerController _bvController;
  private JButton _startSearchBtn;
  
  /**
   * Default constructor.
   */
  public BlastSummaryViewerPanel(BlastSummaryViewerController bvController) {
    super();
    _bvController = bvController;
    createGUI();
  }

  public String getTitle() {
    return BVMessages.getString("BlastSummaryViewerPanel.title");
  }
  /**
   * Set the data to display in this viewer.
   */
  public void setContent(QueryBase query, BlastEntry entry) {
    _entry = entry;
    _filterAction.setTable(_summaryTable);
    _saveAction.setTable(_summaryTable);
    _classifSelectAction.setTable(_summaryTable);
    _importIprScan.setTable(_summaryTable);
    _importIprScan.SetQuery(query);
    _openBasicViewerAction.setTable(_summaryTable);
    _viewQueryIPRAction.setTable(_summaryTable);

    _openBasicViewerAction.setEnabled(false);
    _viewQueryIPRAction.setEnabled(false);

    QueryBaseUI qBaseUI;
    qBaseUI = new QueryBaseUI(query);
    SummaryTableModel resultTableModel = new SummaryTableModel();
    resultTableModel.setQuery(qBaseUI);
    //set the data model and add the link between summary viewer and detail viewer
    _summaryTable.setModel(resultTableModel);
    //_summaryTable.setRowSelectionInterval(0, 0);
    updateViewTypeRows();
    
    updateActions(entry.getResult());
  }

  /**
   * Prepare a SummaryTable component.
   * 
   * @param blastQuery the query to display
   */
  private JComponent prepareSummaryTable() {
    JPanel pnl;
    SummaryTableModel resultTableModel;
    SummaryTable resultTable;
    JScrollPane scrollPaneRT;
    TableColumnManager tcm;
    
    pnl = new JPanel(new BorderLayout());

    // Result Table
    resultTableModel = new SummaryTableModel();
    resultTable = new SummaryTable(resultTableModel);
    resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    resultTable.getTableHeader().setReorderingAllowed(false);
    resultTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    resultTable.setColumnSelectionAllowed(false);
    resultTable.setRowSelectionAllowed(true);
    resultTable.setGridColor(Color.LIGHT_GRAY);

    // Top Scroll Pane
    scrollPaneRT = new JScrollPane(resultTable);
    scrollPaneRT.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPaneRT.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    tcm = new TableColumnManager(resultTable, resultTableModel.getReferenceColumnHeaders());
    scrollPaneRT.setCorner(JScrollPane.UPPER_RIGHT_CORNER, tcm.getInvoker());

    pnl.add(scrollPaneRT, BorderLayout.CENTER);
    
    _summaryTable = resultTable;
    
    //enable the display of a single SROutput within dedicated HitTable
    _summaryTable.getSelectionModel().addListSelectionListener(new MyListSelectionListener());
    //open a separate Full Viewer in response of a double click
    _summaryTable.addMouseListener(new MyMouseAdapter());
    return pnl;
  }

  /**
   * Show list of hits given a selection type.
   * 
   * @param selType type of selection event
   */
  public void showSummary(Selection.SelectType selType) {
    switch(selType) {
    case ALL:
      _rbAllQueries.setSelected(true);
      break;
    case WITHOUT_HITS:
      _rbNoMatchQueries.setSelected(true);
      break;
    case WITH_HITS:
      _rbMatchQueries.doClick();
      break;
    default:
      break;
    }
    _summaryTable.updateRowHeights();
  }
  
  /**
   * Return the result currently selected in this ViewerPanel.
   */
  public SROutput getSelectedResult() {
    int row = _summaryTable.getSelectedRow();
    if (row<0) {
      return null;
    }
    else {
      return (SROutput) _summaryTable.getValueAt(row, SummaryTableModel.RESULT_DATA_COL);
    }
  }

  /**
   * Filter out view using particular classifications.
   * 
   * @param classifs list of classification terms. Such terms come from
   * AnnotationDataModelConstants.ANNOTATION_CATEGORY ; use getType().
   */
  public void applyClassificationFilter(List<String> classifs) {
    _summaryTable.setClassificationsToView(classifs);
  }
  
  /**
   * Filter out view using a regular expression. It applies on table cell
   * contents.
   * 
   * @param regexp a data filter
   */
  public void applyRegExpFilter(String regexp) {
    _searcher.setSearchText(regexp);
    _startSearchBtn.doClick();
  }
  
  /**
   * Update viewer actions according to data.
   */
  private void updateActions(SROutput bo) {
    if (bo.checkFeatures().equals(FEATURES_CONTAINER.none) && 
        bo.checkQueryFeatures().equals(FEATURES_CONTAINER.none)) {
      _classifSelectAction.setEnabled(false);
    }
  }
  
  /**
   * Prepare the viewer.
   */
  private void createGUI() {
    _hitListPane = ConfigManager.getHitTableFactory().createViewer();
    
    JComponent cmp = prepareSummaryTable();
    
    _resultStatusTxt = new JLabel();
    _resultStatusTxt.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    
    JPanel viewTypePanel;

    _rbAllQueries = new JRadioButton("All queries");
    _rbAllQueries.addActionListener(new ViewAllQueriesRadioBtnListener());
    _rbMatchQueries = new JRadioButton("Matching queries");
    _rbMatchQueries.addActionListener(new ViewQueriesWithHitsRadioBtnListener());
    _rbNoMatchQueries = new JRadioButton("Not matching queries");
    _rbNoMatchQueries.addActionListener(new ViewQueriesWithNoHitsRadioBtnListener());

    viewTypePanel = new JPanel();

    viewTypePanel.add(_rbAllQueries);
    viewTypePanel.add(_rbMatchQueries);
    viewTypePanel.add(_rbNoMatchQueries);

    ButtonGroup group = new ButtonGroup();
    group.add(_rbAllQueries);
    group.add(_rbMatchQueries);
    group.add(_rbNoMatchQueries);

    _rbAllQueries.setSelected(true);
    
    JPanel pnlSearcher = new JPanel(new BorderLayout());
    _searcher = new TableSearcherComponent(_summaryTable);
    _startSearchBtn = _searcher.addUserAction(EZEnvironment.getImageIcon("findNew_s.png"), new SearchNextActionListener());

    pnlSearcher.add(viewTypePanel, BorderLayout.NORTH);
    pnlSearcher.add(_searcher, BorderLayout.SOUTH);
    
    JPanel navPanel = new JPanel(new BorderLayout());
    navPanel.add(pnlSearcher, BorderLayout.WEST);
    navPanel.add(getToolbar(), BorderLayout.EAST);
    
    JPanel summaryPanel = new JPanel(new BorderLayout());
    summaryPanel.add(_resultStatusTxt, BorderLayout.NORTH);
    summaryPanel.add(cmp, BorderLayout.CENTER);
    summaryPanel.add(navPanel, BorderLayout.SOUTH);
    
    
    JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    jsp.setTopComponent(summaryPanel);
    jsp.setRightComponent(_hitListPane);
    jsp.setOneTouchExpandable(true);
    jsp.setResizeWeight(0.75);

    this.setLayout(new BorderLayout());
    this.add(_resultStatusTxt, BorderLayout.NORTH);
    this.add(jsp, BorderLayout.CENTER);
    this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  private void updateViewTypeRows() {
    int rows = _summaryTable.getRowCount();

    _resultStatusTxt.setText(rows + (rows > 1 ? " rows" : " row"));
    EZEnvironment.setDefaultCursor();
  }

  private JToolBar getToolbar() {
    JToolBar tBar;
    ImageIcon icon;
    JButton btn;

    tBar = new JToolBar();
    tBar.setFloatable(false);

    icon = EZEnvironment.getImageIcon("open.png");
    if (icon != null) {
      _openBasicViewerAction = new OpenBasicViewerAction("", icon);
    } else {
      _openBasicViewerAction = new OpenBasicViewerAction(BVMessages.getString("BlastHitList.open.btn"));
    }
    _openBasicViewerAction.setEnabled(true);
    btn = tBar.add(_openBasicViewerAction);
    btn.setToolTipText(BVMessages.getString("BlastHitList.open.tip"));
    btn.setText(BVMessages.getString("BlastHitList.open.btn"));

    icon = EZEnvironment.getImageIcon("filterRes.png");
    if (icon != null) {
      _filterAction = new GlobalFilterAction("", icon);
    } else {
      _filterAction = new GlobalFilterAction(BVMessages.getString("BlastHitList.filter.btn"));
    }
    _filterAction.setEnabled(true);
    btn = tBar.add(_filterAction);
    btn.setToolTipText(BVMessages.getString("BlastHitList.filter.tip"));
    btn.setText(BVMessages.getString("BlastHitList.filter.btn"));

    icon = EZEnvironment.getImageIcon("save.png");
    if (icon != null) {
      _saveAction = new GlobalSaveAction("", icon);
    } else {
      _saveAction = new GlobalSaveAction(BVMessages.getString("BlastHitList.save.btn"));
    }
    _saveAction.setEnabled(true);
    btn = tBar.add(_saveAction);
    btn.setToolTipText(BVMessages.getString("BlastHitList.save.tip"));
    btn.setText(BVMessages.getString("BlastHitList.save.btn"));
    
    tBar.addSeparator();

    icon = EZEnvironment.getImageIcon("import_bco_24_24.png");
    if (icon != null) {
      _importIprScan = new MyImportIprScanDomainsAction("", icon);
    } else {
      _importIprScan = new MyImportIprScanDomainsAction(BVMessages.getString("BlastHitList.iprscan.btn"));
    }
    _importIprScan.setEnabled(true);
    _importIprScan.setBlastSummaryViewerController(_bvController);
    btn = tBar.add(_importIprScan);
    btn.setToolTipText(BVMessages.getString("BlastHitList.iprscan.tip"));
    btn.setText(BVMessages.getString("BlastHitList.iprscan.btn"));
    
    icon = EZEnvironment.getImageIcon("view_features.png");
    if (icon != null) {
      _viewQueryIPRAction = new ViewQueryIprScanPredictionsAction("", icon);
    } else {
      _viewQueryIPRAction = new ViewQueryIprScanPredictionsAction(BVMessages.getString("BlastHitList.edit.btn"));
    }
    _viewQueryIPRAction.setEnabled(true);
    btn = tBar.add(_viewQueryIPRAction);
    btn.setToolTipText(BVMessages.getString("BlastHitList.edit.tip"));
    btn.setText(BVMessages.getString("BlastHitList.edit.btn"));
    
    icon = EZEnvironment.getImageIcon("meta_path_24_24.png");
    if (icon != null) {
      _classifSelectAction = new ChooseClassificationAction("", icon);
    } else {
      _classifSelectAction = new ChooseClassificationAction(BVMessages.getString("BlastHitList.classif.btn"));
    }
    _classifSelectAction.setEnabled(true);
    btn = tBar.add(_classifSelectAction);
    btn.setToolTipText(BVMessages.getString("BlastHitList.classif.tip"));
    btn.setText(BVMessages.getString("BlastHitList.classif.btn"));

    return tBar;
  }
  /**
   * Utility class to handle the display of all queries within the Result Table.
   */
  private class ViewAllQueriesRadioBtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (_summaryTable.getRowCount() > 50000)
        EZEnvironment.setWaitCursor();
      _summaryTable.setViewType(SummaryTableModel.VIEW_TYPE.ALL);
      updateViewTypeRows();
    }
  }

  /**
   * Utility class to handle the display of all queries within the Result Table.
   */
  private class ViewQueriesWithHitsRadioBtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (_summaryTable.getRowCount() > 50000)
        EZEnvironment.setWaitCursor();
      _summaryTable.setViewType(SummaryTableModel.VIEW_TYPE.HITS_ONLY);
      updateViewTypeRows();
    }
  }

  /**
   * Utility class to handle the display of all queries within the Result Table.
   */
  private class ViewQueriesWithNoHitsRadioBtnListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (_summaryTable.getRowCount() > 50000)
        EZEnvironment.setWaitCursor();
      _summaryTable.setViewType(SummaryTableModel.VIEW_TYPE.NO_HITS_ONLY);
      updateViewTypeRows();
    }
  }

  /**
   * Listen to mouse selection on SummaryViewer. In response, displays details
   * of selected SROutput into a BlastHitTable viewer.
   */
  private class MyListSelectionListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      //something to do?
      if (event.getValueIsAdjusting())
        return;
      if (_summaryTable.getSelectedRowCount()!=1) {
        _hitListPane.resetDataModel();
        _openBasicViewerAction.setEnabled(false);
        _viewQueryIPRAction.setEnabled(false);
        return;
      }
      _openBasicViewerAction.setEnabled(true);
      _viewQueryIPRAction.setEnabled(true);

      int row = _summaryTable.getSelectedRow();
      if (row<0) {
        _hitListPane.resetDataModel();
        return;
      }
      row = _summaryTable.convertSelectedRowToSelectedSummary(row);
      BlastIteration blastIter = new BlastIteration(_entry, row);
      _hitListPane.resetDataModel();
      _hitListPane.setDataModel(blastIter);
        _hitListPane.repaint();
    }
  }
  
  /**
   * Listen to mouse double click on SummaryViewer. In response, displays details
   * of selected SROutput into a full feature BlastHitTable JInternalFrame.
   */
  private class MyMouseAdapter extends MouseAdapter {
    public void mousePressed(MouseEvent mouseEvent) {
      JTable table =(JTable) mouseEvent.getSource();
      if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
        //get selected SROuput
        SROutput sro = (SROutput) _summaryTable.getValueAt(
            table.getSelectedRow(), 
            SummaryTableModel.RESULT_DATA_COL); 
        if (sro.isEmpty()) {
          return;
        }
        
        //prepare a copy to avoid altering original data in the coming viewer
        SROutput sro_copy = sro.clone(false);
        //such a result only contains a single SRIteration on which we MUST
        //reset ordering number to "1"
        sro_copy.getIteration(0).setIterationIterNum(1);
        //start viewer
        BlastViewerOpener.displayInternalFrame(
            BlastViewerOpener.prepareViewer(sro_copy),
            sro.getBlastTypeStr(), null);
      }
    }
  }
  protected class SelectThread extends Thread {
    private String            searchText;
    private TableSearcherComponentAPI caller;
    private ProgressTinyDialog      monitor;

    public SelectThread(String searchText, TableSearcherComponentAPI caller, ProgressTinyDialog monitor) {
      this.searchText = searchText;
      this.monitor = monitor;
      this.caller = caller;
    }

    public void run() {
      Enumeration<SJFileSummary> summaries;
      SummaryTableModel tModel;
      SJFileSummary bfs;
      QueryBase query;
      Pattern pattern;
      Matcher matcher;
      String value;
      ListSelectionModel lsm;
      ArrayList<Integer> indices;
      int[] indicesArray;
      int idx = 0, cols, i = 0, idx2, nFound = 0, firstIdx = -1;

      query = (QueryBase) _summaryTable.getValueAt(0, SummaryTableModel.QUERY_DATA_COL);
      if (query == null)
        return;

      summaries = query.getSummaries();
      tModel = (SummaryTableModel) _summaryTable.getModel();
      lsm = _summaryTable.getSelectionModel();
      cols = tModel.getColumnCount();
      monitor.setMaxSteps(tModel.getRowCount());
      pattern = Pattern.compile(searchText.toLowerCase());
      lsm.setValueIsAdjusting(true);
      lsm.clearSelection();
      indices = new ArrayList<Integer>();
      while (summaries.hasMoreElements()) {
        bfs = summaries.nextElement();

        if (bfs == null)
          break;
        for (i = 0; i < cols; i++) {
          value = tModel.getValueItem(idx, tModel.getColumnId(i), bfs, null, query).toString();
          matcher = pattern.matcher(value.toLowerCase());
          if (matcher.find()) {
            idx2 = _summaryTable.convertSummaryIdxToTableRow(idx);
            if (idx2 != -1) {
              lsm.addSelectionInterval(idx2, idx2);
              if (firstIdx == -1) {
                firstIdx = idx2;
              }
              indices.add(idx2);
              nFound++;
              break;
            }
          }
        }
        monitor.addToProgress(1);
        if (monitor.stopProcessing())
          break;
        idx++;

      }
      monitor.dispose();
      lsm.setValueIsAdjusting(false);
      _resultStatusTxt.setText(nFound + " row" + (nFound != 1 ? "(s)" : "") + " selected");
      if (firstIdx != -1) {
        _summaryTable.scrollRectToVisible(_summaryTable.getCellRect(firstIdx, 0, false));
        _summaryTable.repaint();
        indicesArray = new int[nFound];
        i = 0;
        for (Integer indice : indices) {
          indicesArray[i] = indice;
          i++;
        }
        indices.clear();
        indices = null;
        caller.setPrecomputedIndex(indicesArray);
      }
    }
  }

  private class MainSelectThread extends Thread {
    private String            searchText;
    private TableSearcherComponentAPI caller;

    public MainSelectThread(String searchText, TableSearcherComponentAPI caller) {
      this.searchText = searchText;
      this.caller = caller;
    }

    public void run() {
      ProgressTinyDialog monitor = new ProgressTinyDialog("Searching for " + searchText + "...", 0, true, true, false);
      new SelectThread(searchText, caller, monitor).start();
      monitor.setVisible(true);
    }
  }

  private class SearchNextActionListener extends AbstractAction implements TableSearcherComponentAction {
    private static final long     serialVersionUID  = -6543582685153860631L;
    private String            searchText;
    private TableSearcherComponentAPI caller;

    @Override
    public void actionPerformed(ActionEvent arg0) {
      if (searchText == null)
        return;
      _hitListPane.resetDataModel();
      SwingUtilities.invokeLater(new MainSelectThread(searchText, caller));
    }

    @Override
    public void setSearchText(String text) {
      searchText = text;
    }

    @Override
    public void setTableSearcherComponentActionGateway(TableSearcherComponentAPI caller) {
      this.caller = caller;
    }
  }

  @SuppressWarnings("serial")
  private class MyImportIprScanDomainsAction extends ImportIprScanDomainsAction{
    private SummaryTable _table;
    public MyImportIprScanDomainsAction(String name) {
      super(name);
      // TODO Auto-generated constructor stub
    }
    public MyImportIprScanDomainsAction(String name, Icon icon) {
      super(name, icon);
    }
    /**
     * Set data object.
     */
    public void setTable(SummaryTable table) {
      _table = table;
    }
    public void updateUI() {
      _table.updateRowHeights();
    }
  }
}
