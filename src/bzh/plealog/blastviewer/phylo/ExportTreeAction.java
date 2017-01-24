/* Copyright (C) 2008-2017 Patrick G. Durand
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
package bzh.plealog.blastviewer.phylo;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import bzh.plealog.blastviewer.config.FileExtension;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileFilter;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.file.EZFileTypes;
import com.plealog.genericapp.implem.file.EZFileExtDescriptor;

import epos.model.tree.Tree;
import epos.model.tree.io.NewickExporter;
import epos.model.tree.io.NexusExporter;

/**
 * This class contains the action to save a Tree using Nexus or Newick format.
 * 
 * @author Patrick G. Durand
 */
public class ExportTreeAction extends AbstractAction {
  private static final long serialVersionUID = -8440499126088963433L;

  private Tree              _tree;

  /**
   * Export constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public ExportTreeAction(String name) {
    super(name);
  }

  /**
   * Export constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public ExportTreeAction(String name, Icon icon) {
    super(name, icon);
  }

  public void setTree(Tree t) {
    _tree = t;
  }

  private void doJob() {
    ArrayList<EZFileExtDescriptor> types;
    EZFileFilter dff;
    File f;
    NexusExporter nexExporter;
    NewickExporter newExporter;

    if (_tree == null)
      return;
    // we provide to export formats: Newick and Nexus
    types = new ArrayList<EZFileExtDescriptor>();
    dff = EZFileTypes.getFileFilter(FileExtension.NEX_FEXT);
    types.add(new EZFileExtDescriptor(FileExtension.NEX_FEXT, dff
        .getDescription()));
    dff = EZFileTypes.getFileFilter(FileExtension.NEW_FEXT);
    types.add(new EZFileExtDescriptor(FileExtension.NEW_FEXT, dff
        .getDescription()));
    
    // ask the user for a file name and path as well as the format
    f = EZFileManager.chooseFileForSaveAction(EZEnvironment.getParentFrame(),
        BVMessages.getString("DDFileTypes.tree.dlg.header"),
        EZFileTypes.getFileFilter(FileExtension.NEX_FEXT), types);
    // cancel?
    if (f == null)
      return;

    try {
      String fext = EZFileFilter.getExtension(f);
      if (fext == null || fext.equals(FileExtension.NEX_FEXT)) {
        nexExporter = new NexusExporter();
        nexExporter.setFile(f);
        nexExporter.setTrees(new Tree[] { _tree });
        nexExporter.write();
      } else {
        newExporter = new NewickExporter();
        newExporter.setFile(f);
        newExporter.setTrees(new Tree[] { _tree });
        newExporter.write();
      }
    } catch (Exception e) {
      String msg = BVMessages.getString("DDFileTypes.tree.err.msg1");
      EZEnvironment.displayErrorMessage(EZEnvironment.getParentFrame(), msg
          + ".");
    }

  }

  public void actionPerformed(ActionEvent event) {
    // OS-dependent File Choosers do not always
    // enable the use of FileExtension chooser. So we bypass these natives
    // Choosers and use default Java ones.
    EZFileManager.useOSNativeFileDialog(false);
    doJob();

    // for the rest of the software, we still use OS-dependent File Choosers
    EZFileManager.useOSNativeFileDialog(true);
  }
}
