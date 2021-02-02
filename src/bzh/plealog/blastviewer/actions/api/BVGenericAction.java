/* Copyright (C) 2003-2017 Patrick G. Durand
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
package bzh.plealog.blastviewer.actions.api;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.ui.blast.hittable.BlastHitTable;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This class implements a generic action. Action execution is delegated to
 * internal BVAction.
 * 
 * @author Patrick G. Durand
 */
public class BVGenericAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;

  private BVAction          _act;
  private BlastHitTable     _hitTable;

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public BVGenericAction(BVAction act) {
    super(act.getName(), act.getIcon());
    _act = act;
  }

  public BVAction getAction() {
    return _act;
  }

  /**
   * Provides this action with the BlastViewer table.
   * 
   * @param ht
   *          the hit table
   * */
  public void setHitTable(BlastHitTable ht) {
    _hitTable = ht;
  }

  private class ActionRunner extends Thread {
    @Override
    public void run() {
      try {
        SROutput sro = _hitTable.getResult();
        int iterNum = _hitTable.getSelectedIteration();
        int [] selHits = _hitTable.getSelectedHits();
        _act.execute(
            sro, 
            iterNum,
            selHits);
      } catch (Throwable t) {
        EZLogger.warn(BVMessages.getString("OpenFileAction.err")
            + t.toString());
      } finally {
        EZEnvironment.setDefaultCursor();
      }
    }
  }
  public void actionPerformed(ActionEvent event) {
   new ActionRunner().start();
  }

}
