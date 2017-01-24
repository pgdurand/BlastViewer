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

import javax.swing.AbstractAction;
import javax.swing.Icon;

import epos.ui.view.treeview.renderer.ZoomMode;

/**
 * This class handles the ActionListener for the Zoom facility of the
 * PhyloPanel.
 * 
 * @author Patrick G. Durand
 */
public class PhyloZoomAction extends AbstractAction {
  private static final long serialVersionUID = 6485936372915913224L;
  private ZOOM_MODE _zoomMode;
  private ZoomMode  _zm;

  public static enum ZOOM_MODE {
    ZOOM_IN, ZOOM_OUT, ZOOM_FIT
  };

  /**
   * Zoom action.
   * 
   * @param name
   *          the name of the action.
   */
  public PhyloZoomAction(String name) {
    super(name);
  }

  /**
   * Zoom action.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public PhyloZoomAction(String name, Icon icon) {
    super(name, icon);
  }

  public void setZoomMode(ZOOM_MODE mode) {
    _zoomMode = mode;
  }

  public void initZoomSystem(ZoomMode zm) {
    _zm = zm;
  }

  public void actionPerformed(ActionEvent event) {
    switch (_zoomMode) {
      case ZOOM_IN:
        _zm.zoomIn();
        break;
      case ZOOM_OUT:
        _zm.zoomOut();
        break;
      case ZOOM_FIT:
        _zm.fullscreen();
        break;
    }
  }
}