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

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * This class implements the action to display a single SROutput 
 * within a Viewer component. Such a SROutput is supposed to contain
 * a single Iteration (i.e. not a multi-query results).
 * 
 * @author Patrick G. Durand
 */
public class OpenBasicViewerAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;
  private SummaryTable _table;
  private boolean _running = false;
  
  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public OpenBasicViewerAction(String name) {
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
  public OpenBasicViewerAction(String name, Icon icon) {
    super(name, icon);
  }

  /**
   * Set data object.
   */
  public void setTable(SummaryTable table) {
    _table = table;
  }  
  
  private void doAction() {
    if (_running || _table==null || _table.getSelectedRowCount()!=1)
      return;

    _running = true;
    
    //get selected SROuput
    SROutput sro = (SROutput) _table.getValueAt(
        _table.getSelectedRow(), 
        SummaryTableModel.RESULT_DATA_COL); 
    if (sro.isEmpty()) {
      return;
    }
    
    //prepare a copy to avoid altering original data in the coming viewer
    SROutput sro_copy = sro.clone(false);
    //such a result only contains a single SRIteration on which we MUST
    //reset ordering number to "1"
    sro_copy.getIteration(0).setIterationIterNum(1);
    //start viewer
    BlastViewerOpener.displayInternalFrame(
        BlastViewerOpener.prepareViewer(sro_copy),
        sro.getBlastTypeStr(), null);
  }

  public void actionPerformed(ActionEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("OpenFileAction.err")
              + t.toString());
        } finally {
          _running = false;
          EZEnvironment.setDefaultCursor();
        }
      }
    });
  }

}
