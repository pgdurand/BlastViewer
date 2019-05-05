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
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.api.data.feature.Feature;
import bzh.plealog.bioinfo.api.data.feature.Qualifier;
import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchresult.SRHsp;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.SRRequestInfo;
import bzh.plealog.bioinfo.data.searchjob.InMemoryQuery;
import bzh.plealog.bioinfo.data.searchresult.SRUtils;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.blast.config.color.ColorPolicyConfig;
import bzh.plealog.bioinfo.ui.blast.config.color.DefaultHitColorPolicy;
import bzh.plealog.bioinfo.ui.blast.core.BlastEntry;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHSP;
import bzh.plealog.bioinfo.ui.blast.core.QueryBaseUI;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListSupport;
import bzh.plealog.bioinfo.ui.blast.hittable.BlastHitTable;
import bzh.plealog.bioinfo.ui.blast.nav.BlastNavigator;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.bioinfo.ui.blast.saviewer.SeqAlignViewer;
import bzh.plealog.bioinfo.ui.blast.summary.GraphicViewer;
import bzh.plealog.bioinfo.ui.carto.data.BasicFeatureOrganizer;
import bzh.plealog.bioinfo.ui.carto.data.FGraphics;
import bzh.plealog.bioinfo.ui.carto.data.FeatureOrganizerManager;
import bzh.plealog.bioinfo.ui.carto.painter.FeaturePainter;
import bzh.plealog.bioinfo.ui.resources.SVMessages;
import bzh.plealog.blastviewer.msa.PhyloMSAPanel;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.ui.common.JHeadPanel;

/**
 * This is the BlastViewer Main Module.
 * 
 * It wraps within a single component the various
 * elements required to displayed Blast data: a BlastNavigator, a Blast Hit Table,
 * the pairwise sequence alignment viewer, etc.
 * 
 * @author Patrick G. Durand
 */
public class BlastViewerPanel extends JPanel {

  private static final long serialVersionUID = -2405089127382200483L;

  protected SummaryTable _summaryTable;
  protected BlastHitTable _hitListPane;
  protected SeqAlignViewer _seqAlignViewer;
  protected BlastNavigator _summaryPane;
  protected JPanel _rightPane;
  protected BlastHitListSupport _updateSupport;
  protected GraphicViewer _cartoViewer;
  protected PhyloMSAPanel _msaPane;
  
  protected static final String HITPANEL_HEADER = SVMessages.getString("BlastViewerPanel.0");
  protected static final String HITPANEL_LIST = SVMessages.getString("BlastViewerPanel.1");
  protected static final String HITPANEL_GRAPHIC = SVMessages.getString("BlastViewerPanel.2");

  /**
   * Default constructor.
   */
  public BlastViewerPanel() {
    super();
    createGUI();
    BasicFeatureOrganizer.setFeatureOrganizerManager(new LocalFeatureOrganizerManager());
  }

