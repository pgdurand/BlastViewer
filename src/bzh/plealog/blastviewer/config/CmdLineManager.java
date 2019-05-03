package bzh.plealog.blastviewer.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.blastviewer.config.directory.DirManager;

public class CmdLineManager {
  //URL of the Blast document to download
  private static final String URL_ARG = "url";
  //URL of the CAS server to be used for user authentication
  private static final String CAS_URL_ARG = "curl";
  private static final String ERR_MSG2 = "Unable to read URL: ";
  private static final String RES_BASE_NAME = "result_";
  private static final String RES_FILE_COUNTER = "result.properties" ;
  private static final String RES_COUNTER_KEY =  "counter" ;

  private static CommandLine           _cmdLinePlainTextLine = null;
  private static Properties            _propResFileIndex = null;
  private static int                   _iQueryCounter = -1;

  public CmdLineManager() {
    // TODO Auto-generated constructor stub
  }

  /**
   * Setup the valid command-line of the application.
   */
  private static Options getCmdLineOptions(){
    Options opts;

    opts = new Options();
    opts.addOption(URL_ARG, true, URL_ARG);
    return opts;
  }

  /**
   * Analyze some command-line arguments.
   */
  public static void handleArguments(String[] args){
    Options   options = null;
    GnuParser parser = null;

    try {       
      parser = new GnuParser();
      // get plain text option
      options = getCmdLineOptions();
      _cmdLinePlainTextLine = parser.parse( options, args);
    }
    catch( ParseException exp ) {
      EZLogger.warn( ERR_MSG2+": " + exp);
      return;
    }
    catch( Exception e ) {
      EZLogger.warn( ERR_MSG2+": " + e);
    }
    if (!_cmdLinePlainTextLine.hasOption(URL_ARG)){
      return;
    }
    //given URLs have to be relative: they must only contain a path
    new OpenerThread(_cmdLinePlainTextLine.getOptionValues(URL_ARG)).start();

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


  private static class OpenerThread extends Thread{
    private String[] urls;
    public OpenerThread(String[] urls){
      this.urls = urls;
    }
    /**
     * Open a URL pointing to a data file.
     */
    private static void openURL(String url){
      /*File             tmpFile;
      FileReaderThread frT;

      if (url==null)
        return;
      if (!isUrlAcceptable(url)){
        UserUIMessenger.displayWarnMessage(ERR_MSG6+url);
        return;
      }
      UserUIMessenger.setWaitCursor();
      UserUIMessenger.setInfoStatusMessage(INFO_MSG1+url);
      HttpBasicEngine engine = new HttpBasicEngine();
      tmpFile = getOutputFile();
      if (engine.doGetEx(url, INFO_MSG0, tmpFile) == null){
        _logger.warn(engine.getErrorMsg());
        UserUIMessenger.displayWarnMessage(ERR_MSG1+": "+url);
        return;
      }
      try {
        BlastManager bm = (BlastManager)
            KLConfiguratorBase.getSystemObject(
                KLConfiguratorBase.SO_MAIN_MANAGER, 
                BlastManager.SYS_NAME);
        frT = new FileReaderThread(bm, new File[]{tmpFile}, null);
        frT.setMakeCopy(false);
        frT.start();
        frT.join();
      } catch (Exception e) {
        //should not happen
        _logger.warn(e);
      }
      //delete the file to avoid a local history
      try{tmpFile.delete();}catch(Exception ex){}
      UserUIMessenger.setDefaultCursor();*/
    }
    public void run(){
      if (urls==null)
        return;
      //given URL are considered to be relative to the base URL
      for(String url : urls){
          openURL(url);
      }
    }
  }

}
