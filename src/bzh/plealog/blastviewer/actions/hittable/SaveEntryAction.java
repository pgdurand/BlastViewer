/* Copyright (C) 2003-2020 Patrick G. Durand
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

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.blastviewer.actions.api.BVActionImplem;
import bzh.plealog.blastviewer.actions.api.BVGenericSaveUtils;

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
    if (isRunning() || sro==null || sro.isEmpty())
      return;
    lock(true);
    BVGenericSaveUtils bsu = new BVGenericSaveUtils(sro, iterationID);
    bsu.saveResult();
    lock(false);

  }
}