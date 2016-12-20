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
package bzh.plealog.blastviewer.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import bzh.plealog.blastviewer.config.color.HitPolicyEditorDialog;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * This class manages an action to start the Color Policy Editor.
 * 
 * @author Patrick G. Durand
 */
public class EditColorPolicyAction extends AbstractAction {
  private static final long serialVersionUID = 6933508753277672501L;
  private JTable _parent;

  public EditColorPolicyAction(String name) {
    super(name);
  }

  public EditColorPolicyAction(String name, Icon icon) {
    super(name, icon);
  }

  public void setParent(JTable table) {
    _parent = table;
  }

  public void actionPerformed(ActionEvent event) {
    HitPolicyEditorDialog dlg;

    dlg = new HitPolicyEditorDialog(JOptionPane.getFrameForComponent(EZEnvironment.getParentFrame()));
    dlg.showDlg();
    _parent.repaint();
  }
}
