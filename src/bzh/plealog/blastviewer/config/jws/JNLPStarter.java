package bzh.plealog.blastviewer.config.jws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

// see https://docs.oracle.com/javase/7/docs/technotes/guides/javaws/index.html
//     https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/faq.html
//     https://docs.oracle.com/javase/9/deploy/migrating-java-applets-jnlp.htm

public class JNLPStarter {

  private static final String URL_ARG = "url";
  private static final String ERR_MSG2 = "Unable to read URL: ";
  private static final String HTTP_PROTOCOL_URL_SEPARATOR = "://";
  private static final String HTTP_PORT_NUMBER_URL_SEPARATOR = ":";
  private static final String HTTP_URL_SEPARATOR = "/";
  private static final String WS_EXT_KEY = ".ws";
  private static final String RES_PATH = "results";
  private static final String RES_BASE_NAME = "result_";
  private static final String RES_FILE_COUNTER = "result.properties" ;
  private static final String RES_COUNTER_KEY =  "counter" ;


  private static SingleInstanceService _singleInstanceService = null;
  private static BasicService          _basicService = null;
  private static SISListener           _singleInstanceListener = null;
  private static CommandLine           _cmdLinePlainTextLine = null;
  private static Properties            _propResFileIndex = null;
  private static String                _sLocalUserResultPath = null;
  private static int                   _iQueryCounter = -1;
  
  private static boolean initBasiService(){
    boolean bRet = true;
    // BasicService.getCodeBase​() -> NULL  if the application is running from local file system.
    if (_basicService != null)
      return bRet;
    try { 
      _basicService = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
    } catch (UnavailableServiceException e2) {
      EZLogger.warn(e2.toString());
      _basicService = null;
      bRet = false;
    }
    return bRet;
  }
  /**
   * Setup the SingleInstanceService.
   */
  public static void startJWSService(){
    // To be called by pre-init() of app
    if (! initBasiService()){
      return;
    }
    try { 
      _singleInstanceService = 
          (SingleInstanceService)ServiceManager.lookup("javax.jnlp.SingleInstanceService");
    } catch (UnavailableServiceException e) {
      EZLogger.warn(e.toString());
      _singleInstanceService=null; 
    }
    if (_singleInstanceService!=null){
      _singleInstanceListener = new SISListener();
      _singleInstanceService.addSingleInstanceListener(_singleInstanceListener);
    }
    loadResFileCounter();
  }

  /**
   * Return the server key. Can return null if JNLP Basic Service is not available.
   */
  public static String getServerHostKey(){
    if (!initBasiService())
      return null;
    String       host;
    StringBuffer hostKey = new StringBuffer();
    char         ch;

    if (_basicService.getCodeBase()==null)
      return null;
    host = _basicService.getCodeBase().getHost()+_basicService.getCodeBase().getPath();
    for(int i=0;i<host.length();i++){
      ch = host.charAt(i);
      if (Character.isDigit(ch)||Character.isLetter(ch))
        hostKey.append(ch);
    }
    if (hostKey.length()==0)
      return null;
    return hostKey.toString();
  }

  /**
   * Return the base code of the application.
   */
  public static String getBaseCode(){
    if (!initBasiService())
      return null;
    if (_basicService.getCodeBase()==null)
      return null;
    return EZFileUtils.terminateURL(_basicService.getCodeBase().toString());
  }

  /**
   * Returns the host name.
   */
  public static String getHostName(){
    if (_basicService.getCodeBase()==null)
      return null;
    else
      return _basicService.getCodeBase().getHost();
  }

  /**
   * Remove the SingleInstanceService.
   */
  public static void removeJWSListener(){
    // to be called at app termination
    if (_singleInstanceService!=null)
      _singleInstanceService.removeSingleInstanceListener(_singleInstanceListener);
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
  private static void handleArguments(String[] args){
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
  
  public static String getUserHomeDirectory (){
    String userHome = "";

    userHome = System.getProperty("user.home");

    // workaround bug #4787931 Windows specific
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4787931
    if (EZEnvironment.getOSType()==EZEnvironment.WINDOWS_OS){
      if ( userHome.indexOf("Windows") != -1){
        userHome = System.getenv("USERPROFILE");
      }       
    }

    return userHome;
  }

  /**
   * Create the path to the directory that will contains the local user
   * data.
   */
  private static String getLocalUserData(){
    StringBuffer userHome = new StringBuffer();

    userHome.append(EZFileUtils.terminatePath(getUserHomeDirectory()));
    userHome.append(WS_EXT_KEY);
    userHome.append(EZApplicationBranding.getAppName());
    userHome.append(File.separator);
    userHome.append("D");
    userHome.append(getServerHostKey());
    userHome.append(File.separator);
    return userHome.toString();
  }

  /**
   * Create the path to the directory that will contains the results
   * loaded from the remote server.
   */
  private static String getLocalUserRes(){
    if (_sLocalUserResultPath==null)
      _sLocalUserResultPath = getLocalUserData()+RES_PATH+File.separator;
    return _sLocalUserResultPath;
  }
  /**
   * Create a file to use to save loaded result data.
   */
  private static synchronized File getOutputFile(){
    String fName;
    File   file;

    fName = RES_BASE_NAME+_iQueryCounter;
    file = new File(getLocalUserRes()+fName);
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
    _propResFileIndex.load(new FileInputStream(getLocalUserRes()+RES_FILE_COUNTER));
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
    _propResFileIndex.store(new FileOutputStream(getLocalUserRes()+RES_FILE_COUNTER), "");
  }
    catch (Exception e) {//not really bad
  }
  }

  /**
   * Handle the SingleInstanceService interface.
   */
  private static class SISListener implements SingleInstanceListener {

    public void newActivation(String[] params) {
      handleArguments(params);
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
      //get a base URL made of the authorized JWS app base URL
      URL          base = _basicService.getCodeBase(); 
      String       urlBase;
      StringBuffer buf = new StringBuffer();

      buf.append(base.getProtocol());
      buf.append(HTTP_PROTOCOL_URL_SEPARATOR);
      buf.append(base.getHost());
      if (base.getPort()!=-1){
        buf.append(HTTP_PORT_NUMBER_URL_SEPARATOR);
        buf.append(base.getPort());
      }
      urlBase = buf.toString();
      //given URL are considered to be relative to the base URL
      for(String url : urls){
        if (!url.startsWith(HTTP_URL_SEPARATOR))
          openURL(urlBase+HTTP_URL_SEPARATOR+url);
        else
          openURL(urlBase+url);
      }
    }
  }

}
