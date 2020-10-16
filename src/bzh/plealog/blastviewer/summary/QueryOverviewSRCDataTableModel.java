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

import java.util.HashMap;

import javax.swing.table.DefaultTableModel;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;

/**
 * Data model for the QueryOverviewSRCDataTable component.
 * 
 * @author Patrick G. Durand
 */
public class QueryOverviewSRCDataTableModel extends DefaultTableModel {
  private static final long serialVersionUID = 1L;

  private HashMap<String, Boolean>	classificationAvailable	= new HashMap<String, Boolean>();

	/**
	 * Constructor.
	 */
	public QueryOverviewSRCDataTableModel() {
		super(AnnotationDataModelConstants.EXTENDED_FEATURE_INDEX_LABELS.length, 2);

		// default value to false for classif available
		for (String classifName : AnnotationDataModelConstants.EXTENDED_FEATURE_INDEX_LABELS) {
			this.classificationAvailable.put(classifName, false);
		}
	}

	/**
	 * Classification is contained in a result.
	 * 
	 * @param classificationName use entries from EXTENDED_FEATURE_INDEX_LABELS
	 */
	public void setClassificationAvailable(String classificationName) {
		this.classificationAvailable.put(classificationName, true);
	}

  /**
   * Check that a classification is contained in a result.
   * 
   * @param row index of classification in the table
   * 
   *  @return true if classification is set to on, false otherwise.
   */
	public boolean containsClassificationData(int row) {
		return this.classificationAvailable.get(AnnotationDataModelConstants.EXTENDED_FEATURE_INDEX_LABELS[row]);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column == 0) {
			return AnnotationDataModelConstants.EXTENDED_FEATURE_INDEX_LABELS[row];
		}
		if (column == 1) {
			return containsClassificationData(row);
		}
		return null;
	}
}
