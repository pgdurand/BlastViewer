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

/**
 * Defines a generic implementation of a BlastViewer action.
 * 
 * @author Patrick G. Durand.
 */
public abstract class BVActionImplem implements BVAction {
  private ImageIcon _icon;
  private String _name;
  private String _description;
  private boolean _systemLocked;
  
  @SuppressWarnings("unused")
  private BVActionImplem() {
  }

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public BVActionImplem(String name) {
    _name = name;
  }

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public BVActionImplem(String name, ImageIcon icon) {
    _name = name;
    _icon = icon;
  }
  
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
  public BVActionImplem(String name, String description, ImageIcon icon) {
    _name = name;
    _icon = icon;
    _description = description;
  }

  public synchronized void lock(boolean lock){
    _systemLocked = lock;
  }
  
  @Override
  public synchronized boolean isRunning(){
    return _systemLocked;
  }
  @Override
  public ImageIcon getIcon() {
    return _icon;
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public String getDescription() {
    return _description;
  }
}
