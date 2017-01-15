/* Copyright (C) 2003-2017 Patrick G. Durand
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
package bzh.plealog.blastviewer.actions.api;

import javax.swing.ImageIcon;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;

/**
 * Defines a BlastViewer action.
 * 
 * @author Patrick G. Durand.
 */
public interface BVAction {
  /**
   * 
   * Figurs out whether or not this action is already running.
   * 
   * @return true if running, false otherwise.
   */
  public boolean isRunning();

  /**
   * Return the icon of this action.
   * 
   * @return an icon.
   */
  public ImageIcon getIcon();

  /**
   * Return the name of this action.
   * 
   * @return a name.
   */
  public String getName();

  /**
   * Return the description of this action.
   * 
   * @return a description.
   */
  public String getDescription();

  /**
   * Execute this action.
   * 
   * @param sro
   *          the Blast result
   * @param iterationID
   *          the iteration ordering number currently displayed in the
   *          BlastViewer.
   * @param selectedHits
   *          selected hit ordering numbers
   */
  public void execute(SROutput sro, int iterationID, int[] selectedHits);
}
