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

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the BlastViewer ActionManager.
 * 
 * @author Patrick G. Durand.
 */
public class BVActionManager {

  private static ArrayList<BVAction> actions = new ArrayList<>();
  
  /**
   * Add an action.
   * 
   * @param act an action
   */
  public static void addAction(BVAction act){
    actions.add(act);
  }
  
  /**
   * Return the list of actions.
   * 
   * @return a list of actions.
   */
  public static List<BVAction> getActions(){
    return actions;
  }
}
