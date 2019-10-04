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
package bzh.plealog.blastviewer.actions.summary;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.ui.common.CheckBoxChooserDialog;
import com.plealog.genericapp.ui.common.CheckBoxModelItem;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;
import bzh.plealog.bioinfo.api.data.searchjob.SRTermSummary;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTable;
import bzh.plealog.bioinfo.ui.blast.resulttable.SummaryTableModel;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This class implements the action to select which classification to show in the Viewer.
 * 
 * @author Patrick G. Durand
 */
public class ChooseClassificationAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;
  private SummaryTable _table;
  private boolean _running = false;
  
  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public ChooseClassificationAction(String name) {
    super(name);
  }

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   * @param icon
   *          the icon of the action.
   */
  public ChooseClassificationAction(String name, Icon icon) {
    super(name, icon);
  }

  /**
   * Set data object.
   */
  public void setTable(SummaryTable table) {
    _table = table;
  }
  
  private void doAction() {
    if (_running)
      return;

    _running = true;
    
    ArrayList<CheckBoxModelItem> classifs = new ArrayList<>();
    SummaryTableModel model = (SummaryTableModel) _table.getModel();
    CheckBoxModelItem item;
    String code;
    
    //prepare the full fill of Classifications supported by the Viewer
    List<String> currentClassifs = model.getClassificationsToView();
    //Loop over Classifications
    for(AnnotationDataModelConstants.ANNOTATION_CATEGORY cat : 
      AnnotationDataModelConstants.ANNOTATION_CATEGORY.values()) {
      if (cat.equals(AnnotationDataModelConstants.ANNOTATION_CATEGORY.TAX) ||
          cat.equals(AnnotationDataModelConstants.ANNOTATION_CATEGORY.LCA)) {
        //we do not handle these classifications types here
        continue;
      }
      else if(cat.equals(AnnotationDataModelConstants.ANNOTATION_CATEGORY.GO)){
        //GO special : handle sub-category (P, C, F)
        for (AnnotationDataModelConstants.ANNOTATION_GO_SUBCATEGORY subcat : 
          AnnotationDataModelConstants.ANNOTATION_GO_SUBCATEGORY.values()) {
          code = SRTermSummary.formatViewType(
              AnnotationDataModelConstants.ANNOTATION_CATEGORY.GO.getType(), 
              subcat.getType());
          item = new CheckBoxModelItem(code, cat.getDescription()+": "+subcat.getDescription(), currentClassifs.contains(code));
          classifs.add(item);
        }
      }
      else {
        item = new CheckBoxModelItem(cat.getType(), cat.getDescription(), currentClassifs.contains(cat.getType()));
        classifs.add(item);
      }
    }
    
    //setup dialogue box
    CheckBoxChooserDialog dlg = new CheckBoxChooserDialog(EZEnvironment.getParentFrame(), 
        BVMessages.getString("ChooseClassificationAction.dialog.header"), classifs);
    
    dlg.showDlg();
    
    if (dlg.isCancelled()) {
      return;
    }
    
    //apply new classification filters
    currentClassifs = new ArrayList<>();
    for(CheckBoxModelItem c : classifs) {
      if (c.isSelected()) {
        currentClassifs.add(c.getCode());
      }
    }
    _table.setClassificationsToView(currentClassifs);
  }

  public void actionPerformed(ActionEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("ChooseClassificationAction.err")
              + t.toString());
        } finally {
          _running = false;
          EZEnvironment.setDefaultCursor();
        }
      }
    });
  }

}