  /**
   * Set the data to display in this viewer.
   */
  public void setContent(BlastEntry entry) {
    _summaryPane.setContent(entry);
    
  //Prepare a View from the Model
    InMemoryQuery query;
    query = new InMemoryQuery();
    List<SROutput> results = SRUtils.splitMultiResult(entry.getResult());
    for(SROutput sro : results) {
      query.addResult(sro);
    }
    // following is done manually, but real data can be retrieved 
    // from bo object (not shown here)
    query.setDatabankName("SwissProt");
    query.setEngineSysName("blastp");
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
   * Return the Hit currently selected in this BlastViewerPanel. Actually, the
   * method returns the Hit that is currently displayed by the SeqAlignViewer
   * panel.
   */
  public BlastHitHSP getSelectedHit() {
    return _seqAlignViewer.getCurrentHit();
  }

  /**
   * Return the HSP currently selected in this BlastViewerPanel. Actually, the
   * method returns the HSP that is currently displayed by the SeqAlignViewer
   * panel.
   */
  public SRHsp getSelectedHsp() {
    return _seqAlignViewer.getCurrentHsp();
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
    JHeadPanel headPanel;
    ImageIcon icon;

    _summaryTable = new SummaryTable(new SummaryTableModel());
    _updateSupport = new BlastHitListSupport();
    _summaryPane = new BlastNavigator();
    _hitListPane = ConfigManager.getHitTableFactory().createViewer();
    _seqAlignViewer = ConfigManager.getSeqAlignViewerFactory().createViewer();
    _msaPane = new PhyloMSAPanel();
    
    icon = EZEnvironment.getImageIcon("hitTable.png");
    if (icon != null) {
      headPanel = new JHeadPanel(icon, HITPANEL_HEADER, _hitListPane);
    } else {
      headPanel = new JHeadPanel(null, HITPANEL_HEADER, _hitListPane);
    }
    headPanel.setToolPanel(_summaryPane);
    _rightPane = new JPanel(new BorderLayout());
    // _rightPane.add(_summaryPane, BorderLayout.NORTH);
    _rightPane.add(headPanel, BorderLayout.CENTER);
    _rightPane.add(_seqAlignViewer, BorderLayout.SOUTH);

    _cartoViewer = new GraphicViewer();

    JTabbedPane jtp;

    if (EZEnvironment.getOSType() == EZEnvironment.MAC_OS) {
      jtp = new JTabbedPane(JTabbedPane.LEFT);
    } else {
      jtp = new JTabbedPane(JTabbedPane.TOP);
    }
    jtp.add("Summary", _summaryTable);
    jtp.add("Hits", _rightPane);
    jtp.add("Graphic", _cartoViewer);
    jtp.add("MSA", _msaPane);
    
    this.setLayout(new BorderLayout());
    this.add(jtp, BorderLayout.CENTER);

    // listeners to the selection of a new BIteration
    _summaryPane.addIterationListener(_hitListPane);
    _summaryPane.addIterationListener(_cartoViewer);
    _summaryPane.addIterationListener(_msaPane);
    // listeners to the change of data model
    _hitListPane.addHitDataListener(_seqAlignViewer);
    // listeners to selection within hit tables
    _hitListPane.registerHitListSupport(_updateSupport);
    _seqAlignViewer.registerHitListSupport(_updateSupport);
    _msaPane.registerHitListSupport(_updateSupport);
    _updateSupport.addBlastHitListListener(_hitListPane);
    _updateSupport.addBlastHitListListener(_seqAlignViewer);
    _updateSupport.addBlastHitListListener(_msaPane);
    this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  private class LocalFeatureOrganizerManager implements FeatureOrganizerManager {

    private ColorPolicyConfig nc;
    private SRHsp fakeHsp;
    
    private LocalFeatureOrganizerManager(){
      nc = (ColorPolicyConfig) ConfigManager.getConfig(ColorPolicyConfig.NAME);
      fakeHsp = CoreSystemConfigurator.getSRFactory().createBHsp();
      fakeHsp.setScores(CoreSystemConfigurator.getSRFactory().createBHspScore());
    }
    
    private Color getColor(String score) {
      Color clr = Color.BLACK;
      int val = (int) Math.round(Double.valueOf(score));
      
      if (nc == null) {
        clr = DefaultHitColorPolicy.getColor(val);
      } else {
        fakeHsp.getScores().setBitScore(val);
        clr = nc.getHitColor(fakeHsp, false);
      }
      return clr;
    }

    @Override
    public String[] getFeatureOrderingNames() {
      return null;
    }

    @Override
    public FGraphics getFGraphics(Feature feat, FGraphics fg) {
      if (feat.getKey().equals(GraphicViewer.BHIT_FEATURE_TYPE) == false)
        return null;

      FGraphics fg2 = fg;
      for (Qualifier qual : Collections.list(feat.enumQualifiers())) {
        if (qual.getName().equals(GraphicViewer.SCORE_BITS_QUALIFIER)) {
          fg2 = (FGraphics) fg.clone();
          fg2.setBackgroundColor(getColor(qual.getValue()));
        }
      }
      return fg2;
    }

    @Override
    public FeaturePainter getFeaturePainter(Feature feat, FeaturePainter fp) {
      return null;
    }

    @Override
    public String getReferenceFeatureName() {
      return null;
    }
  }
}
