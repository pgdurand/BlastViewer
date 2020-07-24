/* Copyright (C) 2003-2016 Patrick G. Durand
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
package bzh.plealog.blastviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.EZGenericApplication;
import com.plealog.genericapp.api.EZUIStarterListener;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;
import com.plealog.genericapp.api.log.EZLoggerManager;
import com.plealog.genericapp.api.log.EZLoggerManager.LogLevel;
import com.plealog.genericapp.api.log.EZSingleLineFormatter;
import com.plealog.genericapp.ui.desktop.CascadingWindowPositioner;
import com.plealog.genericapp.ui.desktop.GDesktopPane;
import com.plealog.genericapp.ui.desktop.JWindowsMenu;

import bzh.plealog.bioinfo.api.core.config.CoreSystemConfigurator;
import bzh.plealog.bioinfo.api.filter.config.FilterSystemConfigurator;
import bzh.plealog.bioinfo.ui.blast.config.ConfigManager;
import bzh.plealog.bioinfo.ui.blast.config.color.ColorPolicyConfig;
import bzh.plealog.bioinfo.ui.config.UISystemConfigurator;
import bzh.plealog.bioinfo.ui.modules.filter.FilterManagerUI;
import bzh.plealog.bioinfo.ui.modules.filter.FilterSystemUI;
import bzh.plealog.bioinfo.ui.util.MemoryMeter;
import bzh.plealog.blastviewer.actions.api.BVAction;
import bzh.plealog.blastviewer.actions.api.BVActionManager;
import bzh.plealog.blastviewer.actions.hittable.FilterEntryAction;
import bzh.plealog.blastviewer.actions.hittable.SaveEntryAction;
import bzh.plealog.blastviewer.actions.main.FetchFromNcbiAction;
import bzh.plealog.blastviewer.actions.main.OpenFileAction;
import bzh.plealog.blastviewer.actions.main.OpenSampleFileAction;
import bzh.plealog.blastviewer.config.CmdLineManager;
import bzh.plealog.blastviewer.config.FileExtension;
import bzh.plealog.blastviewer.config.color.ColorPolicyConfigImplem;
import bzh.plealog.blastviewer.config.directory.DirManager;
import bzh.plealog.blastviewer.hittable.BVHitTableFactoryImplem;
import bzh.plealog.blastviewer.resources.BVMessages;
import bzh.plealog.blastviewer.util.BlastTransferHandler;
import bzh.plealog.blastviewer.util.BlastViewerOpener;

/**
 * Starter class for the Blast Viewer software.
 * 
 * @author Patrick G. Durand
 */
public class BlastViewer {

  /**
   * JVM optional argument. Provide a configuration directory. If not provided,
   * software locates its resources and configuration files within compiled Java
   * codes. Sample use: -DV_CONF=./config
   */
  private static final String JVM_ARG_CONF  = "V_CONF";
  /**
   * JVM optional argument. Switch debug mode to true or false. If not provided,
   * debug mode is disabled. Sample use: -DV_DEBUG=true
   */
  public static final String  JVM_ARG_DEBUG = "V_DEBUG";

  private static final String LOAD_ERR      = "Load Color configuration from: %s";
  private static final String CONF_ERR      = "Unable to load color configuration: ";

