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
package bzh.plealog.blastviewer.actions.api;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileManager;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.implem.file.EZFileExtDescriptor;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.api.data.searchresult.io.SRWriter;
import bzh.plealog.bioinfo.data.searchresult.SRUtils;
import bzh.plealog.bioinfo.io.searchresult.SerializerSystemFactory;
import bzh.plealog.bioinfo.io.searchresult.csv.CSVExportSROutput;
import bzh.plealog.bioinfo.io.searchresult.txt.TxtExportSROutput;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * Utility class to export SRResult in various formats.
 * 
 * @author Patrick G. Durand
 */
public class BVGenericSaveUtils {
  private boolean _bestHitOnly = false;//CSV export only
  private boolean _firstHspOnly = false;//CSV export only
  private int _iterationID = -1;
  private SROutput _sro;
  
  /**
   * Constructor.
   * 
   * @param sro the SROutput to export
   */
  public BVGenericSaveUtils(SROutput sro) {
    _sro = sro;
  }
  /**
   * Constructor.
   * 
   * @param sro the SROutput to export
   * @param iterationID in case of SROutput containing many iterations, 
   * specify which one to export
   */
  public BVGenericSaveUtils(SROutput sro, int iterationID) {
    _sro = sro;
    _iterationID = iterationID;
  }
  /**
   * Constructor.
   * 
   * @param sro the SROutput to export
   * @param bestHitOnly export best hit only or not. Apply to CSV export only.
   * @param firstHspOnly export first hsp only or not. Apply to CSV export only.
   */
  public BVGenericSaveUtils(SROutput sro, boolean bestHitOnly, boolean firstHspOnly) {
    _sro = sro;
    _bestHitOnly = bestHitOnly;
    _firstHspOnly = firstHspOnly;
  }
  /**
   * Export data.
   */
  public void saveResult() {
    List<EZFileExtDescriptor> types = new ArrayList<EZFileExtDescriptor>();
    Arrays.asList(BVMessages.getString("SaveEntryAction.file.types").split(";"))
        .stream()
        .forEach(s -> types.add(new EZFileExtDescriptor(s.split(":")[0], s.split(":")[1])));
    
    // get a filter from user
    //File file = EZFileManager.chooseFileForSaveAction("Save");
    boolean currentState = EZFileManager.isUsingOSNativeFileDialog();
    EZFileManager.useOSNativeFileDialog(false);
    File file = EZFileManager.chooseFileForSaveAction(
        EZEnvironment.getParentFrame(), 
        BVMessages.getString("SaveEntryAction.dlg.title"), 
        null, 
        types);
    EZFileManager.useOSNativeFileDialog(currentState);
    // dialog cancelled ?
    if (file == null)
      return;
    
    try {
      EZEnvironment.setWaitCursor();
      //chooseFileForSaveAction() has forced file extension given one of the FileExtDescriptor
      String fName = file.getName();
      String fExt = fName.substring(fName.lastIndexOf('.')+1);
      SROutput sro_to_save;
      if (_iterationID>=0)
        sro_to_save = SRUtils.extractResult(_sro, _iterationID);
      else
        sro_to_save = _sro;
      SRWriter writer;
      //queries annotated with BCO is a particular feature, so handle that case
      int i, size = sro_to_save.countIteration();
      boolean hasQueryAnnotation = false;
      for (i = 0; i < size; i++) {// loop on iterations
        if (sro_to_save.getIteration(i).getIterationQueryFeatureTable()!=null){
          hasQueryAnnotation=true;
          break;
        }
      }
      switch(fExt){
      case "csv":
        //Use ArrayList specifically to avoid UnsuportedOperationException when
        //trying to update List with additional List of Integers (see below)
        ArrayList<Integer> columns = new ArrayList<>(Arrays.asList(new Integer[] {
            //note: query ID and description are always the two first columns
            TxtExportSROutput.ACCESSION, 
            TxtExportSROutput.DEFINITION, 
            TxtExportSROutput.ORGANISM, 
            TxtExportSROutput.LENGTH, 
            TxtExportSROutput.NBHSPS, 
            TxtExportSROutput.SCORE, 
            TxtExportSROutput.SCORE_BITS, 
            TxtExportSROutput.EVALUE, 
            TxtExportSROutput.ALI_LEN, 
            TxtExportSROutput.IDENTITY, 
            TxtExportSROutput.POSITIVE, 
            TxtExportSROutput.MISMATCHES,
            TxtExportSROutput.T_GAPS,//gaps count
            TxtExportSROutput.GAPS, //% gaps
            TxtExportSROutput.Q_FROM, 
            TxtExportSROutput.Q_TO,
            TxtExportSROutput.Q_GAPS, 
            TxtExportSROutput.Q_FRAME, 
            TxtExportSROutput.Q_COVERAGE, 
            TxtExportSROutput.H_FROM, 
            TxtExportSROutput.H_TO, 
            TxtExportSROutput.H_GAP, 
            TxtExportSROutput.H_FRAME,
            TxtExportSROutput.H_COVERAGE
        }));
        if (sro_to_save.getClassification()!=null) {
          if (hasQueryAnnotation) {
            columns.addAll( 
                0,
                Arrays.asList(new Integer[] {
                TxtExportSROutput.QUERY_BIO_CLASSIF_GO,
                TxtExportSROutput.QUERY_BIO_CLASSIF_IPR, 
                TxtExportSROutput.QUERY_BIO_CLASSIF_EC,
                TxtExportSROutput.QUERY_BIO_CLASSIF_PFM
            }));
          }
          columns.addAll(Arrays.asList(new Integer[] {
              TxtExportSROutput.TAXONOMY,
              TxtExportSROutput.BIO_CLASSIF_TAX, 
              TxtExportSROutput.BIO_CLASSIF_GO,
              TxtExportSROutput.BIO_CLASSIF_IPR, 
              TxtExportSROutput.BIO_CLASSIF_EC,
              TxtExportSROutput.BIO_CLASSIF_PFM
          }));
        }
        try(FileWriter fw = new FileWriter(file)){
          int[] coldIds = new int[columns.size()];
          i=0;
          for(Integer cid:columns) {
            coldIds[i]=cid; i++;
          }
          CSVExportSROutput csvExporter = new CSVExportSROutput();
          csvExporter.showBestHitOnly(_bestHitOnly);
          csvExporter.showFirstHspOnly(_firstHspOnly);
          csvExporter.ssetColumnIds(coldIds);
          csvExporter.export(fw, sro_to_save);
          fw.flush();
        }
        finally {}
        break;
      case "zml":
        writer = SerializerSystemFactory.getWriterInstance(SerializerSystemFactory.NATIVE_WRITER);
        writer.write(file, sro_to_save);
        break;
      case "xml":
        writer = SerializerSystemFactory.getWriterInstance(SerializerSystemFactory.NCBI_WRITER);
        writer.write(file, sro_to_save);
        break;
        default:
          throw new RuntimeException(
              BVMessages.getString("SaveEntryAction.err1")+": "+fExt);
      }
      
    }
    catch(Exception ex){
      EZLogger.warn(ex.toString());
      EZEnvironment.displayWarnMessage(
          EZEnvironment.getParentFrame(), 
          BVMessages.getString("SaveEntryAction.err2"));
    }
    finally {
      EZEnvironment.setDefaultCursor();
    }
  }
}
