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
package bzh.plealog.blastviewer.actions.hittable;

import java.awt.Dimension;

import javax.swing.JOptionPane;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;
import bzh.plealog.bioinfo.ui.filter.BFilterEditorListener;
import bzh.plealog.bioinfo.ui.filter.BFilterEntry;
import bzh.plealog.bioinfo.ui.filter.BFilterTable;
import bzh.plealog.bioinfo.ui.modules.filter.FilterManagerUI;
import bzh.plealog.bioinfo.ui.modules.filter.FilterSystemUI;

/**
 * Utility class to handle SROutput filtering.
 * 
 * @author Patrick G. Durand
 */
public class ActionUtils {

  /**
   * Open the FilterManager.
   * 
   * @return a filter or null if user cancelled the manager.
   */
  public static BFilter getFilter() {
    // create a new filter viewer
    BFilterTable fTable = new BFilterTable(
        FilterSystemConfigurator.getFilterableModel(), true, true, false);
    BFilterEditorListener listener = new BFilterEditorListener();
    fTable.addBFilterEditListener(listener);
    fTable.setPreferredSize(new Dimension(800, 250));
    
    // fill in the filter data model with existing filters
    FilterManagerUI.uploadExistingFilters(fTable,
        FilterSystemUI.getFilterCentralRepositoryPath());

    // display the FilterManager
    int ret = JOptionPane.showOptionDialog(EZEnvironment.getParentFrame(),
        fTable, EZApplicationBranding.getAppName(),
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
        null);
    fTable.removeBFilterEditListener(listener);
    
    // return a filter or null
    if (ret != JOptionPane.OK_OPTION) {
      return null;
    } else {
      BFilterEntry[] selEntries = fTable.getSelectedEntries();
      if (selEntries == null)
        return null;
      return selEntries[0].getFilter();
    }
  }

}