  /**
   * Start application. Relies on the Java Generic Application Framework. See
   * https://github.com/pgdurand/jGAF
   */
  public static void main(String[] args) {
    // This has to be done at the very beginning, i.e. first method call within
    // main().
    EZGenericApplication.initialize("BLASTViewer");
    // Add application branding
    Properties props = getVersionProperties();
    EZApplicationBranding.setAppName(props.getProperty("prg.app.name"));
    EZApplicationBranding.setAppVersion(props.getProperty("prg.version"));
    EZApplicationBranding.setCopyRight(props.getProperty("prg.copyright"));
    EZApplicationBranding.setProviderName(props.getProperty("prg.provider"));

    // setup the logger framework
    // turn off Java Logging standard console log
    EZLoggerManager.enableConsoleLogger(false);
    // turn on UI logger; text size limit is set to 2 million characters (when
    // content of UI logger reaches that limit, then UI text component simply
    // reset its content).
    EZLoggerManager.enableUILogger(true, 2);
    // delegate LogLevel to software config since JVM argument can be used
    // to modify standard behavior (see DocViewerConfig.JVM_ARG_DEBUG)
    initLogLevel();
    // setup the logging system
    EZLoggerManager.initialize();

    // some third party libraries rely on log4j (e.g. Castor XML framework)
    BasicConfigurator.configure();

    // Required to use Plealog Bioinformatics Core objects such as Features,
    // FeatureTables, Sequences
    CoreSystemConfigurator.initializeSystem();

    // Required to use the Plealog Bioinformatics UI library (CartoViewer
    // default graphics)
    UISystemConfigurator.initializeSystem();

    // we need to know where are located this application resources (messages
    // and icons)
    EZEnvironment
        .addResourceLocator(bzh.plealog.blastviewer.resources.BVMessages.class);

    // we enable the application to automatically serialize application
    // properties (e.g. column model of the BlastHitTable)
    ConfigManager.setEnableSerialApplicationProperty(true);
    // we redirect the factory to use our implementation of a BlastHitTable
    ConfigManager.setHitTableFactory(new BVHitTableFactoryImplem());
    // we setup the Directory Manager
    ConfigManager.addConfig(new DirManager());
    
    //setup file extensions
    FileExtension.initialize();
    
    // we setup the color policy
    initColorPolicyConfig();

    // we setup the FilterEngine System
    initFilteringSystem();
    
    // we install actions for the HitTable component
    initActions();
    
    // Add a listener to application startup cycle (see below)
    EZEnvironment.setUIStarterListener(new MyStarterListener());

    // Start the application
    EZGenericApplication.startApplication(args);
  }

  /**
   * Set the log level to info or debug. Rely on the JVM argument DV_DEBUG. Use:
   * -DDV_DEBUG=true.
   */
  public static void initLogLevel() {
    String dbg = System.getProperty(JVM_ARG_DEBUG);
    if (dbg != null) {
      EZLoggerManager
          .setLevel(dbg.toLowerCase().equals("true") ? LogLevel.debug
              : LogLevel.info);
    }
    // set the Formatter of the UI logger: in debug mode, provide full
    // class/method names
    EZLoggerManager.setUILoggerFormatter(new EZSingleLineFormatter(
        EZLoggerManager.getLevel() == LogLevel.debug));
  }

  /**
   * Return the software configuration file. By default, there is no such
   * directory available. Has to be setup using JRM argument: DV_CONF, with
   * value targeting a directory.
   */
  public static String getConfigurationPath() {
    String confP = System.getProperty(JVM_ARG_CONF);

    if (confP == null)
      return null;

    return EZFileUtils.terminatePath(confP);
  }

  /**
   * Return the content of the version resource.
   */
  private static Properties getVersionProperties() {
    Properties props = new Properties();
    try (InputStream in = BlastViewer.class
        .getResourceAsStream("version.properties");) {
      props.load(in);
      in.close();
    } catch (Exception ex) {// should not happen
      System.err.println("Unable to read props: " + ex.toString());
    }
    return props;
  }

  private static void initFilteringSystem(){
    FilterSystemConfigurator.initializeSystem();
    //prepare Filter Manager environment
    FilterSystemUI.initializeSystem();

    DirManager mgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
    String filterStoragePath;
    try {
      filterStoragePath = mgr.getApplicationDataPath();
      filterStoragePath = FilterManagerUI.prepareEnvironment(filterStoragePath);
    } catch (IOException e) {
      EZLogger.warn(e.toString());
      return;
    }
    FilterSystemUI.setFilterCentralRepositoryPath(EZFileUtils.terminatePath(filterStoragePath));
  }
  /**
   * Initializes colors system.
   */
  private static void initColorPolicyConfig() {
    ColorPolicyConfig nc;
    FileInputStream fis = null;
    String confPath;
    File f;
    InputStream in = null;

    try {
      // first, try to locate the file in the user conf dir
      confPath = getConfigurationPath();
      if (confPath != null) {
        confPath += ColorPolicyConfigImplem.CONFIG_FILE_NAME;
        f = new File(confPath);
        if (f.exists()) {
          EZLogger.debug(String.format(LOAD_ERR, f.getAbsolutePath()));
          in = new FileInputStream(f);
        }
      }
      // try from software resource
      if (in == null) {
        EZLogger.debug(String.format(
            LOAD_ERR,
            BVMessages.class.getResource(
                ColorPolicyConfigImplem.CONFIG_FILE_NAME).toString()));
        in = BVMessages.class
            .getResourceAsStream(ColorPolicyConfigImplem.CONFIG_FILE_NAME);
      }
      // will use default color config
      if (in == null)
        return;

      nc = new ColorPolicyConfigImplem(confPath);
      nc.load(in);
      // nc.dumpConfig();
      ConfigManager.addConfig(nc);
    } catch (Exception ex) {
      EZLogger.warn(CONF_ERR + ex.toString());
    } finally {
      IOUtils.closeQuietly(fis);
    }
  }

