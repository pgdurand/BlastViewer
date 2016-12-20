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
package bzh.plealog.blastviewer.config.directory;

import java.io.File;
import java.io.IOException;

import bzh.plealog.bioinfo.ui.blast.config.AbstractPropertiesConfig;
import bzh.plealog.bioinfo.util.CoreUtil;

import com.plealog.genericapp.api.EZApplicationBranding;
import com.plealog.genericapp.api.file.EZFileUtils;

/**
 * This class handle the directories used by the software to store some internal
 * stuffs.
 * 
 * @author Patrick G. Durand
 */
public class DirManager extends AbstractPropertiesConfig {
  public static final String  NAME       = "DirManager";

  private static final String DOC_DIR    = "documents";

  private static final String BLAST_DOCS = "blast";

  private String              _appPath;
  private String              _blastDataPath;

  /**
   * Default constructor.
   * 
   * Such an object is supposed to be created at application startup and stored
   * in the ConfigManager system. Then access this configuration using
   * ConfigManager.
   */
  public DirManager() {
    setName(NAME);
  }

  private void createPath(String path) throws IOException {
    File f;

    f = new File(path);
    if (f.exists()) {
      return;
    }
    if (!f.mkdirs()) {
      throw new IOException("unable to create: " + path);
    }
  }

  /**
   * Get the full path to the directory where the application stores its stuffs.
   * 
   * It is worth noting that the method will try to create that path if it does
   * not exist.
   * 
   * @return a path. Note that path is terminated with OS-dependent path
   *         separator character.
   * 
   * @throws IOException
   *           if the method failed to create the path.
   */
  public String getApplicationDataPath() throws IOException {
    if (_appPath != null)
      return _appPath;

    _appPath = EZFileUtils.terminatePath(System.getProperty("user.home"))
        + "."
        + EZFileUtils.terminatePath(CoreUtil.replaceAll(
            EZApplicationBranding.getAppName(), " ", "_"));

    createPath(_appPath);

    return _appPath;
  }

  /**
   * Get the full path to the directory where the application stores BLAST
   * results retrieved from a remote server.
   * 
   * It is worth noting that the method will try to create that path if it does
   * not exist.
   * 
   * @return a path. Note that path is terminated with OS-dependent path
   *         separator character.
   * 
   * @throws IOException
   *           if the method failed to create the path.
   */
  public String getBlastDataPath() throws IOException {
    if (_blastDataPath != null)
      return _blastDataPath;

    _blastDataPath = getApplicationDataPath()
        + EZFileUtils.terminatePath(DOC_DIR)
        + EZFileUtils.terminatePath(BLAST_DOCS);

    createPath(_blastDataPath);

    return _blastDataPath;
  }
  
  
}
