/* Copyright (C) 2003-2020 Patrick G. Durand
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
package bzh.plealog.blastviewer.actions.main;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.ui.common.RadioChooserDialog;
import com.plealog.genericapp.ui.common.RadioChooserEntry;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.blastviewer.config.directory.DirManager;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * This class implements the action to load a Blast XML results from a file.
 * 
 * @author Patrick G. Durand
 */
public class OpenSampleFileAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;

  private static final String NCBI_BLAST_SAMPLE1 = "p12253-refseq.xml";
  private static final String NCBI_BLAST_SAMPLE2 = "blastp-71queries-swissprot.xml";
  private static final String NCBI_BLAST_SAMPLE3 = "annotated-GAAA01000001.zml";

  @SuppressWarnings("serial")
  private static final Hashtable<Integer, String> SAMPLES = new Hashtable<Integer, String>() {{
    put(1, NCBI_BLAST_SAMPLE1);
    put(2, NCBI_BLAST_SAMPLE2);
    put(3, NCBI_BLAST_SAMPLE3);
  }};
 
  
  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public OpenSampleFileAction(String name) {
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
  public OpenSampleFileAction(String name, Icon icon) {
    super(name, icon);
  }

  private class Loader extends Thread {
  
    private String chooseSample() {
      RadioChooserDialog           dlg;
      ArrayList<RadioChooserEntry> items = new ArrayList<>();
      
      items.add(new RadioChooserEntry( 1, 
          BVMessages.getString("OpenSampleFileAction.lbl1"), 
          BVMessages.getString("OpenSampleFileAction.tip1")));
      items.add(new RadioChooserEntry( 2, 
          BVMessages.getString("OpenSampleFileAction.lbl2"), 
          BVMessages.getString("OpenSampleFileAction.tip2")));
      items.add(new RadioChooserEntry( 3, 
          BVMessages.getString("OpenSampleFileAction.lbl3"), 
          BVMessages.getString("OpenSampleFileAction.tip3")));
      
      dlg = new RadioChooserDialog(EZEnvironment.getParentFrame(), 
          BVMessages.getString("OpenSampleFileAction.dlg"), items);
      dlg.showDlg();
      RadioChooserEntry entry = dlg.getSelectedEntry();
      if(entry==null) {
        return null;
      }
      return SAMPLES.get(entry.getId());
    }
    private void doAction() {
      String tgtPath;
      String sampleBlastFile = chooseSample();
      
      if (sampleBlastFile==null)
        return;
      
      try {
        DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
        tgtPath = dmgr.getBlastDataPath() + sampleBlastFile;
      } catch (IOException e) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
            BVMessages.getString("OpenSampleFileAction.err1"));
        EZLogger.warn(e.toString());
        return;
      }
      
      EZEnvironment.setWaitCursor();
      BlastViewerOpener.setHelperMessage(BVMessages
              .getString("OpenFileAction.msg1"));
      
      File f = new File(tgtPath);
      
      if (f.exists() == false || f.length()==0l){
        InputStream  in = BVMessages.class
              .getResourceAsStream(sampleBlastFile);
  
          try (FileOutputStream fos= new FileOutputStream(f);
              BufferedInputStream bis = new BufferedInputStream(in)) {
            int n;
            byte[] buf = new byte[2048];
            while ((n = bis.read(buf)) != -1) {
              fos.write(buf, 0, n);
            }
            fos.flush();
          } catch (IOException e) {
            EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), 
                BVMessages.getString("OpenSampleFileAction.err2"));
            EZLogger.warn(e.toString());
            return;
          }
        }
      
      SROutput sro = BlastViewerOpener.readBlastFile(f);
      
      BlastViewerOpener.setHelperMessage(BVMessages
              .getString("FetchFromNcbiAction.msg4"));
      
      BlastViewerOpener.displayInternalFrame(
          BlastViewerOpener.prepareViewer(sro),
          f.getName(), null);
  }
  public void run() {
	  try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("OpenFileAction.err")
              + t.toString());
        } finally {
          EZEnvironment.setDefaultCursor();
          BlastViewerOpener.cleanHelperMessage();
        }
      }
  }

  
  public void actionPerformed(ActionEvent event) {
	  new Loader().start();
  }

}
