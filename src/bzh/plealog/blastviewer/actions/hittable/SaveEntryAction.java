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
package bzh.plealog.blastviewer.actions.hittable;

import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.io.SRWriter;
import bzh.plealog.bioinfo.io.searchresult.SerializerSystemFactory;
import bzh.plealog.blastviewer.actions.api.BVActionImplem;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;

/**
 * This class implements the action to save a Blast result in a new file.
 * 
 * @author Patrick G. Durand
 */
public class SaveEntryAction extends BVActionImplem {
  
  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   * @param description
   *          the description of the action.
   * @param icon
   *          the icon of the action.
   */
  public SaveEntryAction(String name, String description, ImageIcon icon) {
    super(name, description, icon);
  }

  @Override
  public void execute(final SROutput sro, int iterationID, int[] selectedHits) {
    if (isRunning())
      return;

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // get a filter from user
        File file = EZFileManager.chooseFileForSaveAction("Save");
        
        // dialog cancelled ?
        if (file == null)
          return;
        
        // start saving
        lock(true);
        try {
          SRWriter writer = SerializerSystemFactory.getWriterInstance(SerializerSystemFactory.NCBI_WRITER);
          writer.write(file, sro);
        }
        catch(Exception ex){
          EZLogger.warn(ex.toString());
          EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), "Unable to save results.");
        }
        finally {
          lock(false);
        }
      }
    }); 
  }
}