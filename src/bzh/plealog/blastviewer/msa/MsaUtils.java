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

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import bzh.plealog.bioinfo.api.data.sequence.DAlphabet;
import bzh.plealog.bioinfo.api.data.sequence.DSequenceAlignment;
import bzh.plealog.bioinfo.api.data.sequence.DSymbol;
import bzh.plealog.bioinfo.data.sequence.ExportableMSA;
import bzh.plealog.blastviewer.msa.actions.CopySelectionToClipBoardAction;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * Provide utility methods for the MSA viewer.
 * 
 * @author Patrick G. Durand
 * */
public class MsaUtils {

  /**
   * Utility method to retrieve headers from an MSA.
   * 
   * @param msa
   *          the multiple sequence alignment
   * @param putConsensus
   *          add consensus if true
   * 
   * @return an array of headers
   */
  public static String[] getHeaders(ExportableMSA msa, boolean putConsensus) {
    ArrayList<String> lst;
    String[] headers;
    int i;

    lst = new ArrayList<String>();
    for (i = msa.fromRow(); i <= msa.toRow(); i++) {
      if (!putConsensus
          && msa.rowHeader(i).equals(DSequenceAlignment.CONSENSUS_NAME))
        continue;
      lst.add(msa.rowHeader(i));
    }
    headers = new String[lst.size()];
    headers = lst.toArray(headers);
    return headers;
  }

  /**
   * Utility method to get a sequence from an MSA.
   * 
   * @param msa
   *          the multiple sequence alignment
   * @param alphabet
   *          the alphabet of the MSA
   * @param from
   *          region to retrieve
   * @param to
   *          region to retrieve
   * @param row
   *          region to retrieve
   * @param speChar
   *          special character to add to sequence in replacement of alphabet
   *          special DSymbol SPACE_SYMBOL_CODE, GAP_SYMBOL_CODE and
   *          UNKNOWN_SYMBOL_CODE
   * 
   * @return a sequence as a string
   */
  public static String getSequence(ExportableMSA msa, DAlphabet alphabet,
      int from, int to, int row, char speChar) {
    StringBuffer buf;
    char ch;

    buf = new StringBuffer();
    while (from <= to) {
      ch = msa.getSymbol(from, row).getChar();
      if (ch == alphabet.getSymbol(DSymbol.SPACE_SYMBOL_CODE).getChar()
          || ch == alphabet.getSymbol(DSymbol.GAP_SYMBOL_CODE).getChar()
          || ch == alphabet.getSymbol(DSymbol.UNKNOWN_SYMBOL_CODE).getChar())
        ch = speChar;
      buf.append(ch);
      from++;
    }
    return buf.toString();
  }

  /**
   * Utility method to retrieve sequences from an MSA.
   * 
   * @param msa
   *          the multiple sequence alignment
   * @param putConsensus
   *          add consensus if true
   * 
   * @return an array of sequences as strings
   */
  public static String[] getSequences(ExportableMSA msa, boolean putConsensus,
      char speChar) {
    ArrayList<String> lst;
    DAlphabet alphabet;
    String[] seqs;
    int i;

    lst = new ArrayList<String>();
    alphabet = msa.getSequence(0).getSequence().getAlphabet();
    for (i = msa.fromRow(); i <= msa.toRow(); i++) {
      if (!putConsensus
          && msa.rowHeader(i).equals(DSequenceAlignment.CONSENSUS_NAME))
        continue;
      lst.add(getSequence(msa, alphabet, msa.fromColumn(), msa.toColumn(), i,
          speChar));
    }
    seqs = new String[lst.size()];
    seqs = lst.toArray(seqs);

    return seqs;
  }

  /**
   * Utility method to get ExportableMSA from user selection.
   * 
   * @param model
   *          MSA data model
   * @param rowlsm
   *          row selection model
   * @param collsm
   *          column selection model
   * @param hModel
   *          header data model
   * @param forceAll
   *          set to true to force getting entire MSA, set to false to get user
   *          selection region only
   * 
   * @return an ExportableMSA object
   * */
  public static ExportableMSA getExportableMSA(MSATableModel model,
      ListSelectionModel rowlsm, ListSelectionModel collsm,
      RowHeaderMSATableModel hModel, boolean forceAll) {
    ArrayList<String> headers;
    int i, rmin, rmax, cmin, cmax;

    if (rowlsm.isSelectionEmpty() || forceAll) {
      rmin = 1;
      rmax = model.getRowCount() - 1;
    } else {
      rmin = rowlsm.getMinSelectionIndex();
      rmax = rowlsm.getMaxSelectionIndex();
    }
    if (collsm.isSelectionEmpty() || forceAll) {
      cmin = 0;
      cmax = model.getColumnCount() - 1;
    } else {
      cmin = collsm.getMinSelectionIndex();
      cmax = collsm.getMaxSelectionIndex();
    }
    // this code is there to avoid selection of special row zero
    rmin = Math.max(1, rmin);
    rmin--;
    rmax--;
    headers = new ArrayList<>();
    for (i = 1; i < hModel.getRowCount(); i++) {
      headers.add(hModel.getValueAt(i, 0).toString());
    }
    return new ExportableMSA(model.getSeqAlign(), headers, rmin, rmax, cmin,
        cmax);
  }
  
  /**
   * Initialize action map on MSATable. For now it links UI copy action to
   * CopySelectionToClipBoard action.
   * 
   * @param mtbl MSA table
   * @param rtbl Row header MSA table
   */
  public static void initActionMap(MSATable mtbl, RowHeaderMSATable rtbl) {
    ActionMap am = mtbl.getActionMap();
    Object cpName = TransferHandler.getCopyAction().getValue(Action.NAME);
    CopySelectionToClipBoardAction act = new CopySelectionToClipBoardAction(BVMessages
        .getString("BlastHitMSA.copy.msf.btn"));
    act.setMsaTable(mtbl);
    act.setRowHeaderMSATable(rtbl);
    am.put(cpName, act);
  }
}
