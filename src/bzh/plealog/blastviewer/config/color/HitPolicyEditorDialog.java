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
package bzh.plealog.blastviewer.config.color;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.log.EZLogger;

/**
 * This is the color policy editor.
 * 
 * @author Patrick G. Durand
 */
public class HitPolicyEditorDialog extends JDialog {
  private static final long           serialVersionUID = 812060132777071379L;
  private HitColorPolicyEditorPanel   _colorPolicy;
  private HitQualityPolicyEditorPanel _qualityPolicy;
  private JTabbedPane                 _jtp;

  /**
   * Constructor.
   * 
   * @param owner
   *          parent frame of this dialog box.
   * */
  public HitPolicyEditorDialog(Frame owner) {
    super(owner, BVMessages.getString("HitPolicyEditorDialog.header"), true);
    buildGUI();
    this.pack();
  }

  /**
   * Create the GUI.
   */
  private void buildGUI() {
    JPanel mainPnl, btnPnl;
    JButton okBtn, cancelBtn;
    ColorPolicyConfigImplem nc;
    HitColorPolicyAtom[] clrPolicy;
    HitQualityPolicyAtom[] qualPolicy;

    nc = (ColorPolicyConfigImplem) ConfigManager.getConfig(ColorPolicyConfigImplem.NAME);
    if (nc == null) {
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(
          new JLabel(BVMessages.getString("HitPolicyEditorDialog.err")),
          BorderLayout.CENTER);
      return;
    }
    clrPolicy = nc.getHitListColorPolicy();
    qualPolicy = nc.getHitListQualityPolicy();
    _colorPolicy = new HitColorPolicyEditorPanel(clrPolicy == null ? 4
        : clrPolicy.length);
    _colorPolicy.setData(clrPolicy);
    _colorPolicy.setValueType(nc.getFieldForColor());
    _qualityPolicy = new HitQualityPolicyEditorPanel(qualPolicy == null ? 4
        : qualPolicy.length);
    _qualityPolicy.setData(qualPolicy);
    _qualityPolicy.setValueType(nc.getFieldForQuality());

    _jtp = new JTabbedPane();
    _jtp.setFocusable(false);
    _jtp.add(BVMessages.getString("HitPolicyEditorDialog.tab1"), _colorPolicy);
    _jtp.add(BVMessages.getString("HitPolicyEditorDialog.tab2"), _qualityPolicy);
    mainPnl = new JPanel(new BorderLayout());
    mainPnl.add(_jtp, BorderLayout.CENTER);

    okBtn = new JButton(BVMessages.getString("HitPolicyEditorDialog.btn1"));
    okBtn.addActionListener(new OkDialogAction());
    cancelBtn = new JButton(BVMessages.getString("HitPolicyEditorDialog.btn2"));
    cancelBtn.addActionListener(new CloseDialogAction());
    btnPnl = new JPanel();
    btnPnl.setLayout(new BoxLayout(btnPnl, BoxLayout.X_AXIS));
    btnPnl.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    btnPnl.add(Box.createHorizontalGlue());
    btnPnl.add(okBtn);
    btnPnl.add(Box.createRigidArea(new Dimension(10, 0)));
    btnPnl.add(cancelBtn);
    btnPnl.add(Box.createHorizontalGlue());

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(mainPnl, BorderLayout.CENTER);
    getContentPane().add(btnPnl, BorderLayout.SOUTH);
  }

  /**
   * Show the dialog box on screen.
   */
  public void showDlg() {
    centerOnScreen();
    setVisible(true);
  }

  /**
   * Center the frame on the screen.
   */
  private void centerOnScreen() {
    Dimension screenSize = this.getToolkit().getScreenSize();
    Dimension dlgSize = this.getSize();

    this.setLocation(screenSize.width / 2 - dlgSize.width / 2,
        screenSize.height / 2 - dlgSize.height / 2);
  }

  /**
   * This inner class manages actions coming from the JButton CloseDialog.
   */
  private class CloseDialogAction extends AbstractAction {
    private static final long serialVersionUID = -7737589249414570785L;

    /**
     * Manages JButton action
     */
    public void actionPerformed(ActionEvent e) {
      dispose();
    }
  }

  /**
   * This inner class manages actions coming from the JButton OkDialog.
   */
  private class OkDialogAction extends AbstractAction {
    private static final long serialVersionUID = -3503165540629775725L;

    /**
     * Manages JButton action
     */
    public void actionPerformed(ActionEvent e) {
      HitColorPolicyAtom[] clrPolicy;
      HitQualityPolicyAtom[] qualPolicy;
      ColorPolicyConfigImplem nc;

      nc = (ColorPolicyConfigImplem) ConfigManager.getConfig(ColorPolicyConfigImplem.NAME);
      // check data
      clrPolicy = _colorPolicy.getData();
      if (clrPolicy == null) {
        _jtp.setSelectedIndex(0);
        return;
      }
      qualPolicy = _qualityPolicy.getData();
      if (qualPolicy == null) {
        _jtp.setSelectedIndex(1);
        return;
      }
      // set and save new data
      nc.setHitListColorPolicy(clrPolicy);
      nc.setFieldForColor(_colorPolicy.getValueType());
      nc.setHitListQualityPolicy(qualPolicy);
      nc.setFieldForQuality(_qualityPolicy.getValueType());
      try {
        nc.save();
      } catch (IOException e1) {
        EZLogger.warn("Unable to save Color Policy: " + e1);
      }
      dispose();
    }
  }
}