  private static void initActions(){
    BVAction  act;
    
    // Filter action
    act = new FilterEntryAction(
          BVMessages.getString("BlastHitList.filter.btn"),
          BVMessages.getString("BlastHitList.filter.tip"),
          EZEnvironment.getImageIcon("filterRes.png"));
    BVActionManager.addAction(act);

    // Save action
    act = new SaveEntryAction(
        BVMessages.getString("BlastHitList.save.btn"),
        BVMessages.getString("BlastHitList.save.tip"),
        EZEnvironment.getImageIcon("saveRes.png"));
    BVActionManager.addAction(act);
  }
  
  /**
   * Implementation of the jGAF API.
   */
  private static class MyStarterListener implements EZUIStarterListener {

    private GDesktopPane _desktop;
    private Component    _mainCompo = null;
    private JPanel       _btnPanel;

    private Component prepareDesktop() {
      JPanel dpanel, mnuPnl, hlpPnl;
      JButton logBtn;
      JWindowsMenu windowsMenu;

      // prepare the Desktop panel
      _desktop   = new GDesktopPane();
      // this is for Drag-and-Drop
      _desktop.setTransferHandler(new BlastTransferHandler());
      
      // prepare the Windows drop down menu; this is for MDI operations
      // (MDI: multiple document interface)
      JMenuBar menuBar = new JMenuBar();
      ImageIcon icon = EZEnvironment.getImageIcon("documents.png");
      if (icon != null) {
        windowsMenu = new JWindowsMenu(_desktop.getDesktopPane());
        windowsMenu.setIcon(icon);
      } else {
        windowsMenu = new JWindowsMenu(
            BVMessages.getString("DocumentViewer.docs.mnu"),
            _desktop.getDesktopPane());
      }
      windowsMenu.setWindowPositioner(new CascadingWindowPositioner(_desktop
          .getDesktopPane()));
      menuBar.add(windowsMenu);

      // prepare the little "logger" button
      logBtn = new JButton(EZEnvironment.getImageIcon("logger.png"));
      logBtn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
      logBtn.addActionListener(new ShowLoggerFrame());
      logBtn.setVerticalTextPosition(SwingConstants.BOTTOM);
      logBtn.setHorizontalTextPosition(SwingConstants.CENTER);
      logBtn.setText("Log");
      windowsMenu.setFont(logBtn.getFont());

      // prepare some panels to layout commands located on top of the Desktop
      dpanel = new JPanel(new BorderLayout());
      mnuPnl = new JPanel(new BorderLayout());
      _btnPanel = new JPanel(new BorderLayout());
      mnuPnl.add(menuBar, BorderLayout.WEST);
      mnuPnl.add(logBtn, BorderLayout.EAST);
      _btnPanel.add(mnuPnl, BorderLayout.EAST);
      //hlpPnl = new JPanel(new GridBagLayout());
      //hlpPnl.add(BlastViewerOpener.getHelperField());
      //_btnPanel.add(hlpPnl, BorderLayout.CENTER);
      _btnPanel.add(getToolbar(), BorderLayout.WEST);
      dpanel.add(_btnPanel, BorderLayout.NORTH);
      dpanel.add(_desktop, BorderLayout.CENTER);

      JPanel statusBar = new JPanel(new BorderLayout());
      hlpPnl = new JPanel(new BorderLayout());
      hlpPnl.add(BlastViewerOpener.getHelperField(), BorderLayout.WEST);
      statusBar.add(new MemoryMeter(), BorderLayout.WEST);
      statusBar.add(hlpPnl, BorderLayout.CENTER);
      dpanel.add(statusBar, BorderLayout.SOUTH);
      
      BlastViewerOpener.setDesktop(_desktop);

      return dpanel;
    }

