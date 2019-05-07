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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchresult.SRIteration;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.SRRequestInfo;
import bzh.plealog.bioinfo.data.searchjob.InMemoryQuery;
import bzh.plealog.bioinfo.data.searchresult.SRUtils;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.blast.core.BlastEntry;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHspImplem;
import bzh.plealog.bioinfo.ui.blast.core.QueryBaseUI;
import bzh.plealog.bioinfo.ui.blast.hittable.BlastHitTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.bioinfo.ui.resources.SVMessages;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * This is the BlastViewer Main Module.
 * 
 * It wraps within a single component the various
 * elements required to displayed Blast data: a BlastNavigator, a Blast Hit Table,
 * the pairwise sequence alignment viewer, etc.
 * 
 * @author Patrick G. Durand
 */
public class BlastSummaryViewerPanel extends JPanel {

  private static final long serialVersionUID = -2405089127382200483L;

  protected SummaryTable _summaryTable;
  protected BlastHitTable _hitListPane;
  private BlastEntry _entry;
  
  protected static final String HITPANEL_HEADER = SVMessages.getString("BlastViewerPanel.0");
  protected static final String HITPANEL_LIST = SVMessages.getString("BlastViewerPanel.1");
  protected static final String HITPANEL_GRAPHIC = SVMessages.getString("BlastViewerPanel.2");

  /**
   * Default constructor.
   */
  public BlastSummaryViewerPanel() {
    super();
    createGUI();
  }

  /**
   * Set the data to display in this viewer.
   */
  public void setContent(BlastEntry entry) {
    _entry = entry;
    //Prepare a View from the Model
    InMemoryQuery query;
    query = new InMemoryQuery();
    List<SROutput> results = SRUtils.splitMultiResult(entry.getResult());
    for(SROutput sro : results) {
      query.addResult(sro);
    }
    query.setDatabankName(entry.getDbName());
    query.setEngineSysName(entry.getBlastClientName());
    query.setJobName(entry.getName());
    // a Blast result loaded from a file is always OK
    query.setStatus(QueryBase.OK);
    // query not provided in blastFile
    query.setQueyPath("n/a");
    // not appropriate here
    query.setRID("n/a");
    QueryBaseUI qBaseUI;
    qBaseUI = new QueryBaseUI(query);
    SummaryTableModel resultTableModel = new SummaryTableModel();
    resultTableModel.setQuery(qBaseUI);
    //set the data model and add the link between summary viewer and detail viewer
    _summaryTable.setModel(resultTableModel);
    _summaryTable.setRowSelectionInterval(0, 0);
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
   * Set the data to display in this viewer.
   */
  public void setContent(SROutput so, String soPath) {
    setContent(prepareEntry(so, soPath));
  }

  /**
   * Set the data to display in this viewer.
   */
  public void setContent(SROutput so) {
    setContent(prepareEntry(so, null));
  }

  /**
   * Return the result currently selected in this ViewerPanel.
   */
  public SROutput getSelectedResult() {
    return null;
  }

  /**
   * Wraps a SROutput object into a BlastEntry.
   */
  private BlastEntry prepareEntry(SROutput bo, String soPath) {
    String val;
    int pos;

    // analyze SROutput object (i.e. a Blast result) to get:
    // program name, query name and databank name
    SRRequestInfo bri = bo.getRequestInfo();
    Object obj = bri.getValue(SRRequestInfo.PRGM_VERSION_DESCRIPTOR_KEY);
    if (obj != null) {
      val = obj.toString();
      if ((pos = val.indexOf('[')) > 0) {
        val = val.substring(0, pos - 1);
      } else {
        val = obj.toString();
      }
    } else {
      val = null;
    }
    String program = val != null ? val : "?";
    obj = bri.getValue(SRRequestInfo.DATABASE_DESCRIPTOR_KEY);
    String dbname = obj != null ? obj.toString() : "?";
    obj = bri.getValue(SRRequestInfo.QUERY_DEF_DESCRIPTOR_KEY);
    String queryName = obj != null ? obj.toString() : "?";

    return new BlastEntry(program, queryName, soPath, bo, null, dbname, false);
  }

  /**
   * Prepare the viewer.
   */
  private void createGUI() {
    _hitListPane = ConfigManager.getHitTableFactory().createViewer();
    JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    jsp.setTopComponent(prepareSummaryTable());
    jsp.setRightComponent(_hitListPane);
    jsp.setOneTouchExpandable(true);
    jsp.setResizeWeight(0.75); 
    this.setLayout(new BorderLayout());
    this.add(jsp, BorderLayout.CENTER);
    this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
        return;
      }
      int row = _summaryTable.getSelectedRow();
      if (row<0) {
        _hitListPane.resetDataModel();
        return;
      }
      //get selected SRoutput and display it within a BlastHitTable
      SROutput sro = (SROutput) _summaryTable.getValueAt(row, SummaryTableModel.RESULT_DATA_COL);
      SRIteration iter = sro.getIteration(0);
      if (iter == null || iter.countHit() == 0) {
        _hitListPane.resetDataModel();
      } else {
          int i, size;
          BlastHitHspImplem[] bhh;
          size = iter.countHit();
          bhh = new BlastHitHspImplem[size];
          for (i = 0; i < size; i++) {
            bhh[i] = new BlastHitHspImplem(
                iter.getHit(i), 
                _entry.getBlastClientName(), 
                1, 
                iter.getIterationQueryLength(), 
                _entry.getResult().getBlastType());
          }
        _hitListPane.setDataModel(bhh);
      }
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
}
