/* Copyright (C) 2020 Patrick G. Durand
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
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput.FEATURES_CONTAINER;
import bzh.plealog.bioinfo.api.data.searchresult.SRRequestInfo;
import bzh.plealog.bioinfo.data.searchjob.InMemoryQuery;
import bzh.plealog.bioinfo.data.searchresult.SRUtils;
import bzh.plealog.bioinfo.ui.blast.core.BlastEntry;
import bzh.plealog.blastviewer.summary.QueryOverviewPanel;
import bzh.plealog.blastviewer.summary.SRCOverviewPanel;

/**
 * BlastQuery viewer.
 */
public class BlastQueryViewer extends JPanel {
  private static final long serialVersionUID = 646119999183853782L;
  private QueryOverviewPanel _completeSummary;
  private BlastSummaryViewerPanel _summary;
  private SRCOverviewPanel _bcoOverview;

  private BlastSummaryViewerController _bvController;
  
  /**
   * Default constructor.
   */
  public BlastQueryViewer() {
    super();
    createGUI();
  }
 
  /**
   * Set the content of this viewer.
   * 
   * @param so usually it is a multi result BLAST data object
   */
  public void setContent(SROutput so) {
    BlastEntry entry = prepareEntry(so);
    QueryBase query = prepareQuery(entry);
    
    //Complete Summary
    _completeSummary.setQuery(query);
    if( ! so.checkQueryFeatures().equals(FEATURES_CONTAINER.none)) {
      _completeSummary.showQueryWithClassificationSummaryTab();
    }

    //dual query/hit tables
    _summary.setContent(query, entry);
    
    //Biological CLassification data
    _bcoOverview.setQuery(query);

  }
  
  /**
   * Create the UI.
   */
  private void createGUI() {
    _bvController = new BlastSummaryViewerController();
    
    _completeSummary = _bvController.getQueryOverviewPanel();
    _summary = _bvController.getBlastSummaryViewerPanel();
    _bcoOverview = _bvController.getSRCOverviewPanel();

    JTabbedPane tabbedPane = _bvController.getViewController();
    tabbedPane.addTab(_completeSummary.getTitle(), _completeSummary);
    tabbedPane.addTab(_summary.getTitle(), _summary);
    tabbedPane.addTab(_bcoOverview.getTitle(), _bcoOverview);

    this.setLayout(new BorderLayout());
    this.add(tabbedPane, BorderLayout.CENTER);

  }
  /**
   * Wraps a BlastEntry object into a QueryBase.
   */
  private QueryBase prepareQuery(BlastEntry entry) {
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
    return query;
  }
  /**
   * Wraps a SROutput object into a BlastEntry.
   */
  private BlastEntry prepareEntry(SROutput bo) {
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

    
    return new BlastEntry(program, queryName, null, bo, null, dbname, false);
  }


}
