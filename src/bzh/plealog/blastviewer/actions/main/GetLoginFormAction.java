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
package bzh.plealog.blastviewer.actions.main;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.protection.distrib.HStringUtils;

import bzh.plealog.blastviewer.config.Preferences;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.LoginForm;

/**
 * This class implements the action to display the LoginForm.
 * 
 * @author Patrick G. Durand
 */
public class GetLoginFormAction extends AbstractAction {
  private static final long serialVersionUID = -6690013898654562799L;

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public GetLoginFormAction(String name) {
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
  public GetLoginFormAction(String name, Icon icon) {
    super(name, icon);
  }

  private void doAction() {
    LoginForm lf = new LoginForm();
    String login, pswd;
    
    //get current credentials, if any already set
    login = EZEnvironment.getApplicationProperty(Preferences.LOGIN_PERSIST_KEY);
    if (login!=null) {
      lf.setUserName(HStringUtils.decryptHexString(login));
    }
    pswd = EZEnvironment.getApplicationProperty(Preferences.PSWD_PERSIST_KEY);
    if (pswd!=null) {
      lf.setPassword(HStringUtils.decryptHexString(pswd));
    }
    
    //display the LoginForm dialogue box
    if (!lf.displayLoginForm(EZEnvironment.getParentFrame())) {
      return;
    }
    //values are stored encrypted 
    login = HStringUtils.encryptHexString(String.valueOf(lf.getUserName()));
    pswd = HStringUtils.encryptHexString(String.valueOf(lf.getPasword()));
    EZEnvironment.setApplicationProperty(Preferences.LOGIN_PERSIST_KEY, login);
    EZEnvironment.setApplicationProperty(Preferences.PSWD_PERSIST_KEY, pswd);
  }

  public void actionPerformed(ActionEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("GetLoginFormAction.err")
              + t.toString());
        } finally {
          EZEnvironment.setDefaultCursor();
        }
      }
    });
  }

}
