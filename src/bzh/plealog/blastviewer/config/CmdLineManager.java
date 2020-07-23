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
package bzh.plealog.blastviewer.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import javax.swing.JComponent;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.api.data.searchresult.SROutput;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.blastviewer.client.ncbi.NcbiFetcher;
import bzh.plealog.blastviewer.config.directory.DirManager;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastViewerOpener;
import bzh.plealog.blastviewer.util.HTTPBasicEngine;
import bzh.plealog.blastviewer.util.HTTPEngineException;

/**
 * Utility class to handle command-line arguments.
 */
public class CmdLineManager {
  //NCBI Blast job RID
  private static final String NRID_ARG = "nrid";
  //URL of the Blast document to download
  private static final String URL_ARG = "url";
  //file path of the Blast to load
  private static final String FILE_ARG = "in";

  //Other stuff
  private static final String RES_BASE_NAME = "result_";
  private static final String RES_FILE_COUNTER = "result.properties" ;
  private static final String RES_COUNTER_KEY =  "counter" ;
  
  private static CommandLine  _cmdLinePlainTextLine = null;
  private static Properties   _propResFileIndex = null;
  private static int          _iQueryCounter = -1;

  public CmdLineManager() {
  }

  /**
   * Setup the valid command-line of the application.
   */
  private static Options getCmdLineOptions(){
    Options opts;

    opts = new Options();
    opts.addOption(NRID_ARG, true, URL_ARG);
    opts.addOption(URL_ARG, true, URL_ARG);
    opts.addOption(FILE_ARG, true, FILE_ARG);
    return opts;
  }

  /**
   * Analyze some command-line arguments.
   */
  public static void handleArguments(String[] args){
    Options   options = null;
    GnuParser parser = null;

    if(args!=null){
      EZLogger.debug(BVMessages.getString("CmdLineManager.msg1"));
      for(String arg:args) {
        EZLogger.debug(arg);
      }
    }
    try {       
      parser = new GnuParser();
      options = getCmdLineOptions();
      _cmdLinePlainTextLine = parser.parse( options, args);
    }
    catch( Exception e ) {
      EZLogger.warn( BVMessages.getString("CmdLineManager.err1")+ e);
      return;
    }
    boolean hasCmdlineObj = _cmdLinePlainTextLine.hasOption(NRID_ARG) ||
        _cmdLinePlainTextLine.hasOption(URL_ARG) ||
        _cmdLinePlainTextLine.hasOption(FILE_ARG);

    //Do we have cmd-line arguments with BLAST XML docs to open?
    if (!hasCmdlineObj){
      return;
    }

    if (_cmdLinePlainTextLine.hasOption(NRID_ARG)) {
      new NcbiOpenerThread(_cmdLinePlainTextLine.getOptionValues(NRID_ARG)).start();
    }
    else if (_cmdLinePlainTextLine.hasOption(URL_ARG)) {
      new UrlOpenerThread(_cmdLinePlainTextLine.getOptionValues(URL_ARG)).start();
    }
    else if (_cmdLinePlainTextLine.hasOption(FILE_ARG)) {
      new FileOpenerThread(_cmdLinePlainTextLine.getOptionValues(FILE_ARG)).start();
    }

    EZEnvironment.getParentFrame().toFront();
  }

  /**
   * Create the path to the directory that will contains the local user
   * data.
   */
  private static String getLocalUserData(){
    String userHome;

    try {
      DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
      userHome = dmgr.getBlastDataPath();
    } catch (IOException e) {
      userHome = EZFileUtils.terminatePath(System.getProperty("java.io.tmpdir"));
    }
    return userHome;
  }

  /**
   * Create a file to use to save loaded result data.
   */
  private static synchronized File getOutputFile(){
    String fName;
    File   file;

    loadResFileCounter();
    fName = RES_BASE_NAME+_iQueryCounter;
    file = new File(getLocalUserData()+fName);
    _iQueryCounter++;
    saveResFileCounter();
    return file;
  }

