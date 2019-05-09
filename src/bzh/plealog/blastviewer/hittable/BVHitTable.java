/* Copyright (C) 2003-2016 Patrick G. Durand
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
package bzh.plealog.blastviewer.hittable;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;

import bzh.plealog.bioinfo.ui.blast.hittable.BlastHitTable;
import bzh.plealog.blastviewer.actions.api.BVAction;
import bzh.plealog.blastviewer.actions.api.BVActionManager;
import bzh.plealog.blastviewer.actions.api.BVGenericAction;
import bzh.plealog.blastviewer.actions.hittable.EditColorPolicyAction;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * Specific implementation of a BlastHitTable that adds some controls.
 * 
 * @author Patrick G. Durand
 */
public class BVHitTable extends BlastHitTable {

  private static final long serialVersionUID = -5195223880667175259L;

  public BVHitTable() {
    super();
  }

  public BVHitTable(String id) {
    super(id);
  }

  @Override
  protected Action[] getTableSpecialActionsForMenu() {
    EditColorPolicyAction editAct;
    Action[] acts;

    acts = new Action[1];
    editAct = new EditColorPolicyAction(
        BVMessages.getString("BVHitTable.tool.edt.clr"));
    editAct.setParent(_blastList);
    acts[0] = editAct;
    return acts;
  }

  protected JToolBar getToolbar() {
    JButton btn;
    BVGenericAction gAction;
    JToolBar tBar;

    tBar = new JToolBar();
    tBar.setFloatable(false);
    
    for (BVAction act : BVActionManager.getActions()){
      gAction = new BVGenericAction(act);
      btn = tBar.add(gAction);
      gAction.setHitTable(this);
      gAction.setEnabled(true);
      if (gAction.getAction().getDescription()!=null){
        btn.setToolTipText(gAction.getAction().getDescription());
      }
    }
    
    return tBar;
  }
}
