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

import javax.swing.JTabbedPane;

import bzh.plealog.bioinfo.ui.util.Selection;
import bzh.plealog.blastviewer.summary.QueryOverviewPanel;
import bzh.plealog.blastviewer.summary.SRCOverviewPanel;

/**
 * Controller of other viewers.
 * 
 * @author Patrick G. Durand
 * */
public class BlastSummaryViewerController {
  private QueryOverviewPanel _completeSummary;
  private BlastSummaryViewerPanel _summary;
  private SRCOverviewPanel _bcoOverview;
  private JTabbedPane _mainTab;
  
  /**
   * Return an instance of QueryOverviewPanel.
   */
  public QueryOverviewPanel getQueryOverviewPanel() {
    if (_completeSummary==null) {
      _completeSummary = new QueryOverviewPanel(this);
    }
    return _completeSummary;
  }
  
  /**
   * Return an instance of BlastSummaryViewerPanel.
   */
  public BlastSummaryViewerPanel getBlastSummaryViewerPanel() {
    if (_summary==null) {
      _summary = new BlastSummaryViewerPanel(this);
    }
    return _summary;
  }
  
  /**
   * Return an instance of SRCOverviewPanel.
   */
  public SRCOverviewPanel getSRCOverviewPanel() {
    if (_bcoOverview==null) {
      _bcoOverview = new SRCOverviewPanel(this);
    }
    return _bcoOverview;
  }
  
  /**
   * Return the component containing all viewers.
   * 
   * */
  public JTabbedPane getViewController() {
    if (_mainTab==null) {
      _mainTab = new JTabbedPane();
    }
    return _mainTab;
  }
  
  /**
   * Show particular classification.
   * 
   * @param vType one of AnnotationDataModelConstants.CLASSIF_NAME_TO_CODE keys
   */
  public void showClassification(String vType) {
    _bcoOverview.showClassification(vType);
    _mainTab.setSelectedComponent(_bcoOverview);
  }
  
  /**
   * Show summary table and set particular selection type.
   * 
   * @param selType selection type. 
   **/
  public void showSummary(Selection.SelectType selType) {
    _summary.showSummary(selType);
    _mainTab.setSelectedComponent(_summary);
  }
  
  /**
   * Update the content of the query overview panel.
   * */
  public void updateQueryOverviewContent() {
    _completeSummary.updateContent();
  }
}