    @Override
    public Component getApplicationComponent() {
      if (_mainCompo != null)
        return _mainCompo;

      // prepare the desktop viewer system
      _mainCompo = prepareDesktop();

      return _mainCompo;
    }

    @Override
    public boolean isAboutToQuit() {
      // You can add some code to figure out if application can exit.

      // Return false to prevent application from exiting (e.g. a background
      // task is still running).
      // Return true otherwise.

      // Do not add a Quit dialogue box to ask user confirmation: the framework
      // already does that for you.
      return true;
    }

    @Override
    public void postStart() {
      // This method is called by the framework just before displaying UI
      // (main frame).
      EZLogger.info(String.format("%s - %s",
          EZApplicationBranding.getAppName(),
          EZApplicationBranding.getAppVersion()));
      EZLogger.info(EZApplicationBranding.getCopyRight());
      try {
        DirManager dmgr = (DirManager) ConfigManager.getConfig(DirManager.NAME);
        //EZLogger.info(dmgr.getApplicationDataPath());
        EZLogger.info("Storage directory is: "+dmgr.getBlastDataPath());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    @Override
    public void preStart() {
      // This method is called by the framework at the very beginning of
      // application startup.
      String[] params = EZEnvironment.getApplicationArguments();
      CmdLineManager.checkShowHelpOrVersionMessage(params);
    }

    @Override
    public void frameDisplayed() {
      String[] params = EZEnvironment.getApplicationArguments();
      CmdLineManager.handleArguments(params);
    }

  }

  private static JToolBar getToolbar() {
    JToolBar tBar;
    ImageIcon icon;
    Action act;
    JButton btn;

    tBar = new JToolBar();
    tBar.setFloatable(false);

    icon = EZEnvironment.getImageIcon("openTest.png");
    if (icon != null) {
      act = new OpenSampleFileAction("", icon);
    } else {
      act = new OpenSampleFileAction(BVMessages.getString("OpenBlastList.test.name"));
    }
    act.setEnabled(true);
    btn = tBar.add(act);
    btn.setToolTipText(BVMessages.getString("OpenBlastList.test.tip"));
    btn.setText(BVMessages.getString("OpenBlastList.test.name"));

    tBar.addSeparator();
    icon = EZEnvironment.getImageIcon("open.png");
    if (icon != null) {
      act = new OpenFileAction("", icon);
    } else {
      act = new OpenFileAction(BVMessages.getString("OpenBlastList.open.name"));
    }
    act.setEnabled(true);
    btn = tBar.add(act);
    btn.setToolTipText(BVMessages.getString("OpenBlastList.open.tip"));
    btn.setText(BVMessages.getString("OpenBlastList.open.name"));

    icon = EZEnvironment.getImageIcon("download.png");
    if (icon != null) {
      act = new FetchFromNcbiAction("", icon);
    } else {
      act = new FetchFromNcbiAction(
          BVMessages.getString("OpenBlastList.openrid.name"));
    }
    act.setEnabled(true);
    btn = tBar.add(act);
    btn.setToolTipText(BVMessages.getString("OpenBlastList.openrid.tip"));
    btn.setText(BVMessages.getString("OpenBlastList.openrid.name"));
    return tBar;
  }

  /**
   * Utility class to show the UI Logger component.
   */
  private static class ShowLoggerFrame implements ActionListener {
    private JFrame frame;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (frame == null) {
        makeFrame();
      }
      frame.setVisible(!frame.isVisible());
    }

    private void makeFrame() {
      int delta = 50;
      frame = new JFrame(BVMessages.getString("DocumentViewer.docs.tab2"));
      frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
      Rectangle rect = EZEnvironment.getParentFrame().getBounds();
      rect.x += delta;
      rect.y += delta;
      rect.width -= 2 * delta;
      rect.height -= 2 * delta;
      frame.getContentPane().add(EZLoggerManager.getUILogger());
      frame.setBounds(rect);
    }
  }

}
