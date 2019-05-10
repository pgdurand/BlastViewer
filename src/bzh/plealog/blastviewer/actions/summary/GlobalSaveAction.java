/* Copyright (C) 2003-2019 Patrick G. Durand
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
package bzh.plealog.blastviewer.actions.summary;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.io.SRWriter;
import bzh.plealog.bioinfo.io.searchresult.SerializerSystemFactory;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This class implements the action to save an entire SROutput from
 * SummaryViewer.
 * 
 * @author Patrick G. Durand
 */
public class GlobalSaveAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;
  private SROutput _sro;
  private boolean _running = false;
  
  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public GlobalSaveAction(String name) {
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
  public GlobalSaveAction(String name, Icon icon) {
    super(name, icon);
  }

  /**
   * Set data to save.
   */
  public void setResult(SROutput sro) {
    _sro = sro;
  }
  
  private void doAction() {
    if (_running || _sro==null || _sro.isEmpty())
      return;

    _running = true;
    
    // get a filter from user
    File file = EZFileManager.chooseFileForSaveAction(BVMessages.getString("SaveFileAction.lbl"));
    
    // dialog cancelled ?
    if (file == null)
      return;

    SRWriter writer = SerializerSystemFactory.getWriterInstance(SerializerSystemFactory.NCBI_WRITER);
    writer.write(file, _sro);
  }

  public void actionPerformed(ActionEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("SaveFileAction.err")
              + t.toString());
        } finally {
          _running = false;
          EZEnvironment.setDefaultCursor();
        }
      }
    });
  }

}
