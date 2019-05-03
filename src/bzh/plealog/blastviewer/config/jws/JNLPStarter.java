package bzh.plealog.blastviewer.config.jws;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;

import com.plealog.genericapp.api.log.EZLogger;

import bzh.plealog.blastviewer.config.CmdLineManager;

// see https://docs.oracle.com/javase/7/docs/technotes/guides/javaws/index.html
//     https://docs.oracle.com/javase/8/docs/technotes/guides/javaws/developersguide/faq.html
//     https://docs.oracle.com/javase/9/deploy/migrating-java-applets-jnlp.htm

public class JNLPStarter {


  private static SingleInstanceService _singleInstanceService = null;
  private static BasicService          _basicService = null;
  private static SISListener           _singleInstanceListener = null;
  
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
   * Handle the SingleInstanceService interface.
   */
  private static class SISListener implements SingleInstanceListener {
    public void newActivation(String[] params) {
      CmdLineManager.handleArguments(params);
    }
  }
}
