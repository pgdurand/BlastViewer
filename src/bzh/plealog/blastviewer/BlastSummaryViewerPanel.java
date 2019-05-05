/* Copyright (C) 2003-2016 Patrick G. Durand
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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.SRRequestInfo;
import bzh.plealog.bioinfo.data.searchjob.InMemoryQuery;
import bzh.plealog.bioinfo.data.searchresult.SRUtils;
import bzh.plealog.bioinfo.ui.blast.core.BlastEntry;
import bzh.plealog.bioinfo.ui.blast.core.QueryBaseUI;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.bioinfo.ui.resources.SVMessages;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;

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
    _summaryTable.setModel(resultTableModel);
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

  private void createGUI() {
    this.setLayout(new BorderLayout());
    this.add(prepareSummaryTable(), BorderLayout.CENTER);
    this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

}
