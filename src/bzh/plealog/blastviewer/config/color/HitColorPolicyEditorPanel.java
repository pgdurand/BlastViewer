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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * This is the editor of color policy.
 * 
 * @author Patrick G. Durand
 */
public class HitColorPolicyEditorPanel extends HitPolicyEditorBasePanel {
  private static final long serialVersionUID = 9094467477284177517L;

  private JButton[] _clr;

  private static final String MSG_P1_TIP = "Click to change colour";
  private static final String MSG_HEAD = "Colour policy will be applied using";
  private static final String HELP_MSG = "This panel defines the value ranges used to set Hit colours. "
      + "Values set for each range have to be defined by decreasing order. "
      + "Only use positive real numbers; scientific notation is allowed (e.g. 1e-3). "
      + "Click on a coloured button to change a colour.";
  private static final String CHOOSE_MSG = "Choose a color";
  
  /**
   * Constructor.
   * 
   * @param nb of color classes to use
   */
  public HitColorPolicyEditorPanel(int nClasses) {
    super();
    if (nClasses == 0)
      nClasses = 4;
    _nClasses = nClasses;
    buildGUI(nClasses);
  }

  /**
   * Constructor.
   *
   * @param policy a color policy
   * 
   * @param valueType one of ColorColicyConfig.XXX_FIELD
   */
  public HitColorPolicyEditorPanel(HitColorPolicyAtom[] policy, int valueType) {
    super();
    if (policy == null)
      _nClasses = 4;
    else
      _nClasses = policy.length;
    buildGUI(_nClasses);
    setData(policy);
    setValueType(valueType);
  }

  /**
   * Set a color policy.
   * 
   * @param policy the color policy
   * 
   * */
  public void setData(HitColorPolicyAtom[] policy) {
    Color clr;
    int i;

    if (policy == null || policy.length != _nClasses)
      return;
    for (i = 0; i < _nClasses; i++) {
      _valuesTo[i].setText(String.valueOf(policy[i].getThreshold()));
      clr = policy[i].getColor(false);
      _clr[i].setBackground(clr);
    }
  }

  /**
   * Get the color policy.
   * 
   * @return the color policy
   * 
   * */
  public HitColorPolicyAtom[] getData() {
    HitColorPolicyAtom[] policy = null;
    String val;
    Color clr;
    int i;
    double[] thresholds;

    thresholds = new double[_nClasses];
    for (i = 0; i < _nClasses - 1; i++) {
      val = _valuesTo[i].getText();
      if (val.length() == 0) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), MSG_ERR2);
        _valuesTo[i].grabFocus();
        return null;
      }
      try {
        thresholds[i] = Double.valueOf(val);
        if (thresholds[i] < 0)
          throw new NumberFormatException();
      } catch (Exception e) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), BAD_VALUE.format(new Object[] { val }));
        _valuesTo[i].grabFocus();
        return null;
      }
    }
    for (i = 0; i < _nClasses - 2; i++) {
      if (thresholds[i + 1] > thresholds[i]) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), MSG_ERR1);
        _valuesTo[i].grabFocus();
        return null;
      }
    }
    policy = new HitColorPolicyAtom[_nClasses];
    for (i = 0; i < _nClasses - 1; i++) {
      policy[i] = new HitColorPolicyAtom();
      policy[i].setThreshold(thresholds[i]);
      clr = _clr[i].getBackground();
      policy[i].setColor(clr);
    }
    i = _nClasses - 1;
    policy[i] = new HitColorPolicyAtom();
    policy[i].setThreshold(0);
    clr = _clr[i].getBackground();
    policy[i].setColor(clr);
    return policy;
  }

  private JButton createClrButton() {
    JButton btn;
    FontMetrics fm;
    Dimension dim;
    int h;

    btn = new JButton();
    btn.setOpaque(true);
    btn.setBorder(BorderFactory.createRaisedBevelBorder());
    btn.setFont(DEF_FNT);
    btn.setToolTipText(MSG_P1_TIP);
    btn.addActionListener(new ColorChooser());
    fm = btn.getFontMetrics(DEF_FNT);
    h = fm.getHeight();
    dim = new Dimension(6 * h, h);
    btn.setPreferredSize(dim);
    return btn;
  }

  protected void initSpecialComponents(int nClasses) {
    _clr = new JButton[nClasses];
    for (int i = 0; i < nClasses; i++) {
      _clr[i] = createClrButton();
    }
  }

  protected JComponent getSpecialComponent(int i) {
    return _clr[i];
  }

  protected String getHeaderMsg() {
    return MSG_HEAD;
  }

  protected String getHelpMsg() {
    return HELP_MSG;
  }

  private class ColorChooser implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      Color bg;

      bg = JColorChooser.showDialog(HitColorPolicyEditorPanel.this, CHOOSE_MSG,
          ((JButton) e.getSource()).getBackground());
      if (bg != null)
        ((JButton) e.getSource()).setBackground(bg);
    }
  }
}
