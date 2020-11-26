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

import java.util.List;

import javax.swing.table.AbstractTableModel;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;
import bzh.plealog.bioinfo.api.data.searchresult.SRClassificationCountTerm;
import bzh.plealog.bioinfo.ui.util.TableColumnManager;
import bzh.plealog.bioinfo.ui.util.TableHeaderColumnItem;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * Data model of QueryOverviewSRCDataTable.
 * 
 * @author Patrick G. Durand
 */
public class SRCOverviewTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 7630301685381501916L;
  private TableHeaderColumnItem[] _refColumnIds;
  private TableHeaderColumnItem[] _columnIds;
  private Object[][] _data;
  private String[] _defColNames;
  private String _colIDs;
  private AnnotationDataModelConstants.ANNOTATION_CATEGORY _indexType;

  private final static Object[][] TABLE_DEFAULT_ROW = { { 0, "", "", "", 0, 0.d } };

  public static final String DEF_COL_ITEM_HEADERS_INT = "0,1,2,3,5,6";
  public static final String TAX_COL_ITEM_HEADERS_INT = "0,1,2,3,4,5,6";
  public static final String RANK_COL_ITEM_HEADERS_INT = "0,4,5,6";

  public final static int RANK_COLUMN_INDEX = 0;
  public final static int URL_COLUMN_INDEX = 1;
  public final static int ACCESS_COLUMN_INDEX = 2;
  public final static int LABEL_COLUMN_INDEX = 3;
  public final static int TAXRANK_COLUMN_INDEX = 4;
  public final static int NB_HITS_INDEX = 5;
  public final static int PERCENT_COLUMN_INDEX = 6;

  public final static int[] RES_HEADERS_INT = { 
      RANK_COLUMN_INDEX, URL_COLUMN_INDEX, ACCESS_COLUMN_INDEX, 
      LABEL_COLUMN_INDEX,TAXRANK_COLUMN_INDEX, NB_HITS_INDEX, 
      PERCENT_COLUMN_INDEX };

  private final String[] RES_HEADERS_HITS = { 
      BVMessages.getString("SRCOverviewPanel.lbl3"), // #
      " ", // #URL
      BVMessages.getString("SRCOverviewPanel.lbl4"), // Accession
      BVMessages.getString("SRCOverviewPanel.lbl5"), // Label
      BVMessages.getString("SRCOverviewPanel.lbl34"), // Rank
      BVMessages.getString("SRCOverviewPanel.lbl6"), // Nb Hits
      BVMessages.getString("SRCOverviewPanel.lbl7"),// Percent.
  };
  private final String[] RES_HEADERS_QUERIES = { 
      BVMessages.getString("SRCOverviewPanel.lbl3"), // #
      " ", // #URL
      BVMessages.getString("SRCOverviewPanel.lbl4"), // Accession
      BVMessages.getString("SRCOverviewPanel.lbl5"), // Label
      BVMessages.getString("SRCOverviewPanel.lbl34"), // Rank
      BVMessages.getString("SRCOverviewPanel.lbl35"), // Nb Queries
      BVMessages.getString("SRCOverviewPanel.lbl7"),// Percent.
  };


  /**
   * Constructor.
   * 
   * @param terms list of classification/ontology terms to display
   * @param indexType type of classification/ontology
   */
  public SRCOverviewTableModel(List<SRClassificationCountTerm> terms,
      AnnotationDataModelConstants.ANNOTATION_CATEGORY indexType) {

    if (terms != null && terms.isEmpty() == false) {
      // Setup the viewer model
      Object[][] sData;

      sData = new Object[terms.size()][SRCOverviewTableModel.RES_HEADERS_INT.length];
      int idx = 0;
      String s;
      for (SRClassificationCountTerm term : terms) {
        sData[idx][SRCOverviewTableModel.RANK_COLUMN_INDEX] = idx;
        if(indexType.equals(AnnotationDataModelConstants.ANNOTATION_CATEGORY.TAX)) {
          s = term.getTerm().getID();
          s = s.substring(s.indexOf(':')+1);
        }
        else {
          s = term.getTerm().getID();
        }
        sData[idx][SRCOverviewTableModel.ACCESS_COLUMN_INDEX] = 
            String.format(
                "[%s%s] %s", 
                term.getQueryCount()!=0?"Q":"-",
                term.getHitCount()!=0?"H":"-",
                s);
        sData[idx][SRCOverviewTableModel.URL_COLUMN_INDEX] = "";
        sData[idx][SRCOverviewTableModel.LABEL_COLUMN_INDEX] = term.getTerm().getDescription();
        sData[idx][SRCOverviewTableModel.TAXRANK_COLUMN_INDEX] = "-";// TODO: specific code
        sData[idx][SRCOverviewTableModel.NB_HITS_INDEX] = term.getCount();
        sData[idx][SRCOverviewTableModel.PERCENT_COLUMN_INDEX] = term.getPercent();

        idx++;
      }

      _data = sData;
    } else {
      _data = TABLE_DEFAULT_ROW;
    }
    _indexType = indexType;
    initDefColNames();
    if (_indexType.equals(AnnotationDataModelConstants.ANNOTATION_CATEGORY.TAX)) {
      createDefaultColHeaders(TAX_COL_ITEM_HEADERS_INT);
    } else {
      createDefaultColHeaders(DEF_COL_ITEM_HEADERS_INT);
    }
    createStandardColHeaders();
  }

  /**
   * Return the type of classification/ontology.
   */
  public AnnotationDataModelConstants.ANNOTATION_CATEGORY getIndexType() {
    return _indexType;
  }

  private void initDefColNames() {
    String[] headers;

    if (_indexType.equals(AnnotationDataModelConstants.ANNOTATION_CATEGORY.LCA)) {
      headers = RES_HEADERS_QUERIES;
    } else {
      headers = RES_HEADERS_HITS;
    }
    _defColNames = new String[headers.length];
    for (int i = 0; i < headers.length; i++) {
      _defColNames[i] = headers[i];
    }
  }

  public void updateColumnHeaders(TableHeaderColumnItem[] colH) {
    _columnIds = colH;
  }

  public Object[][] getData() {
    return _data;
  }

  public String getAccession(int row) {
    String value = this.getValueAt(row, SRCOverviewTableModel.ACCESS_COLUMN_INDEX).toString();
    return value.substring(value.indexOf(' ')+1);
  }
  public String getLabel(int row) {
    return this.getValueAt(row, SRCOverviewTableModel.LABEL_COLUMN_INDEX).toString();
  }
  
  private void createDefaultColHeaders(String defColIDs) {
    List<Integer> idSet;

    if (defColIDs == null)
      defColIDs = DEF_COL_ITEM_HEADERS_INT;

    _colIDs = defColIDs;

    idSet = TableColumnManager.getDefColumns(defColIDs);
    _refColumnIds = new TableHeaderColumnItem[7];
    _refColumnIds[0] = new TableHeaderColumnItem(_defColNames[0], RES_HEADERS_INT[0], true,
        idSet.contains(RES_HEADERS_INT[0]));

    _refColumnIds[1] = new TableHeaderColumnItem(_defColNames[1], RES_HEADERS_INT[1], false,
        idSet.contains(RES_HEADERS_INT[1]));

    _refColumnIds[2] = new TableHeaderColumnItem(_defColNames[2], RES_HEADERS_INT[2], false,
        idSet.contains(RES_HEADERS_INT[2]));
    _refColumnIds[3] = new TableHeaderColumnItem(_defColNames[3], RES_HEADERS_INT[3], false,
        idSet.contains(RES_HEADERS_INT[3]));
    _refColumnIds[4] = new TableHeaderColumnItem(_defColNames[4], RES_HEADERS_INT[4], false,
        idSet.contains(RES_HEADERS_INT[4]));
    _refColumnIds[5] = new TableHeaderColumnItem(_defColNames[5], RES_HEADERS_INT[5], false,
        idSet.contains(RES_HEADERS_INT[5]));
    _refColumnIds[6] = new TableHeaderColumnItem(_defColNames[6], RES_HEADERS_INT[6], false,
        idSet.contains(RES_HEADERS_INT[6]));
  }

  private void createStandardColHeaders() {
    int i, n;

    n = 0;
    for (i = 0; i < _refColumnIds.length; i++) {
      if (_refColumnIds[i].isVisible())
        n++;
    }
    _columnIds = new TableHeaderColumnItem[n];
    n = 0;
    for (i = 0; i < _refColumnIds.length; i++) {
      if (_refColumnIds[i].isVisible()) {
        _columnIds[n] = _refColumnIds[i];
        n++;
      }
    }
  }

  public Object getValueAt(int row, int col) {
    Object val = null;
    int id;

    if (_data == null)
      return "-";

    id = _columnIds[col].getIID();

    val = _data[row][id];

    if (val == null) {
      val = "-";
    }

    return (val);
  }

  public Class<?> getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

  public int getRowCount() {
    return _data.length;
  }

  public int getColumnCount() {
    return _columnIds.length;
  }

  public String getColumnName(int column) {
    return _columnIds[column].getSID();
  }

  public TableHeaderColumnItem[] get_columnIds() {
    return _columnIds;
  }

  /**
   * @return the colIDs
   */
  public String getColIDs() {
    return _colIDs;
  }

}