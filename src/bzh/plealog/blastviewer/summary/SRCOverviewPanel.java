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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FontMetrics;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;
import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.ui.blast.config.color.ColorPolicyConfig;
import bzh.plealog.bioinfo.ui.blast.hittable.PercentRenderer;
import bzh.plealog.bioinfo.ui.feature.FeatureWebLinker;
import bzh.plealog.bioinfo.ui.feature.TableCellButtonLinker;
import bzh.plealog.bioinfo.ui.feature.TableCellButtonRenderer;
import bzh.plealog.blastviewer.BlastSummaryViewerController;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * The ontology summary viewer.
 * 
 * @author Patrick G. Durand
 */
public class SRCOverviewPanel extends JPanel {

  private static final long serialVersionUID = 6568478900968849909L;

  private JFeaturesTable _jfeaturesTable = null;
  private JScrollPane _jtableScrollPane = null;
  private SRCOverviewControllerPanel _bcoOverviewQueryPanel;
  private JPanel _cardTablePanel;
  private TableCellButtonLinker _cellLinker;
  private TableCellButtonRenderer _cellRenderer;
  private static final String COMPUTATION_NO_DATA_FOUND_LABEL = BVMessages.getString("SRCOverviewPanel.lbl31");
  private static final String COMPUTATION_REFRESH_LABEL = BVMessages.getString("SRCOverviewPanel.lbl13");
  private static final String COMPUTATION_ON_GOING_LABEL = BVMessages.getString("SRCOverviewPanel.lbl14");
  private static final String SEARCH_NO_DATA_FOUND_TABLE_PANEL_NAME = "no_data_table_panel_name";
  private static final String SEARCH_EMPTY_TABLE_PANEL_NAME = "empty_table_panel_name";
  private static final String TABLE_PANE_NAME = "table_pane_name";
  private static final String SEARCH_ON_GOING_TABLE_PANEL_NAME = "ongoing_table_panel_name";
  
  /**
   * Constructor.
   * 
   * @param bvController the controller aims at sharing events between components
   */
  public SRCOverviewPanel(BlastSummaryViewerController bvController) {
    super();
    createUI();
  }
  /**
   * Return the title of this component
   * 
   * @return a title
   */
  public String getTitle() {
    return BVMessages.getString("SRCOverviewPanel.title");
  }
  /**
   * Set the current query and update the UI to display the new query's data
   *  
   * @param query
   */
  public void setQuery(QueryBase query) {
    _bcoOverviewQueryPanel.setQuery(query);
    ((CardLayout) _cardTablePanel.getLayout()).show(_cardTablePanel, SEARCH_EMPTY_TABLE_PANEL_NAME);
  }

  /**
   * Update display model given type of ontology to show.
   *  
   * @param model the new ontology data model
   */
  public void updateModel(SRCOverviewTableModel model) {
    if (model != null) {
      _jfeaturesTable.setModel(model);
      _jfeaturesTable.setAutoCreateRowSorter(true);
      initColumnSize(_jfeaturesTable.getSize().width);
      ((CardLayout) _cardTablePanel.getLayout()).show(_cardTablePanel, TABLE_PANE_NAME);
    } else {
      ((CardLayout) _cardTablePanel.getLayout()).show(_cardTablePanel, SEARCH_NO_DATA_FOUND_TABLE_PANEL_NAME);
    }
  }

