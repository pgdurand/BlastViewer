/* 
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
package test;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.plealog.genericapp.protection.distrib.HStringUtils;

import bzh.plealog.blastviewer.util.LoginForm;

/**
 * A sample application to test the LoginForm class.
 * 
 * @author Patrick G. Durand
 */
public class LoginFormTest {
  public static void main(String[] args) {
    Runnable r = new Runnable() {

      @Override
      public void run() {
        JLabel credentialsLbl = new JLabel("<html><body style='width: 300px; height: 175px;></body></html>", SwingConstants.CENTER);
        JFrame appFrame = new JFrame("LoginFormTest");
        appFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        appFrame.setLocationByPlatform(true);
        appFrame.add(credentialsLbl);
        appFrame.pack();
        appFrame.setMinimumSize(appFrame.getSize());
        appFrame.setVisible(true);

        LoginForm lif = new LoginForm();
        lif.setUserName(System.getProperty("user.name"));
        
        if (lif.displayLoginForm(appFrame)) {
          String encrypted = HStringUtils.encryptHexString(new String(lif.getPasword()));
          System.out.println(encrypted);
          credentialsLbl.setText(String.format("Welcome %s (%s)", lif.getUserName(), encrypted));
        } else {
          credentialsLbl.setText("No credentials set!");
        }
      }
    };
    SwingUtilities.invokeLater(r);
  }

}
