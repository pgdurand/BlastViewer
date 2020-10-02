/* Copyright (C) 2003-2020 Patrick G. Durand
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
package bzh.plealog.blastviewer.actions.hittable;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;

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
import bzh.plealog.blastviewer.actions.api.BVActionImplem;
import bzh.plealog.blastviewer.resources.BVMessages;

/**
 * This class implements the action to save a Blast result in a new file.
 * 
 * @author Patrick G. Durand
 */
public class SaveEntryAction extends BVActionImplem {
  
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
  public SaveEntryAction(String name, String description, ImageIcon icon) {
    super(name, description, icon);
  }

  @Override
  public void execute(final SROutput sro, int iterationID, int[] selectedHits) {
    if (isRunning() || sro==null || sro.isEmpty())
      return;
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
    
    // start saving
    lock(true);
    try {
      //chooseFileForSaveAction() has forced file extension given one of the FileExtDescriptor
      String fName = file.getName();
      String fExt = fName.substring(fName.lastIndexOf('.')+1);
      SROutput sro_to_save = SRUtils.extractResult(sro, iterationID);
      SRWriter writer;
      
      switch(fExt){
      case "csv":
        List<Integer> columns = Arrays.asList(new Integer[] {
            //note: query ID and description are always the two first columns
            TxtExportSROutput.HIT_NUM,
            TxtExportSROutput.ACCESSION, 
            TxtExportSROutput.DEFINITION, 
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
        });
        if (sro.getClassification()!=null) {
          columns.addAll(Arrays.asList(new Integer[] {TxtExportSROutput.TAXONOMY,
              TxtExportSROutput.BIO_CLASSIF, 
              TxtExportSROutput.BIO_CLASSIF_TAX, 
              TxtExportSROutput.BIO_CLASSIF_GO,
              TxtExportSROutput.BIO_CLASSIF_IPR, 
              TxtExportSROutput.BIO_CLASSIF_EC
          }));
        }
        try(FileWriter fw = new FileWriter(file)){
          int[] coldIds = new int[columns.size()];
          int i=0;
          for(Integer cid:columns) {
            coldIds[i]=cid; i++;
          }
          CSVExportSROutput csvExporter = new CSVExportSROutput();
          csvExporter.ssetColumnIds(coldIds);
          csvExporter.export(fw, sro);
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
      lock(false);
    }
  }
}