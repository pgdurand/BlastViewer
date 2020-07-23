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
package bzh.plealog.blastviewer.actions.main;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JOptionPane;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.blastviewer.client.ncbi.NcbiFetcherThread;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This class implements the action used to fetch a Blast results directly from
 * the NCBI.
 * 
 * @author Patrick G. Durand
 */
public class FetchFromNcbiAction extends AbstractAction {
  private static final long serialVersionUID = -2654059939053088591L;


  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public FetchFromNcbiAction(String name) {
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
  public FetchFromNcbiAction(String name, Icon icon) {
    super(name, icon);
  }

  public void actionPerformed(ActionEvent event) {
    String rid;

    rid = JOptionPane.showInputDialog(EZEnvironment.getParentFrame(),
        BVMessages.getString("FetchFromNcbiAction.lbl"),
        EZApplicationBranding.getAppName(), JOptionPane.QUESTION_MESSAGE);
    if (rid == null || rid.length() == 0)
      return;
    new NcbiFetcherThread(rid).start();
  }
}
