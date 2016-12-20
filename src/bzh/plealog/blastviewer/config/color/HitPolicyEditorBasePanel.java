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
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the base panel for color and quality policy editors.
 * 
 * @author Patrick G. Durand
 */
public abstract class HitPolicyEditorBasePanel extends JPanel {
  private static final long serialVersionUID = -5471357033799472849L;
  protected JTextField[] _valuesFrom;
  protected JTextField[] _valuesTo;
  protected int _nClasses;
  protected int _valueType;// one of ColorColicyConfig.XXX_FIELD
  protected JComboBox<String> _valueTypeChooser;

  protected static final String MSG_P1 = "Assign";
  protected static final String MSG_P2 = "if value is above";
  protected static final String MSG_P3 = "if value is less than";
  protected static final String MSG_P4 = "and above";
  protected static final String MSG_P5 = "in any other cases";
  protected static final String MSG_ERR1 = "Values are not given by descreasing order.";
  protected static final String MSG_ERR2 = "Value is not defined.";
  protected static final Font DEF_FNT = new Font("sans-serif", Font.PLAIN, 12);
  protected static final MessageFormat BAD_VALUE = new MessageFormat("Value {0} is not a positive real number.");

  /**
   * Constructor.
   */
  public HitPolicyEditorBasePanel() {
    super();
  }

  /**
   * Set the type of data.
   * 
   * @param valueType one of ColorColicyConfig.XXX_FIELD
   */
  public void setValueType(int valueType) {
    _valueType = valueType;
    _valueTypeChooser.setSelectedIndex(valueType);
  }

  /**
   * Get the type of data.
   * 
   * @return one of ColorColicyConfig.XXX_FIELD
   */
  public int getValueType() {
    return _valueType;
  }

  /**
   * Create a text field.
   * 
   * @param editable true or false
   * 
   * @return a text field
   */
  protected JTextField createTextField(boolean editable) {
    JTextField tf;

    tf = new JTextField();
    tf.setEditable(editable);
    // tf.setBorder(null);
    // tf.setOpaque(false);
    tf.setFont(DEF_FNT);

    return tf;
  }

  /**
   * Create a label.
   * 
   * @param msg the message of the label
   * 
   * @return a label
   */
  protected JLabel createLabel(String msg) {
    JLabel btn;

    btn = new JLabel(msg);
    btn.setFont(DEF_FNT);
    return btn;
  }

  /**
   * Create a text area used to display help message.
   * 
   * @param nLines number of lines
   * @param defMsg default message
   * 
   * @return a text area
   */
  protected JTextArea createHelper(int nLines, String defMsg) {
    JTextArea helpArea = new JTextArea();
    helpArea.setRows(nLines);
    helpArea.setLineWrap(true);
    helpArea.setWrapStyleWord(true);
    helpArea.setEditable(false);
    helpArea.setOpaque(false);
    helpArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    helpArea.setText(defMsg);
    helpArea.setFont(DEF_FNT);
    return helpArea;
  }

  /**
   * Implement this method to initialize specific components.
   * 
   * @param nClasses nb of classes to use
   */
  protected abstract void initSpecialComponents(int nClasses);

  /**
   * Return a particular component.
   * 
   * @param i ith component to return
   * 
   * @return a component
   */
  protected abstract JComponent getSpecialComponent(int i);

  /**
   * Return the header message.
   * 
   * @return header message
   * */
  protected abstract String getHeaderMsg();

  /**
   * Return the help message.
   * 
   * @return help message
   * */
  protected abstract String getHelpMsg();

  /**
   * Create the UI.
   * 
   * @param nClasses nb of classes to use
   */
  protected void buildGUI(int nClasses) {
    DefaultFormBuilder builder;
    FormLayout layout;
    JLabel emptyLbl;
    JPanel pnl, pnl2, pnl3, pnl4;
    int i;

    emptyLbl = new JLabel();
    _valuesFrom = new JTextField[nClasses];
    _valuesTo = new JTextField[nClasses];
    for (i = 0; i < nClasses; i++) {
      _valuesFrom[i] = createTextField(false);
      _valuesTo[i] = createTextField(true);
    }
    initSpecialComponents(nClasses);
    nClasses--;
    for (i = 0; i < nClasses; i++) {
      _valuesTo[i].getDocument().addDocumentListener(new EditListener(_valuesTo[i], _valuesFrom[i + 1]));
    }
    nClasses++;
    layout = new FormLayout("30dlu, 2dlu, 25dlu, 2dlu, 80dlu, 2dlu, 40dlu, 2dlu, 40dlu, 2dlu, 40dlu", "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    for (i = 0; i < nClasses; i++) {
      if (i == 0) {
        builder.append(createLabel(MSG_P1));
        builder.append(getSpecialComponent(i));
        builder.append(createLabel(MSG_P2));
        builder.append(_valuesTo[i]);
        builder.append(emptyLbl);
        builder.append(emptyLbl);
      } else if (i == nClasses - 1) {
        builder.append(createLabel(MSG_P1));
        builder.append(getSpecialComponent(i));
        builder.append(createLabel(MSG_P5));
        builder.append(emptyLbl);
        builder.append(emptyLbl);
        builder.append(emptyLbl);
      } else {
        builder.append(createLabel(MSG_P1));
        builder.append(getSpecialComponent(i));
        builder.append(createLabel(MSG_P3));
        builder.append(_valuesFrom[i]);
        builder.append(createLabel(MSG_P4));
        builder.append(_valuesTo[i]);
      }
      if ((i + 1) < nClasses)
        builder.nextLine();
    }
    pnl = new JPanel();
    _valueTypeChooser = new JComboBox<>();
    _valueTypeChooser.setFont(DEF_FNT);
    _valueTypeChooser.addActionListener(new ValueTypeSelector());
    for (i = 0; i < ColorPolicyConfigImplem.FIELDS.length; i++) {
      _valueTypeChooser.addItem(ColorPolicyConfigImplem.FIELDS[i]);
    }
    pnl.add(createLabel(getHeaderMsg()));
    pnl.add(_valueTypeChooser);
    pnl.add(createLabel("values"));
    pnl2 = new JPanel(new BorderLayout());
    pnl2.add(pnl, BorderLayout.WEST);
    pnl2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    pnl3 = new JPanel(new BorderLayout());
    pnl3.add(createHelper(5, getHelpMsg()), BorderLayout.CENTER);
    pnl4 = new JPanel(new BorderLayout());
    pnl4.add(pnl2, BorderLayout.NORTH);
    pnl4.add(builder.getContainer(), BorderLayout.CENTER);
    pnl4.add(pnl3, BorderLayout.SOUTH);
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(pnl4);
  }

  protected class EditListener implements DocumentListener {
    private JTextField _src;
    private JTextField _tgt;

    public EditListener(JTextField src, JTextField tgt) {
      _src = src;
      _tgt = tgt;
    }

    public void insertUpdate(DocumentEvent e) {
      _tgt.setText(_src.getText());
    }

    public void removeUpdate(DocumentEvent e) {
      _tgt.setText(_src.getText());
    }

    public void changedUpdate(DocumentEvent e) {
    }
  }

  protected class ValueTypeSelector implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      _valueType = ((JComboBox<?>) e.getSource()).getSelectedIndex();
    }
  }

}
