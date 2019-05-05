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
package bzh.plealog.blastviewer.util;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.ui.desktop.GDesktopPane;
import com.plealog.genericapp.ui.desktop.GInternalFrame;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.io.SRLoader;
import bzh.plealog.bioinfo.io.searchresult.SerializerSystemFactory;
import bzh.plealog.blastviewer.BlastSummaryViewerPanel;
import bzh.plealog.blastviewer.BlastViewerPanel;

/**
 * Utility class to enable interaction with the GDesktopPane.
 * 
 * @author Patrick G. Durand
 */
public class BlastViewerOpener {
  private static GDesktopPane _desktop;
  private static JLabel       _helperField;

  public static ImageIcon     WORKING_ICON       = EZEnvironment
                                                     .getImageIcon("circle_all.gif");
  private static Color        RUNNING_TASK_COLOR = Color.GREEN.darker();
  private static Color        NOT_RUNNING_TASK_COLOR;

  /**
   * Register the desktop to this component.
   */
  public static void setDesktop(GDesktopPane desktop) {
    _desktop = desktop;
  }

  /**
   * Return the help field.
   */
  public static JComponent getHelperField() {
    if (_helperField == null) {
      _helperField = new JLabel();
      _helperField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      _helperField.setOpaque(true);
      _helperField.setFocusable(false);
      NOT_RUNNING_TASK_COLOR = _helperField.getBackground();
    }
    return _helperField;
  }

  /**
   * Set a message during some operation.
   */
  public static void setHelperMessage(String msg) {
    if (msg == null) {
      cleanHelperMessage();
    }
    _helperField.setText(msg);
    _helperField.setIcon(WORKING_ICON);
    _helperField.setBackground(RUNNING_TASK_COLOR);
  }

  /**
   * Clear the message.
   */
  public static void cleanHelperMessage() {
    _helperField.setText("");
    _helperField.setIcon(null);
    _helperField.setBackground(NOT_RUNNING_TASK_COLOR);
  }

  /**
   * Load a BLAST XML data file.
   * 
   * @param f a file. Must be a Blast legacy XML file or a ZML one.
   * 
   * @return a SROutput object
   */
  public static SROutput readBlastFile(File f) {
    
    SROutput sro = null;
    
    SRLoader ncbiBlastLoader = SerializerSystemFactory
      .getLoaderInstance(SerializerSystemFactory.NCBI_LOADER);
    if (ncbiBlastLoader.canRead(f)){
      sro = ncbiBlastLoader.load(f);
      return sro;
    }
    
    SRLoader nativeBlastLoader = SerializerSystemFactory
        .getLoaderInstance(SerializerSystemFactory.NATIVE_LOADER);
    if (nativeBlastLoader.canRead(f)){
      sro = nativeBlastLoader.load(f);
      return sro;
    }
    
    return null;
  }

  /**
   * Prepare a new instance of a Blast Viewer panel.
   * 
   * @param data a Blast result
   * 
   * @return a JComponent object
   */
  public static JComponent prepareViewer(SROutput data){
    if (data.getBlastType()==SROutput.PSIBLAST || data.countIteration()==1) {
      BlastViewerPanel viewer = new BlastViewerPanel();
      viewer.setContent(data);
      return viewer;
    }
    else {
      BlastSummaryViewerPanel viewer = new BlastSummaryViewerPanel();
      viewer.setContent(data);
      return viewer;
    }
  }
  
  /**
   * Add a new BlastViewer to this desktop.
   * 
   * @param viewer the viewer to add. Should be a Blast Viewer component.
   * Cannot be null.
   * @param title the internal frame title. Cannot be null.
   * @param icon the internal frame icon. Can be null.
   */
  public static void displayInternalFrame(JComponent viewer, String title,
      ImageIcon icon) {
    int delta = 20;

    GInternalFrame iFrame = new GInternalFrame(viewer, // the viewer
        title, // iFrame title will be the entry ID
        true, true, true, // resizable, closable, maximizable: allowed
        false);// does not allow iconifiable: not working with JRE1.7+ on OSX !
               // Known bug.
    if (icon != null)
      iFrame.setFrameIcon(icon);
    Dimension dim = _desktop.getSize();
    iFrame.setVisible(false);
    _desktop.addGInternalFrame(iFrame);
    iFrame.setSize(dim);
    iFrame.setBounds(delta, delta, dim.width - 2 * delta, dim.height - 2
        * delta);
    // for future use...
    // iFrame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
    // iFrame.addInternalFrameListener(new IFrameListener(navigator));
    iFrame.setVisible(true);
  }

  /**
   * Add a new BlastViewer to this desktop.
   */
  public static void displayInternalFrame(JComponent viewer) {
    displayInternalFrame(viewer, "Blast Results", null);
  }

}
