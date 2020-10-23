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
package bzh.plealog.blastviewer.summary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.TextAnchor;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;
import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchjob.SJFileSummary;
import bzh.plealog.bioinfo.api.data.searchjob.SJTermSummary;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.bioinfo.ui.util.Selection;
import bzh.plealog.blastviewer.BlastSummaryViewerController;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This panel displays an overview of a QueryBase.
 *
 * @author Patrick G. Durand 
 */
public class QueryOverviewPanel extends JPanel {

  private static final long serialVersionUID = 2471156364969442676L;

	private static final String	PIECHART_SECTION_NO_MATCH	= BVMessages.getString("QueryOverviewPanel.lbl4");
	private static final String	PIECHART_SECTION_MATCH    = BVMessages.getString("QueryOverviewPanel.lbl5");

	private static final String	BARCHART_SECTION_SEQUENCES = BVMessages.getString("QueryOverviewPanel.lbl6");
	private static final String	BARCHART_SECTION_HITS      = BVMessages.getString("QueryOverviewPanel.lbl7");

	private static final int    DEFAULT_CHART_SIZE      = 220;
	private static final int    DEFAULT_HEIGHT_CLASSIF  = 120;

	public static final Color   COLOR_MATCH     = new Color(151, 235, 181);
	public static final Color   COLOR_NO_MATCH  = new Color(230, 230, 230);

  private static final String NO_DATA_LABEL        = BVMessages.getString("QueryOverviewPanel.msg1");
  private static final String DEFAULT_FONT_NAME    = "Arial";
  private static final String PIE_CHART_FONT_NAME  = DEFAULT_FONT_NAME;
  private static final Font   PIE_CHART_TITLE_FONT = new Font(PIE_CHART_FONT_NAME, Font.BOLD, 14);
  private static final Font   PIE_CHART_LABEL_FONT = new Font(PIE_CHART_FONT_NAME, Font.PLAIN, 9);

  private QueryBase                    currentQuery = null;
	private JPanel                       infoPanel = null;
	private JPanel                       resultPanel = null;
	private ChartPanel                   resultChart = null;
	private ChartPanel                   sequencesChart = null;
	private JLabel                       lblError = null;
	private QueryOverviewSRCDataTableModel classificationDataModel = null;
	private HitAndSequencesDisplayer     hitAndSequencesDisplayer = new HitAndSequencesDisplayer();
	private BlastSummaryViewerController _bvController;
	
	/**
	 * Constructor.
	 * 
	 * @param bvController the controller aims at sharing events between components
	 */
	public QueryOverviewPanel(BlastSummaryViewerController bvController) {
	  super();
	  _bvController = bvController;
	  createUI();
	}

	/**
	 * Return the title of this component
	 * 
	 * @return a title
	 */
	public String getTitle() {
    return BVMessages.getString("QueryOverviewPanel.title");
  }
	
	/**
	 * Creates a pie chart
	 * 
	 * @param dataset the data to display
	 * @param chartTitle chart title
	 */
	private JFreeChart createChart(PieDataset dataset, String chartTitle) {
	  TextTitle title = null;

	  JFreeChart chart = ChartFactory.createPieChart(
	      chartTitle, // chart title
	      dataset, // data
	      false, // include legend
	      true, // with tooltips
	      false); //with urls
	  chart.setBorderVisible(false);
	  chart.setBackgroundPaint(UIManager.getColor("Panel.background"));
	  title = chart.getTitle();
	  title.setFont(PIE_CHART_TITLE_FONT);
	  title.setPaint(EZEnvironment.getSystemTextColor());

	  PiePlot piePlot = (PiePlot) chart.getPlot();
	  piePlot.setLabelFont(PIE_CHART_LABEL_FONT);
	  piePlot.setNoDataMessage(NO_DATA_LABEL);
	  piePlot.setCircular(true);
	  piePlot.setLabelGap(0.1);
	  piePlot.setBackgroundPaint(UIManager.getColor("Panel.background"));
	  piePlot.setOutlineVisible(false);
	  return chart;
	}

