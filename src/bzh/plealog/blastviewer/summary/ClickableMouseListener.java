/* Copyright (C) 2020 Patrick G. Durand
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
package bzh.plealog.blastviewer.summary;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Utility class to handle cursor look on QueryOverviewPanel components.
 * 
 * @author Patrick G. Durand
 */
public class ClickableMouseListener implements MouseListener {
  public static final Cursor    NORMAL_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
  public static final Cursor    HAND_CURSOR   = new Cursor(Cursor.HAND_CURSOR);

	@Override
	public void mouseExited(MouseEvent e) {
		e.getComponent().setCursor(NORMAL_CURSOR);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		e.getComponent().setCursor(HAND_CURSOR);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

}