  /**
   * Show particular classification.
   * 
   * @param vType one of  AnnotationDataModelConstants.CLASSIF_NAME_TO_CODE keys
   */
  public void showClassification(String vType) {
    _bcoOverviewQueryPanel.showClassification(vType);
  }
  /**
   * Creathe the UI.
   */
  private void createUI() {
    setLayout(new BorderLayout());

    _cellLinker = new TableCellButtonLinker(
        new MyFeatureWebLinker(), //will load default URLs to classifications located in Plealog-UI package 
        new JCheckBox(),
        SRCOverviewTableModel.URL_COLUMN_INDEX,
        SRCOverviewTableModel.ACCESS_COLUMN_INDEX);
    _cellRenderer = new TableCellButtonRenderer();
    _bcoOverviewQueryPanel = new SRCOverviewControllerPanel();
    _bcoOverviewQueryPanel.setParentPanel(this);
    JPanel actionTopPanel = new JPanel(new BorderLayout());
    actionTopPanel.add(_bcoOverviewQueryPanel, BorderLayout.CENTER);

    // middle panels
    JPanel noDataFoundTablePanel = createInfoPanel(COMPUTATION_NO_DATA_FOUND_LABEL);
    JPanel emptyTablePanel = createInfoPanel(COMPUTATION_REFRESH_LABEL);
    JPanel ongoingTablePanel = createInfoPanel(COMPUTATION_ON_GOING_LABEL);

    JScrollPane tablePane = createResultTablePane();

    JTabbedPane featuresTabbedPane = new JTabbedPane();
    featuresTabbedPane.add("Table", tablePane);

    JPanel cardTablePanel = new JPanel(new CardLayout());
    cardTablePanel.add(noDataFoundTablePanel, SEARCH_NO_DATA_FOUND_TABLE_PANEL_NAME);
    cardTablePanel.add(emptyTablePanel, SEARCH_EMPTY_TABLE_PANEL_NAME);
    cardTablePanel.add(featuresTabbedPane, TABLE_PANE_NAME);
    cardTablePanel.add(ongoingTablePanel, SEARCH_ON_GOING_TABLE_PANEL_NAME);

    _cardTablePanel = cardTablePanel;

    ((CardLayout) cardTablePanel.getLayout()).show(cardTablePanel, SEARCH_EMPTY_TABLE_PANEL_NAME);

    add(actionTopPanel, BorderLayout.NORTH);
    add(cardTablePanel, BorderLayout.CENTER);
  }

