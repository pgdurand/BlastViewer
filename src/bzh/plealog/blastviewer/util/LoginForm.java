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
package bzh.plealog.blastviewer.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A basic form to ask a user for credentials.
 * 
 * @author Patrick G. Durand
 */
public class LoginForm {

  private JPanel         _loginForm;
  private JTextField     _userNameField;
  private JPasswordField _passwordField;
  private JCheckBox      _hidePasswordCheckbox;
  
  private static final String FORM_LBL  = "Set crendentials";
  private static final String UNAME_LBL = "User name:";
  private static final String PSWD_LBL  = "Password:";
  private static final String PSWD2_LBL = "show password";
  
  /**
   * Constructor.
   */
  public LoginForm() {
    initializeLoginForm();
  }

  /**
   * Constructor.
   * 
   * @param username user name to set in the userName field
   */
  public LoginForm(String username) {
    this();
    setUserName(username);
  }

  /**
   * Constructor.
   * 
   * @param username user name to set in the userName field
   * @param password password to set in the password field
   */
  public LoginForm(String username, String password) {
    this(username);
    setPassword(password);
  }

  /**
   * Display the login form as a model dialogue box.
   * 
   * @param parent parent component of this dialogue box
   * 
   * @return true if the user closes the dialogue box using OK button,
   * false otherwise.
   * */
  public boolean displayLoginForm(Component parent) {
    int retVal = JOptionPane.showConfirmDialog(
        parent, 
        _loginForm, 
        FORM_LBL, 
        JOptionPane.OK_CANCEL_OPTION, 
        JOptionPane.QUESTION_MESSAGE);
    return (retVal==JOptionPane.OK_OPTION);
  }

  /**
   * Create a login form.
   */
  private void initializeLoginForm() {
    DefaultFormBuilder builder;
    FormLayout layout;
    
    _loginForm = new JPanel(new BorderLayout(5, 5));
    _userNameField = new JTextField();
    _passwordField = new JPasswordField();
    _hidePasswordCheckbox = new JCheckBox(PSWD2_LBL);
    
    _hidePasswordCheckbox.addItemListener(new ItemListener() {
      private char curEchoChar = _passwordField.getEchoChar();
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          _passwordField.setEchoChar((char) 0);
        } else {
          _passwordField.setEchoChar(curEchoChar);
        }
      }
    });

    layout = new FormLayout("40dlu, 1dlu, 100dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    
    builder.append(UNAME_LBL, _userNameField);
    builder.nextLine();
    builder.append(PSWD_LBL, _passwordField);
    builder.nextLine();
    builder.append("", _hidePasswordCheckbox);
    _loginForm.add(builder.getContainer(), BorderLayout.CENTER);
    
  }

  /**
   * Get the user name.
   * @return a user name
   */
  public String getUserName() {
    return _userNameField.getText();
  }

  /**
   * Set the user name.
   * 
   * @param username a user name
   * */
  public void setUserName(String username) {
    _userNameField.setText(username);
  }

  /**
   * Get the password.
   * @return a password
   */
  public char[] getPasword() {
    return _passwordField.getPassword();
  }

  /**
   * Set the password.
   * 
   * @param password a password
   * */
  public void setPassword(String password) {
    _passwordField.setText(password);
  }
}
