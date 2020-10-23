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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchjob.SJFileSummary;
import bzh.plealog.bioinfo.api.data.searchresult.SRIteration;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.io.gff.iprscan.IprGffObject;
import bzh.plealog.bioinfo.io.gff.iprscan.IprGffReader;
import bzh.plealog.bioinfo.tools.ImportIprScanPredictions;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.blastviewer.BlastSummaryViewerController;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

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
  private BlastSummaryViewerController _bvController;
  //TODO: enable user to choose between two import modes
  private boolean _importInNewView = true;
  
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
  /**
   * Set BlastSummaryViewerController.
   * 
   * @param bvController BlastSummaryViewerController
   **/
  public void setBlastSummaryViewerController(BlastSummaryViewerController bvController) {
   _bvController = bvController; 
  }
  
  /**
   * Use a thread to avoid UI lock.
   */
  private class Loader extends Thread {
    /**
     * Ask user for Iprscan GFF3 file(s).
     * 
     * @return array of files or null if user canceled dialog box
     */
    private File[] chooseFile() {
      return EZFileManager.chooseFilesForOpenAction(BVMessages
          .getString("ImportIprScanFileAction.lbl"));
    }
    /**
     * Load Iprscan data files.
     * 
     * @param fs array of files
     * 
     * @return Iprscan data or null if files do not contain any appropriate data
     * */
    private Map<String, List<IprGffObject>> loadIprData(File[] fs) {
      //Collect GFF3 files...
  
      EZLogger.info(String.format(BVMessages.getString("ImportIprScanFileAction.log3"), 
          fs.length));
      
      //... then collect all domains
      Map<String, List<IprGffObject>> allGffMap, gffMap;
      allGffMap = new HashMap<String, List<IprGffObject>>();
      String msg = BVMessages.getString("ImportIprScanFileAction.msg3");
      int ncount=0;
      for(File f:fs) {
        ncount++;
        BlastViewerOpener.setHelperMessage(String.format(msg, ncount, fs.length));
        IprGffReader gr = new IprGffReader();
        gffMap = gr.processFileToMap(f.getAbsolutePath());
        allGffMap.putAll(gffMap);
      }
      
      if (allGffMap.isEmpty()) {
        EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(), 
            fs.length==1 ?
                BVMessages.getString("ImportIprScanFileAction.msg1") : 
                  BVMessages.getString("ImportIprScanFileAction.msg1b") );
        return null;
      }
      
      EZLogger.info(String.format(BVMessages.getString("ImportIprScanFileAction.log1"), 
          fs.length, allGffMap.size()));
      
      return allGffMap;
    }
    /**
     * Import IPRscan domain predictions in current view.
     * 
     * @param allGffMap IPRscan data
     * */
    private void importIprInCurrentView(Map<String, List<IprGffObject>> allGffMap) {
      int ncount=0;
      SummaryTableModel model = (SummaryTableModel) _table.getModel();
      SROutput sro;
      int nannot, nSeqAnnotated=0, rows=model.getRowCount();
      ncount=0;
      String msg = BVMessages.getString("ImportIprScanFileAction.msg4");
      for(int i=0; i< rows; i++) {
        ncount++;
        BlastViewerOpener.setHelperMessage(String.format(msg, ncount, rows));
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
    }
    
    /**
     * Import IPRscan domain predictions in a new view.
     * 
     * @param allGffMap IPRscan data
     * @param viewName name of new view
     * */
    private void importIprInNewView(Map<String, List<IprGffObject>> allGffMap, String viewName) {
      SummaryTableModel model = (SummaryTableModel) _table.getModel();
      SROutput          sro, sroMaster=null;
      int               size, nSeqAnnotated=0;
      
      QueryBase query = model.getQuery();
      
      size = query.sequences();
      for(int i=0; i<size; i++) {
        sro = (SROutput) query.getResult(i).clone(false);
        ImportIprScanPredictions importer = new ImportIprScanPredictions();
        if (importer.annotateBlastWithIprscan(sro, allGffMap)!=0)
          nSeqAnnotated++;
       
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
      BlastViewerOpener.displayInternalFrame(viewer, viewName, null);
    }

    /**
     * Run data import job.
     * */
    private void doAction() {
      if (_running)
        return;
      
      _running = true;

      //choose files with IPRscan data (gff3)
      File[] fs = chooseFile();
      if (fs==null) {//user has canceled dialog box
        return;
      }
      
      EZEnvironment.setWaitCursor();
      
      //load data
      Map<String, List<IprGffObject>> allGffMap = loadIprData(fs);
      
      if (allGffMap == null) {
        return;
      }
      
      //annotate queries with data
      if (_importInNewView) {
        importIprInNewView(allGffMap, fs[0].getName());
      }
      else {
        importIprInCurrentView(allGffMap);
      }
      
      //update the query overview panel
      _bvController.updateQueryOverviewContent();
    }
    
    public void run() {
      try {
        doAction();
      } catch (Throwable t) {
        EZLogger.warn(BVMessages.getString("ChooseClassificationAction.err")
            + t.toString());
      } finally {
        _running = false;
        BlastViewerOpener.cleanHelperMessage();
        EZEnvironment.setDefaultCursor();
      }
    }
  }

  public void actionPerformed(ActionEvent event) {
    new Loader().start();
  }

}
