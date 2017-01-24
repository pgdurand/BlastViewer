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

import javax.swing.table.AbstractTableModel;

import bzh.plealog.bioinfo.api.data.searchresult.SRHit;
import bzh.plealog.bioinfo.api.data.searchresult.SRIteration;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.sequence.DAlphabet;
import bzh.plealog.bioinfo.api.data.sequence.DSequence;
import bzh.plealog.bioinfo.api.data.sequence.DSequenceAlignment;
import bzh.plealog.bioinfo.api.data.sequence.DSymbol;
import bzh.plealog.bioinfo.api.data.sequence.stat.ConsensusCell;
import bzh.plealog.bioinfo.api.data.sequence.stat.ConsensusModel;
import bzh.plealog.bioinfo.api.data.sequence.stat.DefaultConsensModel;
import bzh.plealog.bioinfo.api.data.sequence.stat.PositionSpecificMatrix;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHSP;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHspImplem;
import bzh.plealog.bioinfo.ui.blast.core.BlastIteration;

import com.plealog.genericapp.api.log.EZLogger;

/**
 * TableModel class handling the access to the multiple sequence alignment data.
 * 
 * @author Patrick G. Durand
 */
public class MSATableModel extends AbstractTableModel implements ConsensusModel {
  private static final long      serialVersionUID = 2567742518510864517L;
  private BlastIteration         _iteration;
  private DefaultConsensModel    _consensusModel;
  private DSequenceAlignment     _msa;
  private PositionSpecificMatrix _pssMatrix;
  private ArrayList<BlastHitHSP> _data;
  private boolean                _hasConsensus;
  private boolean                _hasRefSeq;

  public MSATableModel() {
    _data = new ArrayList<>();
  }

  public MSATableModel(BlastIteration iter, int[] colIds) {
    this();
    
    SROutput bo;
    DSequence refSeq;

    if (iter == null)
      return;
    _iteration = iter;
    try {
      _msa = _iteration.getIteration().getMultipleSequenceAlignment(
          _iteration.getQuerySize(), _iteration.getBlastType());
      _pssMatrix = _msa
          .getPositionSpecificMatrix(PositionSpecificMatrix.POSITION_SPECIFIC_COUNTING_MATRIX);
    } catch (Exception ex) {
      EZLogger.warn(ex.toString());
      _msa = null;
    }
    bo = iter.getEntry().getResult();
    if (bo.getBlastType() == SROutput.BLASTP
        || bo.getBlastType() == SROutput.SCANPS
        || bo.getBlastType() == SROutput.PSIBLAST
        || bo.getBlastType() == SROutput.BLASTN
        || bo.getBlastType() == SROutput.TBLASTN) {
      refSeq = iter.getEntry().getQuery();
    } else {
      // to allow the display of translated query within the consensus
      // header, we will need to translate the query on the fly using
      // the genetic code employed during the Blast search!!!
      // this work has to be done... one day!
      refSeq = null;
    }
    _consensusModel = new DefaultConsensModel(_msa,
        PositionSpecificMatrix.POSITION_SPECIFIC_COUNTING_MATRIX);
    _consensusModel.computeConsensus(1);
    _msa.setSpecialSequences(_consensusModel, refSeq);
    _hasConsensus = _consensusModel != null;
    _hasRefSeq = refSeq != null;
    createDataTable(_hasConsensus, _hasRefSeq);
  }

  public PositionSpecificMatrix getPositionSpecificMatrix() {
    return _pssMatrix;
  }

  public String getColumnName(int column) {
    // value for header is handled by the CellRenderer
    return "";
  }

  public boolean hasConsensus() {
    return _hasConsensus;
  }

  public boolean hasRefSeq() {
    return _hasRefSeq;
  }

  public int getColumnCount() {
    if (_msa == null)
      return 0;
    else
      return _msa.columns();
  }

  public int getRowCount() {
    if (_msa == null)
      return 0;
    else
      return _msa.rows() + 1;
  }

  protected DSequenceAlignment getSeqAlign() {
    return _msa;
  }

  public ConsensusCell[] getConsensusCells() {
    if (_consensusModel == null)
      return null;
    return _consensusModel.getConsensusCells();
  }

  public Object getValueAt(int row, int col) {
    Object val;
    DSymbol symbol;
    DAlphabet alph;
    BlastHitHSP data;

    if (row == 0 && col >= 0)
      return _pssMatrix.getCounter(col);
    row--;
    data = (BlastHitHSP) _data.get(row);
    if (col < 0)
      return data;
    symbol = _msa.getSymbol(col, row);
    alph = _msa.getSequence(0).getSequence().getAlphabet();
    if (symbol.getChar() == alph.getSymbol(DSymbol.UNKNOWN_SYMBOL_CODE)
        .getChar())
      val = alph.getSymbol(DSymbol.SPACE_SYMBOL_CODE);
    else
      val = symbol;
    return val;
  }

  public void computeConsensus(int threshold) {
    if (_consensusModel == null)
      return;
    _consensusModel.computeConsensus(threshold);
    this.fireTableRowsUpdated(0, 1);
  }

  private void createDataTable(boolean hasConsensus, boolean hasRefSeq) {
    SRIteration iter;
    SRHit hit;
    int qSize, i, hits, j, hsps, bType;

    qSize = _iteration.getQuerySize();
    iter = _iteration.getIteration();
    hits = iter.countHit();
    bType = _iteration.getBlastType();
    if (hasConsensus)
      _data.add(null);
    if (hasRefSeq)
      _data.add(null);
    for (i = 0; i < hits; i++) {
      hit = iter.getHit(i);
      hsps = hit.countHsp();
      for (j = 0; j < hsps; j++) {
        _data.add(new BlastHitHspImplem(hit, _iteration.getEntry()
            .getBlastClientName(), j + 1, qSize, bType));
      }
    }
  }
}