/* Copyright (C) 2003-2019 Patrick G. Durand
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
package bzh.plealog.blastviewer.actions.summary;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchjob.SJFileSummary;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.io.gff.iprscan.IprGffObject;
import bzh.plealog.bioinfo.io.gff.iprscan.IprGffReader;
import bzh.plealog.bioinfo.tools.ImportIprScanPredictions;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This class implements the action to enable user importing classification data.
 * For now it means importing GFF3 Iprscan result files.
 * 
 * @author Patrick G. Durand
 */
public class ImportIprScanDomainsAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;
  private SummaryTable _table;
  private boolean _running = false;

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public ImportIprScanDomainsAction(String name) {
    super(name);
  }

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public ImportIprScanDomainsAction(String name, Icon icon) {
    super(name, icon);
  }

  /**
   * Set data object.
   */
  public void setTable(SummaryTable table) {
    _table = table;
  }
  
  private void doAction() {
    if (_running)
      return;

    _running = true;

    //Collect GFF3 files...
    File[] fs = EZFileManager.chooseFilesForOpenAction(BVMessages
        .getString("ImportIprScanFileAction.lbl"));
    if (fs == null)// user canceled dlg box
      return;

    EZLogger.info(String.format(BVMessages.getString("ImportIprScanFileAction.log3"), 
        fs.length));
    
    //... then collect all domains
    Map<String, List<IprGffObject>> allGffMap, gffMap;
    allGffMap = new HashMap<String, List<IprGffObject>>();
    for(File f:fs) {
      IprGffReader gr = new IprGffReader();
      gffMap = gr.processFileToMap(f.getAbsolutePath());
      allGffMap.putAll(gffMap);
    }
    
    if (allGffMap.isEmpty()) {
      EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), 
          fs.length==1 ?
              BVMessages.getString("ImportIprScanFileAction.msg1") : 
                BVMessages.getString("ImportIprScanFileAction.msg1b") );
      return;
    }
    
    EZLogger.info(String.format(BVMessages.getString("ImportIprScanFileAction.log1"), 
        fs.length, allGffMap.size()));
    
    //Solution 1: update current view
    SummaryTableModel model = (SummaryTableModel) _table.getModel();
    SROutput sro;
    int nannot, nSeqAnnotated=0;
    for(int i=0; i< model.getRowCount(); i++) {
      sro = (SROutput) model.getValueAt(i, SummaryTableModel.RESULT_DATA_COL);
      ImportIprScanPredictions importer = new ImportIprScanPredictions();
      nannot = importer.annotateBlastWithIprscan(sro, allGffMap);
      if (nannot==0)
        continue;
      SJFileSummary summary = (SJFileSummary) model.getValueAt(i, SummaryTableModel.SUMMARY_DATA_COL);
      if (summary != null) {
        summary.updateQueryClassificationData(sro);
        nSeqAnnotated++;
      }
    }
    EZLogger.info(String.format(BVMessages.getString("ImportIprScanFileAction.log2"), 
        nSeqAnnotated));
    if (nSeqAnnotated==0) {
      EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(),
                BVMessages.getString("ImportIprScanFileAction.msg2") );
      return;
    }
    _table.updateRowHeights();
    
    //Solution 2: open a new viewer
    /*
    SummaryTableModel model = (SummaryTableModel) _table.getModel();
    SROutput sro, sroMaster=null;
    int nannot, nSeqAnnotated=0;
    for(int i=0; i< model.getRowCount(); i++) {
      sro = (SROutput) model.getValueAt(i, SummaryTableModel.RESULT_DATA_COL);
      ImportIprScanPredictions importer = new ImportIprScanPredictions();
      nannot = importer.annotateBlastWithIprscan(sro, allGffMap);
      if (nannot==0)
        continue;
      SJFileSummary summary = (SJFileSummary) model.getValueAt(i, SummaryTableModel.SUMMARY_DATA_COL);
      if (summary != null) {
        summary.updateQueryClassificationData(sro);
        nSeqAnnotated++;
      }
     
      if (sroMaster==null) {
        sroMaster = sro;
      }
      else {
        Enumeration<SRIteration> sriEnum = sro.enumerateIteration();
        while(sriEnum.hasMoreElements()) {
          sroMaster.addIteration(sriEnum.nextElement());
        }
      }
    }
    EZLogger.info(String.format(BVMessages.getString("ImportIprScanFileAction.log2"), 
        nSeqAnnotated));
    if (nSeqAnnotated==0) {
      EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(),
                BVMessages.getString("ImportIprScanFileAction.msg2") );
      return;
    }
    JComponent viewer = BlastViewerOpener.prepareViewer(sroMaster);
    BlastViewerOpener.displayInternalFrame(viewer, fs[0].getName(), null);
    */
  }

  public void actionPerformed(ActionEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("ChooseClassificationAction.err")
              + t.toString());
        } finally {
          _running = false;
          EZEnvironment.setDefaultCursor();
        }
      }
    });
  }

}
