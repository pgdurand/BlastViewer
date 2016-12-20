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
package test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import bzh.plealog.blastviewer.config.color.ColorPolicyConfigImplem;
import bzh.plealog.blastviewer.config.color.HitColorPolicyAtom;
import bzh.plealog.blastviewer.config.color.HitColorPolicyEditorPanel;

public class HitColorPolicyEditorPanelTest {
  public static void main(String[] args) {
    JFrame frame = new JFrame("Hello");
    JPanel pnl = new JPanel(new BorderLayout());
    frame.addWindowListener(new ApplicationCloser());
    Container contentPane = frame.getContentPane();
    final HitColorPolicyEditorPanel toto = new HitColorPolicyEditorPanel(4);
    toto.setValueType(ColorPolicyConfigImplem.GAPS_FIELD);
    pnl.add(toto, BorderLayout.CENTER);
    JButton checker = new JButton("check values!");
    checker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        HitColorPolicyAtom[] policy = toto.getData();
        if (policy == null) {
          return;
        }
        System.out.println("Value type: "
            + ColorPolicyConfigImplem.FIELDS[toto.getValueType()]);
        System.out.println("Policy: ");
        for (int i = 0; i < policy.length; i++) {
          System.out.println("[" + i + "] Threshold = "
              + policy[i].getThreshold());
          System.out.println("    Color = " + policy[i].getClrRepr());
        }
      }
    });
    pnl.add(checker, BorderLayout.SOUTH);
    contentPane.add(pnl);
    frame.pack();
    // frame.setSize(200, 600);
    frame.setVisible(true);
  }

  private static class ApplicationCloser extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
  }
}