  private JPanel createInfoPanel(String label) {
    JPanel panel = null;
    JLabel jlabel = null;

    jlabel = new JLabel(label, null, JLabel.CENTER);
    panel = new JPanel(new BorderLayout());
    panel.add(jlabel, BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    SRCOverviewControllerPanel.setFontSize(jlabel);

    return panel;
  }

  private JScrollPane createResultTablePane() {
    SRCOverviewTableModel model = null;

    model = new SRCOverviewTableModel(null, AnnotationDataModelConstants.ANNOTATION_CATEGORY.TAX);

    _jfeaturesTable = new JFeaturesTable(model);
    _jfeaturesTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
    _jfeaturesTable.getTableHeader().setReorderingAllowed(false);
    _jfeaturesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    _jfeaturesTable.setColumnSelectionAllowed(false);
    _jtableScrollPane = new JScrollPane(_jfeaturesTable);

    return _jtableScrollPane;
  }

  private void initColumnSize(int width) {
    FontMetrics fm;
    TableColumnModel tableColumnModel;
    TableColumn tableColumn = null;
    TableColumn lastTableColumn = null;
    String header;
    int i, size, tot, val, d;

    fm = _jfeaturesTable.getFontMetrics(_jfeaturesTable.getFont());
    tableColumnModel = _jfeaturesTable.getColumnModel();
    size = tableColumnModel.getColumnCount();
    tot = 0;
    for (i = 0; i < size; i++) {
      tableColumn = tableColumnModel.getColumn(i);
      header = tableColumn.getHeaderValue().toString();
      if (i != SRCOverviewTableModel.LABEL_COLUMN_INDEX) {
        if (i==SRCOverviewTableModel.RANK_COLUMN_INDEX) {
          d = 6;
        }
        else {
          d = 3;
        }
          
        val = d * fm.stringWidth(header);
        tableColumn.setPreferredWidth(val);
        /*
         * Following can be use to lock the width of a column Could be interesting to
         * add to the TableHeaderColumItem a field specifying which column has a locked
         * size. 
         */
        /*if(i==SRCOverviewTableModel.URL_COLUMN_INDEX) {
          tableColumn.setMinWidth(val); tableColumn.setMaxWidth(val);
        }*/
        tot += val;
      } else {
        lastTableColumn = tableColumn;
      }
    }
    if (lastTableColumn != null) {
      lastTableColumn.setPreferredWidth(width - tot - 2);
    }
  }

  /**
   * feature table class.
   * 
   */
  private class JFeaturesTable extends JTable {
    /**
     * 
     */
    private static final long serialVersionUID = -6418320961313526926L;
    PercentRenderer _pctRenderer = null;

    public JFeaturesTable(TableModel model) {
      super(model);
      this.setGridColor(Color.LIGHT_GRAY);
      _pctRenderer = new PercentRenderer();
    }

    public boolean isCellEditable(int rowIndex, int colIndex) {
      if (colIndex == SRCOverviewTableModel.URL_COLUMN_INDEX)
        return true;
      else
        return false;
    }

    public TableCellEditor getCellEditor(int row, int column) {
      if (column == SRCOverviewTableModel.URL_COLUMN_INDEX)
        return _cellLinker;
      else
        return super.getCellEditor(row, column);
    }
    /**
     * This table cell renderer.
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
      TableCellRenderer tcr;
      if (column == SRCOverviewTableModel.URL_COLUMN_INDEX) {
        //_cellRenderer.setIcon(EZEnvironment.getImageIcon("small_earth.png"));
        //return _cellRenderer;
        FeatureWebLinker linker = _cellLinker.getFeatureWebLinker();
        String qName = getValueAt(row, SRCOverviewTableModel.URL_COLUMN_INDEX).toString();
        String qVal = getValueAt(row, SRCOverviewTableModel.ACCESS_COLUMN_INDEX).toString();
        _cellRenderer.setText("");
        if (linker != null && linker.isLinkable(qName, qVal)) {
          _cellRenderer.setIcon(EZEnvironment.getImageIcon("small_earth.png"));
          return _cellRenderer;
        }
      }
      int id;

      id = ((SRCOverviewTableModel) this.getModel()).get_columnIds()[column].getIID();

      if (id == SRCOverviewTableModel.PERCENT_COLUMN_INDEX) {
        tcr = _pctRenderer;
      } else {
        tcr = super.getCellRenderer(row, column);
        if (tcr instanceof JLabel) {
          JLabel lbl;
          lbl = (JLabel) tcr;
          if (id == SRCOverviewTableModel.RANK_COLUMN_INDEX || id == SRCOverviewTableModel.NB_HITS_INDEX) {
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
          } else {
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
          }
          if (row % 2 == 0) {
            lbl.setBackground(ColorPolicyConfig.BK_COLOR);
          } else {
            lbl.setBackground(Color.WHITE);
          }
        }
      }
      return tcr;
      
     
    }
  }
  private class MyFeatureWebLinker extends FeatureWebLinker{

    public MyFeatureWebLinker() {
      super();
    }

    @Override
    public String[] getData(String qualName, String qualValue) {
      String dbCode, dbId;
      
      if (qualValue.startsWith(AnnotationDataModelConstants.ANNOTATION_CATEGORY.EC.getType())) {
        dbCode = AnnotationDataModelConstants.ANNOTATION_CATEGORY.EC.getEncoding();
        dbId = qualValue.substring(qualValue.indexOf(':')+1);
      }
      else if (qualValue.startsWith(AnnotationDataModelConstants.ANNOTATION_CATEGORY.GO.getType())) {
        dbCode = AnnotationDataModelConstants.ANNOTATION_CATEGORY.GO.getEncoding();
        dbId = qualValue;
      }
      else if (qualValue.startsWith(AnnotationDataModelConstants.ANNOTATION_CATEGORY.IPR.getType())) {
        dbCode = AnnotationDataModelConstants.ANNOTATION_CATEGORY.IPR.getEncoding();
        dbId = qualValue;
      }
      else {
        dbCode = AnnotationDataModelConstants.ANNOTATION_CATEGORY.TAX.getEncoding();
        dbId = qualValue;
      }
      return new String[] { dbCode, dbId };
    }
    
  }

}
