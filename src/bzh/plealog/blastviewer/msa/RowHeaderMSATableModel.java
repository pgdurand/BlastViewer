/* Copyright (C) 2006-2017 Patrick G. Durand
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import bzh.plealog.bioinfo.api.data.searchresult.SRHit;
import bzh.plealog.bioinfo.api.data.searchresult.SRIteration;
import bzh.plealog.bioinfo.api.data.sequence.DSequenceAlignment;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHspImplem;
import bzh.plealog.bioinfo.ui.blast.core.BlastIteration;

/**
 * TableModel class handling data model for the row headers of the MSA.
 * 
 * @author Patrick G. Durand
 */
public class RowHeaderMSATableModel extends AbstractTableModel {
  private static final long              serialVersionUID = -8575984249817425958L;
  /** current BIteration to display */
  private BlastIteration                 _iteration;
  /** column headers */
  private int[]                          _columnIds;
  /** list of BlastHitHsp objects */
  private ArrayList<RowHeaderEntry>      _data            = new ArrayList<>();
  private Map<String, BlastHitHspImplem> _bhhMap;
  private boolean                        _hasConsensus;
  private boolean                        _hasRefSeq;

  private static final String[]          HEADERS          = { "Sequence", "HSP" };
  public static final int                HIT              = 0;
  public static final int                HSP              = 1;
  // Note: if a new column is added, do not forget to modify
  // the value of constant NB_HEADER_COLUMN
  private static final int               NB_HEADER_COLUMN = 1;

  public RowHeaderMSATableModel() {
    createStandardColHeaders();
  }

  public RowHeaderMSATableModel(BlastIteration iter, boolean hasConsensus,
      boolean hasRefSeq) {
    _hasConsensus = hasConsensus;
    _hasRefSeq = hasRefSeq;
    updateModel(iter);
  }

  public void updateModel(BlastIteration iter) {
    createStandardColHeaders();
    if (iter == null)
      return;
    _iteration = iter;
    createDataTable();
  }

  /**
   * Given a name and a number, returns an identifier.
   */
  private String getKey(String name, int num) {
    return name + "_" + num;
  }

  private void createDataTable() {
    SRIteration iter;
    SRHit hit;
    String id;
    BlastHitHspImplem bhh;
    int qSize, i, hits, j, hsps, bType;

    qSize = _iteration.getQuerySize();
    iter = _iteration.getIteration();
    hits = iter.countHit();
    bType = _iteration.getBlastType();
    _bhhMap = new Hashtable<String, BlastHitHspImplem>();
    if (_hasRefSeq) {
      id = iter.getIterationQueryDesc();
      if (id == null) {
        id = DSequenceAlignment.REFERENCE_NAME;
      } else {
        if (id.length() > 16) {
          id = id.substring(0, 15);
        }
      }
      _data.add(new RowHeaderEntry(id, null));
    }
    if (_hasConsensus)
      _data.add(new RowHeaderEntry(DSequenceAlignment.CONSENSUS_NAME, null));
    for (i = 0; i < hits; i++) {
      hit = iter.getHit(i);
      hsps = hit.countHsp();
      for (j = 0; j < hsps; j++) {
        id = getKey(hit.getHitAccession(), j + 1);
        bhh = new BlastHitHspImplem(hit, _iteration.getEntry()
            .getBlastClientName(), j + 1, qSize, bType);
        _data.add(new RowHeaderEntry(id, bhh));
        _bhhMap.put(id, bhh);
      }
    }
  }

  public Map<String, BlastHitHspImplem> getBhhMap() {
    return _bhhMap;
  }

  public String getColumnName(int column) {
    return HEADERS[_columnIds[column]];
  }

  private void createStandardColHeaders() {
    _columnIds = new int[NB_HEADER_COLUMN];
    _columnIds[0] = HIT;
  }

  public int getColumnCount() {
    return _columnIds.length;
  }

  public int getRowCount() {
    if (_data == null)
      return 0;
    return _data.size() + 1;
  }

  public Object getValueAt(int row, int col) {
    Object val;
    RowHeaderEntry data;
    if (row == 0)
      return "";
    row--;
    data = (RowHeaderEntry) _data.get(row);
    if (col < 0)
      return data;
    switch (col) {
      case HIT:
        val = data.getName();
        break;
      default:
        val = "";
    }
    return val;
  }
}