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

import javax.swing.ImageIcon;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.filter.BFilter;
import bzh.plealog.bioinfo.data.searchresult.SRUtils;
import bzh.plealog.blastviewer.actions.api.BVActionImplem;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * This class implements the action to apply a filter on a Blast result.
 * 
 * @author Patrick G. Durand
 */
public class FilterEntryAction extends BVActionImplem {
  
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
  public FilterEntryAction(String name, String description, ImageIcon icon) {
    super(name, description, icon);
  }


  @Override
  public void execute(final SROutput sro, int iterationID, int[] selectedHits) {
    if (isRunning() || sro==null || sro.isEmpty())
      return;

    // get a filter from user
    BFilter filter = ActionUtils.getFilter();
    
    // dialog cancelled ?
    if (filter == null)
      return;
    lock(true);
    try {
      EZLogger.info(String.format(
          BVMessages.getString("FilterEntryAction.msg1"),
          filter.getTxtString()));

      //apply the filter
      SROutput sro_to_filter = SRUtils.extractResult(sro, iterationID);
      SROutput sro2 = filter.execute(sro_to_filter);

      // any results?
      if (sro2 == null || sro2.isEmpty()) {
        EZEnvironment.displayInfoMessage(EZEnvironment.getParentFrame(),
            BVMessages.getString("FilterEntryAction.msg2"));
        return;
      }
      // yes: open a new viewer
      BlastViewerOpener.displayInternalFrame(
          BlastViewerOpener.prepareViewer(sro2),
          BVMessages.getString("FilterEntryAction.header"), null);
    } finally {
      lock(false);
    }
  }
}