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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import com.plealog.genericapp.api.EZEnvironment;

/**
 * This is the editor of quality policy.
 * 
 * @author Patrick G. Durand
 */
public class HitQualityPolicyEditorPanel extends HitPolicyEditorBasePanel {
  private static final long    serialVersionUID = 7552474180799736916L;

  private JComboBox<Integer>[] _quality;

  private static final String  MSG_HEAD         = "Quality policy will be applied using";
  private static final String  HELP_MSG         = "This panel defines the value ranges used to set Hit quality. "
                                                    + "Values set for each range have to be defined by decreasing order. "
                                                    + "Only use positive real numbers; scientific notation is allowed (e.g. 1e-3).";

  /**
   * Constructor.
   * 
   * @param nb
   *          of quality classes to use
   */
  public HitQualityPolicyEditorPanel(int nClasses) {
    super();
    if (nClasses == 0)
      nClasses = 4;
    _nClasses = nClasses;
    buildGUI(nClasses);
  }

  /**
   * Constructor.
   * 
   * @param policy
   *          a quality policy
   * 
   * @param valueType
   *          one of ColorColicyConfig.XXX_FIELD
   */
  public HitQualityPolicyEditorPanel(HitQualityPolicyAtom[] policy,
      int valueType) {
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
   * Set a quality policy.
   * 
   * @param policy
   *          the quality policy
   * 
   * */
  public void setData(HitQualityPolicyAtom[] policy) {
    int i;

    if (policy == null || policy.length != _nClasses)
      return;
    for (i = 0; i < _nClasses; i++) {
      _valuesTo[i].setText(String.valueOf(policy[i].getThreshold()));
      _quality[i].setSelectedIndex(policy[i].getIconId());
    }
  }

  /**
   * Get the quality policy.
   * 
   * @return the quality policy
   * 
   * */
  public HitQualityPolicyAtom[] getData() {
    HitQualityPolicyAtom[] policy = null;
    String val;
    int i, idx;
    double[] thresholds;

    thresholds = new double[_nClasses];
    for (i = 0; i < _nClasses - 1; i++) {
      val = _valuesTo[i].getText();
      if (val.length() == 0) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(),
            MSG_ERR2);
        _valuesTo[i].grabFocus();
        return null;
      }
      try {
        thresholds[i] = Double.valueOf(val);
        if (thresholds[i] < 0)
          throw new NumberFormatException();
      } catch (Exception e) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(),
            BAD_VALUE.format(new Object[] { val }));
        _valuesTo[i].grabFocus();
        return null;
      }
    }
    for (i = 0; i < _nClasses - 2; i++) {
      if (thresholds[i + 1] > thresholds[i]) {
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(),
            MSG_ERR1);
        _valuesTo[i].grabFocus();
        return null;
      }
    }
    policy = new HitQualityPolicyAtom[_nClasses];
    for (i = 0; i < _nClasses - 1; i++) {
      policy[i] = new HitQualityPolicyAtom();
      policy[i].setThreshold(thresholds[i]);
      idx = _quality[i].getSelectedIndex();
      policy[i].setQualityIcon(ColorPolicyConfigImplem.QUALITY_SMILEY[idx]);
      policy[i].setIconId(idx);

    }
    i = _nClasses - 1;
    policy[i] = new HitQualityPolicyAtom();
    policy[i].setThreshold(0);
    idx = _quality[i].getSelectedIndex();
    policy[i].setQualityIcon(ColorPolicyConfigImplem.QUALITY_SMILEY[idx]);
    policy[i].setIconId(idx);
    return policy;
  }

  private JComboBox<Integer> createQualityCombo() {
    JComboBox<Integer> c = new JComboBox<>();
    c.setFont(DEF_FNT);
    c.setRenderer(new ComboBoxRenderer());
    for (int i = 0; i < ColorPolicyConfigImplem.QUALITY_SMILEY.length; i++) {
      c.addItem(new Integer(i));
    }
    return c;
  }

  @SuppressWarnings("unchecked")
  protected void initSpecialComponents(int nClasses) {
    _quality = new JComboBox[nClasses];
    for (int i = 0; i < nClasses; i++) {
      _quality[i] = createQualityCombo();
    }
  }

  protected JComponent getSpecialComponent(int i) {
    return _quality[i];
  }

  protected String getHeaderMsg() {
    return MSG_HEAD;
  }

  protected String getHelpMsg() {
    return HELP_MSG;
  }

  private class ComboBoxRenderer extends JLabel implements
      ListCellRenderer<Integer> {
    private static final long serialVersionUID = -3429945853105749966L;
    private Dimension         DEF_DIM          = new Dimension(20, 20);

    public ComboBoxRenderer() {
      setOpaque(true);
      setHorizontalAlignment(CENTER);
      setVerticalAlignment(CENTER);
    }

    public Dimension getPreferredSize() {
      return DEF_DIM;
    }

    public Component getListCellRendererComponent(
        JList<? extends Integer> list, Integer value, int index,
        boolean isSelected, boolean cellHasFocus) {
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setIcon(ColorPolicyConfigImplem.QUALITY_SMILEY[value]);
      return this;
    }
  }
}
