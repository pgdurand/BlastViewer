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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;
import bzh.plealog.blastviewer.BlastSummaryViewerController;

/**
 * A table aims at displaying classification data contained in the results of a QueryBase.
 * 
 * @author Patrick G. Durand
 */
public class QueryOverviewSRCDataTable extends JTable {
  private static final long serialVersionUID = 6338612364355659306L;
	private static Color	lblForegroundOk		= UIManager.getColor("Label.foreground");
	private static Icon		checkIcon			= EZEnvironment.getImageIcon("check_white.png");

	static {
		if (!Color.WHITE.equals(lblForegroundOk)) {
			// the black one
			checkIcon = EZEnvironment.getImageIcon("check.png");
		}
	}

	private Color			lblForegroundNok	= null;
	private BlastSummaryViewerController _bvController;
	
	/**
	 * Constructor.
	 * 
	 * @param background color used to display labels
	 * @param tableModel the data model
	 */
	public QueryOverviewSRCDataTable(Color background, QueryOverviewSRCDataTableModel tableModel, 
	    BlastSummaryViewerController bvController) {
		super(tableModel);
		_bvController = bvController;
		this.setBackground(background);
		this.getColumnModel().getColumn(0).setPreferredWidth(200);
		this.getColumnModel().getColumn(1).setPreferredWidth(30);
		this.addMouseListener(new OpenClassificationListener());
		this.setRowSorter(null);
		this.setRowSelectionAllowed(false);
		this.setShowGrid(false);
		this.lblForegroundNok = background.brighter().brighter();

	}

	@SuppressWarnings("serial")
  @Override
	public TableCellRenderer getCellRenderer(int row, int column) {

		if (column == 0) {
			DefaultTableCellRenderer result = new BorderLessTableCellRenderer();
			if (!((QueryOverviewSRCDataTableModel) getModel()).containsClassificationData(row)) {
				result.setForeground(lblForegroundNok);
			} else {
				result.setForeground(lblForegroundOk);
				QueryOverviewPanel.setClickableLabel(result);
			}
			return result;
		}
		if (column == 1) {
			return new DefaultTableCellRenderer() {
				@Override
				protected void setValue(Object value) {
					if (value instanceof Boolean) {
						if ((Boolean) value.equals(Boolean.TRUE)) {
							setIcon(checkIcon);
						}
					}
				}
			};
		}
		return new DefaultTableCellRenderer();
	}

	private class OpenClassificationListener extends MouseAdapter {
		@Override
		public void mouseClicked(java.awt.event.MouseEvent evt) {
			int row = rowAtPoint(evt.getPoint());
			int col = columnAtPoint(evt.getPoint());
			if (row >= 0 && col >= 0) {
				if (((QueryOverviewSRCDataTableModel) getModel()).containsClassificationData(row)) {
				  _bvController.showClassification(AnnotationDataModelConstants.EXTENDED_FEATURE_INDEX_LABELS[row]);
				}
			}
		}
	}

	@SuppressWarnings("serial")
  private static class BorderLessTableCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(final JTable table, final Object value, 
		    final boolean isSelected, final boolean hasFocus, final int row, final int col) {
			final Component c = super.getTableCellRendererComponent(
			    table, value, isSelected, 
			    false, // never has focus to disable the HighLight border
					row, col);
			return c;
		}
	}
}
