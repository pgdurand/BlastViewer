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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.plealog.genericapp.api.EZEnvironment;

import bzh.plealog.bioinfo.api.data.feature.AnnotationDataModelConstants;
import bzh.plealog.bioinfo.api.data.searchjob.QueryBase;
import bzh.plealog.bioinfo.api.data.searchjob.SJFileSummary;
import bzh.plealog.bioinfo.api.data.searchjob.SJTermSummary;
import bzh.plealog.bioinfo.api.data.searchresult.SRCTerm;
import bzh.plealog.bioinfo.api.data.searchresult.SRClassificationCount;
import bzh.plealog.bioinfo.api.data.searchresult.SRClassificationCountTerm;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * Utility class to contro the content of SRCOverviewPanel.
 * 
 * @author Patrick G. Durand
 */
public class SRCOverviewControllerPanel extends JPanel {
  private static final long serialVersionUID = -5833282402986985497L;

  private QueryBase         _query;
  private SRCOverviewPanel   _parentPnl;
  private JComboBox<String> _jcomboFeatureIndexes = null;
  private JButton           _jbtnQuery = null;

  private static final String JBUTTON_REFRESH_LABEL = 
      BVMessages.getString("SRCOverviewPanel.lbl18");
  private static final String JBUTTON_REFRESH_TOOLTIP_LABEL = 
      BVMessages.getString("SRCOverviewPanel.lbl33");

  private static final String DEFAULT_FONT_NAME = "Arial";
  private static final Font DEFAULT_FONT = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 11);
  private static final Font DEFAULT_FONT_MAC = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 9);
  private static final Font DEFAULT_FONT_LINUX = new Font(DEFAULT_FONT_NAME, Font.PLAIN, 11);
  
  /**
   * Constructor.
   */
  public SRCOverviewControllerPanel() {
    createUI();
  }

  /**
   * Set the data to display.
   * 
   * @param query a QueryBase object
   */
  public void setQuery(QueryBase query) {
    _query = query;
    _jbtnQuery.setEnabled(_query == null ? false : true);
  }
  
  /**
   * Connect this component to its parent.
   * 
   * @param pnl parent component
   * */
  public void setParentPanel(SRCOverviewPanel pnl) {
    _parentPnl = pnl;
  }
  
  /**
   * Apply of font to a component
   * 
   * @param component the component to apply new font
   */
  protected static void setFontSize(Component component) {
    setFontSize(component, DEFAULT_FONT_MAC, DEFAULT_FONT_LINUX, DEFAULT_FONT);
  }

  /**
   * Apply of font to a component
   * 
   * @param component the component to apply new font
   * @param fontMac macOS specific font
   * @param fontLinux Linux specific font
   * @param fontDefault Windows specific font
   */
  protected static void setFontSize(Component component, Font fontMac, Font fontLinux, Font fontDefault) {
    if (EZEnvironment.getOSType() == EZEnvironment.MAC_OS) {
      component.setFont(fontMac);
    } else if (EZEnvironment.getOSType() == EZEnvironment.LINUX_OS) {
      component.setFont(fontLinux);
    } else {
      component.setFont(fontDefault);
    }
  }

  /**
   * Create the UI.
   */
  private void createUI() {
    _jcomboFeatureIndexes = new JComboBox<String>(AnnotationDataModelConstants.EXTENDED_FEATURE_INDEX_LABELS);
    _jcomboFeatureIndexes.setSelectedIndex(0);

    _jbtnQuery = new JButton(JBUTTON_REFRESH_LABEL);
    _jbtnQuery.addActionListener(new RefreshActionListener());
    _jbtnQuery.setToolTipText(JBUTTON_REFRESH_TOOLTIP_LABEL);
    _jbtnQuery.setEnabled(false);

    setFontSize(_jcomboFeatureIndexes);
    setFontSize(_jbtnQuery);

    JPanel pnl = new JPanel(new BorderLayout());
    pnl.add(_jcomboFeatureIndexes, BorderLayout.CENTER);
    pnl.add(_jbtnQuery, BorderLayout.EAST);
    this.setLayout(new BorderLayout());
    this.add(pnl, BorderLayout.WEST);
    this.setBorder(BorderFactory.createEmptyBorder(0, 15, 5, 0));
  }

  /**
   * Show particular classification.
   * 
   * @param vType one of  AnnotationDataModelConstants.CLASSIF_NAME_TO_CODE keys
   */
  public void showClassification(String vType) {
    _jcomboFeatureIndexes.setSelectedItem(vType);
    showClassification();
  }
  
  /**
   * Show a particular classification.
   */
  private void showClassification() {
    //build counting classification for best hits only, 
    //then add predictions on queries, if any
    Enumeration<SJFileSummary> summaries = _query.getSummaries();
    SRClassificationCount cClassification = new SRClassificationCount();
    Enumeration<String> ids;
    String id;
    SRCTerm term;
    while(summaries.hasMoreElements()) {
      SJFileSummary summary = summaries.nextElement();
      //Hit data
      if(summary.getHitClassification()!=null) {
        ids = summary.getHitClassification().getTermIDs();
        while(ids.hasMoreElements()) {
          id = ids.nextElement();
          term = summary.getHitClassification().getTerm(id);
          if ( ! term.getType().equals(SRCTerm.FAKE_TERM) ) {
            //then discard FAKE terms (those making path of Terms associated to hits)
            cClassification.addClassification(new SJTermSummary(id, term));
          }
        }
      }
      //Query data (if InterproScan data imported somewhere)
      if (summary.getQueryClassification()!=null) {
        ids = summary.getQueryClassification().getTermIDs();
        while(ids.hasMoreElements()) {
          id = ids.nextElement();
          term = summary.getQueryClassification().getTerm(id);
          if ( ! term.getType().equals(SRCTerm.FAKE_TERM) ) {
            //then discard FAKE terms (those making path of Terms associated to hits)
            cClassification.addClassification(new SJTermSummary(id, term));
          }
        }
      }
    }

    //get classification type to display
    Object classifType = _jcomboFeatureIndexes.getSelectedItem();
    String vType = AnnotationDataModelConstants.CLASSIF_NAME_TO_CODE.get(classifType.toString());
    AnnotationDataModelConstants.ANNOTATION_CATEGORY cat = 
        AnnotationDataModelConstants.CLASSIF_INDEX_TYPE.get(classifType.toString()); 
    Hashtable<String, SRClassificationCountTerm> classif = 
        cClassification.getBCCountClassification(vType);
    cClassification.finalizeCounting();

    SRCOverviewTableModel model = null;
    if (classif.isEmpty()==false) {
      //Order classification terms by occurrences
      List<SRClassificationCountTerm> terms = classif.entrySet().stream()
          .sorted((e1, e2) -> 0-e1.getValue().compareTo(e2.getValue())) 
          .map(e1 -> e1.getValue())
          .collect(Collectors.toList());
      //Setup component model
      model = new SRCOverviewTableModel(terms, cat);
    }
    
    //update view
    _parentPnl.updateModel(model);
  }
  
  private class RefreshActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      showClassification();
    }
  }
}