	/**
	 * Creathe the UI.
	 */
	private void createUI() {
	  this.setLayout(new BorderLayout());

	  this.resultPanel = new JPanel();
	  this.resultPanel.setLayout(new GridBagLayout());

	  // result chart
	  GridBagConstraints c = new GridBagConstraints();

	  // error message
	  JPanel tmp = new JPanel(new BorderLayout());
	  c = new GridBagConstraints();
	  c.fill = GridBagConstraints.NONE;
	  c.gridx = 0;
	  c.gridy = 0;
	  c.gridwidth = 3;
	  c.insets = new Insets(0, 0, 20, 0);
	  lblError = new JLabel("", EZEnvironment.getImageIcon("sign_warning.png"), 0);
	  tmp.add(lblError);
	  this.resultPanel.add(tmp, c);

	  // headers
	  tmp = new JPanel(new BorderLayout());
	  c = new GridBagConstraints();
	  c.fill = GridBagConstraints.NONE;
	  c.gridx = 0;
	  c.gridy = 1;
	  tmp.add(new JLabel(BVMessages.getString("QueryOverviewPanel.lbl1")));
	  this.resultPanel.add(tmp, c);
	  c.gridx = 1;
	  tmp = new JPanel(new BorderLayout());
	  tmp.add(new JLabel(BVMessages.getString("QueryOverviewPanel.lbl2")));
	  this.resultPanel.add(tmp, c);
	  c.gridx = 2;
	  tmp = new JPanel(new BorderLayout());
	  tmp.add(new JLabel(BVMessages.getString("QueryOverviewPanel.lbl3")));
	  this.resultPanel.add(tmp, c);

	  c = new GridBagConstraints();
	  c.fill = GridBagConstraints.NONE;
	  c.gridx = 0;
	  c.gridy = 2;
	  this.resultChart = new ChartPanel(
	      createChart(new DefaultPieDataset(), ""), DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, 
	      DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, 
	      false, false, false, false, false, false);
	  this.resultChart.setMinimumSize(new Dimension(DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE));
	  this.resultChart.addMouseListener(new ClickableMouseListener());
	  this.resultChart.addChartMouseListener(new ChartMouseListener() {

	    @Override
	    public void chartMouseMoved(ChartMouseEvent arg0) {

	    }

	    @Override
	    public void chartMouseClicked(ChartMouseEvent arg0) {
	      ChartEntity entity = arg0.getEntity();
	      if (entity instanceof PieSectionEntity) {
	        String sectionClicked = ((PieSectionEntity) entity).getSectionKey().toString();
	        if (sectionClicked.equals(PIECHART_SECTION_MATCH)) {
	          _bvController.showSummary(Selection.SelectType.WITH_HITS);
	        } else if (sectionClicked.equals(PIECHART_SECTION_NO_MATCH)) {
            _bvController.showSummary(Selection.SelectType.WITHOUT_HITS);
	        }
	      }
	    }
	  });
	  this.resultPanel.add(resultChart, c);

	  // classification data
	  c = new GridBagConstraints();
	  c.fill = GridBagConstraints.NONE;
	  c.gridx = 1;
	  this.classificationDataModel = new QueryOverviewSRCDataTableModel();
	  tmp = new JPanel(new BorderLayout());
	  QueryOverviewSRCDataTable classificationTable = 
	      new QueryOverviewSRCDataTable(this.resultPanel.getBackground(), this.classificationDataModel, _bvController);
	  classificationTable.addMouseListener(new ClickableMouseListener());
	  tmp.add(classificationTable, BorderLayout.CENTER);
	  tmp.setMaximumSize(new Dimension(DEFAULT_CHART_SIZE, DEFAULT_HEIGHT_CLASSIF));
	  tmp.setMinimumSize(new Dimension(DEFAULT_CHART_SIZE, DEFAULT_HEIGHT_CLASSIF));
	  this.resultPanel.add(tmp, c);

	  // queries
	  c.fill = GridBagConstraints.HORIZONTAL;
	  c.gridx = 2;
	  this.sequencesChart = new ChartPanel(
	      ChartFactory.createBarChart(
	          null, "", "", 
	          new DefaultCategoryDataset(), 
	          PlotOrientation.VERTICAL, false, false, false),
	        DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, 
	        DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE, 
	        false, false, false, false, false,
	      false);
	  this.sequencesChart.setMinimumSize(new Dimension(DEFAULT_CHART_SIZE, DEFAULT_CHART_SIZE));
	  this.sequencesChart.addMouseListener(new ClickableMouseListener());
	  this.sequencesChart.addChartMouseListener(new ChartMouseListener() {

	    @Override
	    public void chartMouseMoved(ChartMouseEvent arg0) {

	    }

	    @Override
	    public void chartMouseClicked(ChartMouseEvent arg0) {
	      ChartEntity entity = arg0.getEntity();
	      if (entity instanceof CategoryItemEntity) {
	        String sectionClicked = ((CategoryItemEntity) entity).getRowKey().toString();
	        if (sectionClicked.equals(BARCHART_SECTION_SEQUENCES)) {
	          //getProjectManagerPanel().showBestHitTable(SelectType.ALL);
	        } else if (sectionClicked.equals(BARCHART_SECTION_HITS)) {
	          //getProjectManagerPanel().showHitTable();
	        }
	      }
	    }
	  });
	  this.resultPanel.add(this.sequencesChart, c);
	  JScrollPane scroller = new JScrollPane(this.resultPanel);
	  scroller.setBorder(BorderFactory.createEmptyBorder());
	  this.add(scroller, BorderLayout.CENTER);


	  this.infoPanel = new JPanel();
	  this.infoPanel.setLayout(new GridBagLayout());
	  this.add(this.infoPanel, BorderLayout.EAST);
	}