  /**
   * Utility method to load the result properties file.
   */
  private static void loadResFileCounter(){
    _propResFileIndex = new Properties();
    try {
      _propResFileIndex.load(new FileInputStream(getLocalUserData()+RES_FILE_COUNTER));
      _iQueryCounter = Integer.valueOf(_propResFileIndex.getProperty(RES_COUNTER_KEY)).intValue();
    }
    catch (Exception e) {
      _iQueryCounter = 1;
    }
  }
  /**
   * Utility method to save the result properties file.
   */
  private static void saveResFileCounter(){
    try {
      _propResFileIndex.setProperty(RES_COUNTER_KEY, String.valueOf(_iQueryCounter));
      _propResFileIndex.store(new FileOutputStream(getLocalUserData()+RES_FILE_COUNTER), "");
    }
    catch (Exception e) {//not really bad
    }
  }


  private static class NcbiOpenerThread extends Thread{
    private String[] rids;
    public NcbiOpenerThread(String[] rids){
      this.rids = rids;
    }
    public void run(){
      if (rids==null)
        return;
      for(String rid : rids){
        NcbiFetcher.fetchAndShow(rid);
      }
    }
  }
  
  private static class UrlOpenerThread extends Thread{
    private String[] urls;
    public UrlOpenerThread(String[] urls){
      this.urls = urls;
    }
    /**
     * Open a URL pointing to a data file.
     */
    private static void openURL(String url){
      File tmpFile;
      
      //Step 1: download data given provided URL
      try {
        tmpFile = HTTPBasicEngine.doGet(url);
      } catch (HTTPEngineException ex) {
        String msg = BVMessages.getString("CmdLineManager.err2");
        msg = MessageFormat.format(msg, new Object[] {url, ex});
        EZLogger.warn(msg);
        msg = BVMessages.getString("CmdLineManager.err3");
        msg = MessageFormat.format(msg, new Object[] {url});
        EZEnvironment.displayWarnMessage(EZEnvironment.getParentFrame(), msg);
        return;
      }
      
      //Step 2: read file content and prepare a viewer
      SROutput sro = BlastViewerOpener.readBlastFile(tmpFile);

      BlastViewerOpener.setHelperMessage(BVMessages
          .getString("FetchFromNcbiAction.msg4"));

      JComponent viewer = BlastViewerOpener.prepareViewer(sro);

      BlastViewerOpener.displayInternalFrame(viewer, url, null);
    }
    public void run(){
      if (urls==null)
        return;
      EZEnvironment.setWaitCursor();
      for(String url : urls){
        openURL(url);
      }
      BlastViewerOpener.cleanHelperMessage();
      EZEnvironment.setDefaultCursor();
    }
  }

  private static class FileOpenerThread extends Thread{
    private String[] urls;
    public FileOpenerThread(String[] urls){
      this.urls = urls;
    }
    /**
     * Open a URL pointing to a data file.
     */
    private static void openFile(File f){
      BlastViewerOpener.setHelperMessage(BVMessages
          .getString("OpenFileAction.msg1"));

      SROutput sro = BlastViewerOpener.readBlastFile(f);

      BlastViewerOpener.setHelperMessage(BVMessages
          .getString("FetchFromNcbiAction.msg4"));

      JComponent viewer = BlastViewerOpener.prepareViewer(sro);

      BlastViewerOpener.displayInternalFrame(viewer, f.getName(), null);
    }
    public void run(){
      if (urls==null)
        return;
      EZEnvironment.setWaitCursor();
      //given URL are considered to be relative to the base URL
      for(String f : urls){
        //handle error on a "per file basis"
        try {
          openFile(new File(f));
        } catch (Throwable t) {
          EZLogger.warn(BVMessages.getString("OpenFileAction.err")
              + t.toString());
        } finally {
          BlastViewerOpener.cleanHelperMessage();
          EZEnvironment.setDefaultCursor();
        }
      }
    }
  }

}
