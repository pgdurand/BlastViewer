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
import java.util.HashMap;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;
import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchresult.SRHit;
import bzh.plealog.bioinfo.api.data.searchresult.SRHsp;
import bzh.plealog.bioinfo.api.data.searchresult.SRIteration;
import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.io.searchresult.csv.AnnotationDataModel;
import bzh.plealog.bioinfo.io.searchresult.csv.ExtractAnnotation;
import bzh.plealog.bioinfo.io.searchresult.txt.TxtExportSROutput;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This class implements the action to prepare BCO viewer.
 * 
 * @author Patrick G. Durand
 */
public class PrepareBCOViewerAction extends AbstractAction {
  private static final long serialVersionUID = -3984245135396746453L;
  private QueryBase _qb;
  private boolean _running = false;
  private boolean _firstHspOnly = false;
  private boolean _bestHitOnly = false;

  /**
   * Action constructor.
   * 
   * @param name
   *          the name of the action.
   */
  public PrepareBCOViewerAction(String name) {
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
  public PrepareBCOViewerAction(String name, Icon icon) {
    super(name, icon);
  }

  /**
   * Set data to process.
   */
  public void setQuery(QueryBase sro) {
    _qb = sro;
  }
  
  public void processAnnotation(SROutput bo){
    TreeMap<String, TreeMap<AnnotationDataModelConstants.ANNOTATION_CATEGORY, HashMap<String, AnnotationDataModel>>> annotatedHitsHashMap = null;
    TreeMap<AnnotationDataModelConstants.ANNOTATION_CATEGORY, TreeMap<String, AnnotationDataModel>> annotationDictionary = null;

    // Extract Bio Classification: IPR, EC, GO and TAX
    annotatedHitsHashMap = new TreeMap<String, TreeMap<AnnotationDataModelConstants.ANNOTATION_CATEGORY, HashMap<String, AnnotationDataModel>>>();
    annotationDictionary = new TreeMap<AnnotationDataModelConstants.ANNOTATION_CATEGORY, TreeMap<String, AnnotationDataModel>>();
    ExtractAnnotation.buildAnnotatedHitDataSet(bo, 0, annotatedHitsHashMap, annotationDictionary);
    
    int i, j, k, size, size2, size3;
    SRIteration iteration;
    SRHit hit;
    SRHsp hsp;
    String s;
    // Loop over each Hit and get Bio Classification data
    size = bo.countIteration();
    for (i = 0; i < size; i++) {// loop on iterations
      iteration = bo.getIteration(i);
      size2 = iteration.countHit();
      for (j = 0; j < size2; j++) {// loop on hits
        hit = iteration.getHit(j);
        System.out.println("> " + hit.getHitId());
        size3 = hit.countHsp();
        for (k = 0; k < size3; k++) {// loop on hsp
          hsp = hit.getHsp(k);
          s = TxtExportSROutput.getFormattedData(
              annotatedHitsHashMap, iteration, hit, hsp, TxtExportSROutput.BIO_CLASSIF_TAX, false, false);
          System.out.println("   " + s);
          s = TxtExportSROutput.getFormattedData(
              annotatedHitsHashMap, iteration, hit, hsp, TxtExportSROutput.BIO_CLASSIF_GO, false, false);
          System.out.println("   " + s);
          s = TxtExportSROutput.getFormattedData(
              annotatedHitsHashMap, iteration, hit, hsp, TxtExportSROutput.BIO_CLASSIF_EC, false, false);
          System.out.println("   " + s);
          s = TxtExportSROutput.getFormattedData(
              annotatedHitsHashMap, iteration, hit, hsp, TxtExportSROutput.BIO_CLASSIF_IPR, false, false);
          System.out.println("   " + s);
          if (_firstHspOnly) {
            break;
          }
        }
        if (_bestHitOnly) {
          break;
        }
      }
    }
    
    //do some cleaning
    if (annotatedHitsHashMap != null)
      annotatedHitsHashMap.clear();
    if (annotationDictionary != null)
      annotationDictionary.clear();
  }

  private void doAction() {
    if (_running || _qb==null || _qb.getSummaries().hasMoreElements()==false)
      return;

    _running = true;
    
    int i, size = _qb.sequences();
    SROutput sro;
    long tim = System.currentTimeMillis();
    System.out.println("start");
    for(i=0;i<size;i++) {
      sro = _qb.getResult(i);
      processAnnotation(sro);
    }
    System.out.println(System.currentTimeMillis()-tim);
  }

  public void actionPerformed(ActionEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          doAction();
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("SaveFileAction.err")
              + t.toString());
        } finally {
          _running = false;
          EZEnvironment.setDefaultCursor();
        }
      }
    });
  }

}