	/**
	 * Update the parameter label to make it clickable : underline + cursor HAND
	 * 
	 * @param label
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void setClickableLabel(final JLabel label) {
	  Font font = label.getFont();
	  Map attributes = font.getAttributes();
	  attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
	  label.setFont(font.deriveFont(attributes));
	  label.addMouseListener(new ClickableMouseListener());
	}


	/**
	 * Set the current query and update the UI to display the new query's data
	 *  
	 * @param query
	 */
	public void setQuery(QueryBase query) {
	  this.currentQuery = query;
	  updateContent();
	  this.infoPanel.repaint();
	  this.repaint();
	}

	private void handleTerms(List<SJTermSummary> terms, SJFileSummary summary) {
	  if(terms!=null) {
      for(SJTermSummary term : terms) {
        String vType = AnnotationDataModelConstants.CLASSIF_CODE_TO_NAME.get(term.getViewType());
        if (vType!=null) {
          this.classificationDataModel.setClassificationAvailable(vType);
        }
        else if (summary.hasTaxonomyData()) {
          this.classificationDataModel.setClassificationAvailable(
              AnnotationDataModelConstants.TAXON_INDEX_LABEL);
        }
      }
    }
	}
	/**
	 * Update the result panel looking at the current query's state 
	 */
	public void updateContent() {
	  //For future use: BLAST/PLAST engine may report an error
	  this.lblError.setVisible(false);

	  // results chart
	  DefaultPieDataset dataset = new DefaultPieDataset();
	  dataset.setValue(PIECHART_SECTION_MATCH, this.currentQuery.getTotalMatchingQueries());
	  dataset.setValue(PIECHART_SECTION_NO_MATCH, this.currentQuery.getTotalNotMatchingQueries());

	  JFreeChart result = createChart(dataset, "");

	  PiePlot piePlot = (PiePlot) result.getPlot();
	  piePlot.setSimpleLabels(true);
	  piePlot.setLabelGenerator(
	      new StandardPieSectionLabelGenerator(
	          "{0} {2} ({1})", 
	          NumberFormat.getInstance(Locale.US), 
	          NumberFormat.getPercentInstance(Locale.US)));
	  piePlot.setSectionPaint(PIECHART_SECTION_MATCH, COLOR_MATCH);
	  piePlot.setSectionPaint(PIECHART_SECTION_NO_MATCH, COLOR_NO_MATCH);
	  piePlot.setShadowPaint(null);
	  this.resultChart.setChart(result);

	  // check Classifications data
	  Enumeration<SJFileSummary> summaries = this.currentQuery.getSummaries();
	  while(summaries.hasMoreElements()) {
	    SJFileSummary summary = summaries.nextElement();
	    //Hit
	    handleTerms(summary.getHitClassificationForView(), summary);
	    //Query (e.g. IPRscan domain prediction import)
	    handleTerms(summary.getQueryClassificationForView(), summary);
	  }

	  // bar chart for hit and sequences
	  this.hitAndSequencesDisplayer.updateContent();
	  this.revalidate();
	  this.repaint();

	}


