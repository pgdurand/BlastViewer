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
package bzh.plealog.blastviewer.msa.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.api.data.sequence.DAlphabet;
import bzh.plealog.bioinfo.api.data.sequence.DSequence;
import bzh.plealog.bioinfo.api.data.sequence.DSequenceInfo;
import bzh.plealog.bioinfo.data.sequence.ExportableMSA;
import bzh.plealog.bioinfo.io.sequence.FastaExport;
import bzh.plealog.bioinfo.io.sequence.GCGMsaExport;
import bzh.plealog.bioinfo.util.DAlphabetUtils;
import bzh.plealog.blastviewer.config.FileExtension;
import bzh.plealog.blastviewer.msa.MSATable;
import bzh.plealog.blastviewer.msa.MSATableModel;
import bzh.plealog.blastviewer.msa.MsaUtils;
import bzh.plealog.blastviewer.msa.RowHeaderMSATable;
import bzh.plealog.blastviewer.msa.RowHeaderMSATableModel;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileFilter;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.file.EZFileTypes;
import com.plealog.genericapp.implem.file.EZFileExtDescriptor;

import epos.model.sequence.SequenceQNode;
import epos.model.sequence.io.ReadSeqWriter;

/**
 * Action to save an entire MSA or selected region to a file.
 * 
 * @author Patrick G. Durand
 */
public class ExportMSAAction extends AbstractAction {
  private static final long serialVersionUID = -8278921863124027609L;

  private MSATable             msaTable;
  private RowHeaderMSATable headerMsaTable;

  /**
   * Export constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public ExportMSAAction(String name) {
    super(name);
  }

  /**
   * Export constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public ExportMSAAction(String name, Icon icon) {
    super(name, icon);
  }

  public void setMsaTable(MSATable mtbl) {
    msaTable = mtbl;
  }

  public void setRowHeaderMSATable(RowHeaderMSATable mtbl) {
    headerMsaTable = mtbl;
  }

  private void doJob(){
    ArrayList<EZFileExtDescriptor> types;
    String extDesc = null;
    EZFileFilter dff;
    File f;
    ExportableMSA msa;
    DAlphabet alph;
    GCGMsaExport exporter;
    ReadSeqWriter.PhylipWriter phyWriter;
    FastaExport fasExporter;
    String[] headers, seqs;
    DSequence[] fseqs;
    int i, size;
    boolean isProteic;

    // compute the Exportable MSA: either entire one or only used-defined region (selection)
    msa = MsaUtils.getExportableMSA((MSATableModel) msaTable.getModel(), msaTable.getSelectionModel(), 
        msaTable.getColumnModel().getSelectionModel(),(RowHeaderMSATableModel) headerMsaTable.getModel(), false);
    headers = MsaUtils.getHeaders(msa, true);
    seqs = MsaUtils.getSequences(msa, true, '-');
    if (headers.length < 2) {
      EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(),
          BVMessages.getString("BlastHitMSA.save.err1"));
      return;
    }
    
    // setup save dialog box with available export format
    types = new ArrayList<EZFileExtDescriptor>();
    dff = EZFileTypes.getFileFilter(FileExtension.MSF_FEXT);
    types.add(new EZFileExtDescriptor(FileExtension.MSF_FEXT, dff
        .getDescription()));
    dff = EZFileTypes.getFileFilter(FileExtension.PHY_FEXT);
    types.add(new EZFileExtDescriptor(FileExtension.PHY_FEXT, dff
        .getDescription()));
    
    dff = EZFileTypes.getFileFilter(FileExtension.FAS_A_FEXT);
    types.add(new EZFileExtDescriptor(FileExtension.FAS_A_FEXT, dff
        .getDescription()));
    
    dff = EZFileTypes.getFileFilter(FileExtension.FAS_NA_FEXT);
    types.add(new EZFileExtDescriptor(FileExtension.FAS_NA_FEXT, dff
        .getDescription()));
    
    // choose a file
    f = EZFileManager.chooseFileForSaveAction(EZEnvironment.getParentFrame(),
        BVMessages.getString("DDFileTypes.msf.dlg.header"),
        EZFileTypes.getFileFilter(FileExtension.MSF_FEXT), types);
    //cancel ?
    if (f == null)
      return;
    
    // we need the alphabet
    alph = msa.getSequence(0).getSequence().getAlphabet();
    
    // we need the file extension
    extDesc = EZFileFilter.getExtension(f);

    // MSF export
    if (extDesc==null || extDesc.equals(FileExtension.MSF_FEXT)) {
      exporter = new GCGMsaExport(msa);
      if (!exporter.export(f)) {
        String msg = BVMessages.getString("DDFileTypes.msf.err.msg1");
        EZEnvironment.displayErrorMessage(EZEnvironment.getParentFrame(), msg
            + ".");
      }
    } else if (extDesc.equals(FileExtension.PHY_FEXT)) {
      // Phylip export
      phyWriter = new ReadSeqWriter.PhylipWriter();
      isProteic = (alph.getType() == DAlphabet.PROTEIN_ALPHABET);
      phyWriter.setFile(f);
      phyWriter.setType(isProteic ? SequenceQNode.Type.PROTEIN
          : SequenceQNode.Type.DNA);
      phyWriter.setNames(headers);
      phyWriter.setSequences(seqs);
      try {
        phyWriter.write();
      } catch (Exception e) {
        String msg = BVMessages.getString("DDFileTypes.msf.err.msg1");
        EZEnvironment.displayErrorMessage(EZEnvironment.getParentFrame(), msg
            + ".");
      }
    } else {
      // Fasta export
      size = headers.length;
      fseqs = new DSequence[size];
      for (i = 0; i < size; i++) {
        StringReader str = new StringReader(seqs[i]);
        DSequenceInfo dsi = new DSequenceInfo();
        dsi.setId(headers[i]);
        isProteic = (alph.getType() == DAlphabet.PROTEIN_ALPHABET);
        DSequence ds = CoreSystemConfigurator.getSequenceFactory()
            .getSequence(
                str,
                isProteic ? DAlphabetUtils
                    .getIUPAC_Protein_Alphabet() : DAlphabetUtils
                    .getIUPAC_DNA_Alphabet());
        ds.setSequenceInfo(dsi);
        fseqs[i] = ds;
      }

      fasExporter = new FastaExport();
      try (FileOutputStream fos = new FileOutputStream(f)) {
        fasExporter.export(fos, fseqs,
            extDesc.equals(FileExtension.FAS_NA_FEXT) ? DAlphabetUtils.getSpecialSymbols(alph)
                : null);
        fos.flush();
        fos.close();
      } catch (Exception e) {
        String msg = BVMessages.getString("DDFileTypes.msf.err.msg1");
        EZEnvironment.displayErrorMessage(EZEnvironment.getParentFrame(), msg
            + ".");
      }
    }
  }
  public void actionPerformed(ActionEvent event) {
    // OS-dependent File Choosers do not always
    // enable the use of FileExtension chooser. So we bypass these natives
    // Choosers and use default Java ones.
    EZFileManager.useOSNativeFileDialog(false);
    doJob();

    // for the rest of the software, we still use OS-dependent File Choosers
    EZFileManager.useOSNativeFileDialog(true);
  }
}