	/*
	 * For future use, use a separate class. Future use means adding BLAST/PLAST engine to BlastViewer.
	 * At that time, we'll need to refresh dynamically this viewer.
	 * */
	private class HitAndSequencesDisplayer {

	  public void updateContent() {
	    QueryOverviewPanel.this.sequencesChart.setVisible(false);
	    int nbAccessions = QueryOverviewPanel.this.currentQuery.getTotalHitAccessions();
	    if (nbAccessions < 0) {
	      nbAccessions = 0;
	    }
	    DefaultCategoryDataset sequencesDataSet = new DefaultCategoryDataset();
	    sequencesDataSet.addValue(QueryOverviewPanel.this.currentQuery.sequences(), BARCHART_SECTION_SEQUENCES, "");
	    sequencesDataSet.addValue(nbAccessions, BARCHART_SECTION_HITS, "");
	    JFreeChart chart = ChartFactory.createBarChart(null, "", "", sequencesDataSet, PlotOrientation.VERTICAL, true, false, false);
	    chart.setBackgroundPaint(QueryOverviewPanel.this.getBackground());
	    // legend
	    LegendTitle legend = (LegendTitle) chart.getLegend();
	    legend.setBorder(0, 0, 0, 0);
	    legend.setItemFont(UIManager.getFont("Label.font"));
	    legend.setItemPaint(UIManager.getColor("Label.foreground"));
	    legend.setBackgroundPaint(QueryOverviewPanel.this.getBackground());
	    // get a reference to the plot for further customisation...
	    CategoryPlot plot = chart.getCategoryPlot();
	    // remove y axis
	    plot.getRangeAxis().setVisible(false);
	    plot.setRangeGridlinesVisible(false);
	    plot.setBackgroundPaint(new Color(0, 0, 0, 0));
	    plot.setOutlineVisible(false);
	    BarRenderer renderer = (BarRenderer) plot.getRenderer();
	    renderer.setShadowVisible(false);
	    // display values
	    for (int i = 0; i < sequencesDataSet.getRowCount(); i++) {
	      renderer.setSeriesItemLabelGenerator(i, new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getInstance(Locale.US)));
	      renderer.setSeriesItemLabelsVisible(i, true);
	      renderer.setSeriesItemLabelFont(i, UIManager.getFont("Label.font"));
	      renderer.setSeriesItemLabelPaint(i, UIManager.getColor("Label.foreground"));
	    }
	    // to avoid value label cut off
	    int max = Math.max(QueryOverviewPanel.this.currentQuery.sequences(), nbAccessions);
	    int min = Math.min(QueryOverviewPanel.this.currentQuery.sequences(), nbAccessions);
	    double pctDif = ((double) min / (double) max) * 100;
	    if (pctDif > 90) {
	      renderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BASELINE_CENTER));
	      renderer.setSeriesPositiveItemLabelPosition(1, new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BASELINE_CENTER));
	    } else if (QueryOverviewPanel.this.currentQuery.sequences() > nbAccessions) {
	      renderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BASELINE_CENTER));
	    } else if (QueryOverviewPanel.this.currentQuery.sequences() < nbAccessions) {
	      renderer.setSeriesPositiveItemLabelPosition(1, new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.BASELINE_CENTER));
	    }
	    renderer.setSeriesPaint(0, SummaryTableModel.COLOR_QUERY);
	    renderer.setSeriesPaint(1, SummaryTableModel.COLOR_HIT);
	    renderer.setBarPainter(new StandardBarPainter());

	    chart.getCategoryPlot().setRenderer(renderer);
	    QueryOverviewPanel.this.sequencesChart.setChart(chart);
	    QueryOverviewPanel.this.sequencesChart.setVisible(true);
	  }
	}
}
