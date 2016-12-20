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
package bzh.plealog.blastviewer.config.color;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import org.apache.commons.io.IOUtils;

import bzh.plealog.bioinfo.api.data.searchresult.SRHsp;
import bzh.plealog.bioinfo.api.data.searchresult.SRHspScore;
import bzh.plealog.bioinfo.ui.blast.config.color.ColorPolicyConfig;
import bzh.plealog.bioinfo.util.CoreUtil;

import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.api.file.EZFileUtils;
import com.plealog.genericapp.api.log.EZLogger;

/**
 * This class contains the color policy used to display colored Blast alignment,
 * scores and so on.
 * 
 * @author Patrick G. Durand
 */
public class ColorPolicyConfigImplem extends ColorPolicyConfig {
  /** contains the color policy */
  private HitColorPolicyAtom[]    _hitListColorPolicy;
  private HitQualityPolicyAtom[]  _hitListQualityPolicy;
  private int                     _hlcField;
  private int                     _hlqField;
  private Color                   _defFeatClr;
  private boolean                 _useInvVideo;
  private boolean                 _useAntialias         = true;
  private boolean                 _useTransparency      = true;
  private String                  _confPath;

  /** of this configuration */
  public static final String      NAME                  = "ColorPolicyConfig";
  /**
   * key used to retrieve the color configuration within a resource bundle file
   */
  private static final String     MSA_REVERSE_VIDEO_KEY = "inverse.video";
  private static final String     TRANSPARENCY_KEY      = "clr.transparency";
  private static final String     ANTIALIAS_KEY         = "antialias";
  private static final String     TABLE_BK_COLOR_KEY    = "table.bk.color";
  private static final String     HLC_CLASSES           = "hitListColorClasses";
  private static final String     HLQ_CLASSES           = "hitListQualityClasses";
  private static final String     HLC_CLASS             = "hlc.";
  private static final String     HLQ_CLASS             = "hlq.";
  private static final String     HLC_FIELD             = "hlc.field";
  private static final String     HLQ_FIELD             = "hlq.field";
  private static final String     THRESH_SUFFIX         = ".threshold";
  private static final String     CLR_SUFFIX            = ".color";
  private static final String     QUAL_SUFFIX           = ".qualityCode";
  private static final String     DEF_FEAT_CLR          = "feature.clr";

  public static Color             BK_COLOR              = Color.WHITE;
  public static final Color       QUERY_CELL_BK_COLOR   = new Color(184, 207,
                                                            229);
  public static final int         TRANSPARENCY_FACTOR   = 128;

  public static final ImageIcon[] QUALITY_SMILEY        = new ImageIcon[] {
      EZEnvironment.getImageIcon("smiley_g.png"),
      EZEnvironment.getImageIcon("smiley_y.png"),
      EZEnvironment.getImageIcon("smiley_o.png"),
      EZEnvironment.getImageIcon("smiley_r.png")       };

  public static final String[]    FIELDS                = new String[] {
      "Bit score", "E-Value", "Identity (%)", "Similarity (%)", "Gaps (%)",
      "Query Coverage (%)", "Hit Coverage (%)", "Score" };

  public static final int         BITS_FIELD            = 0;
  public static final int         EVAL_FIELD            = 1;
  public static final int         IDENT_FIELD           = 2;
  public static final int         SIMIL_FIELD           = 3;
  public static final int         GAPS_FIELD            = 4;
  public static final int         COVERAGE_FIELD        = 5;
  public static final int         HCOVERAGE_FIELD       = 6;
  public static final int         SCORE_FIELD           = 7;

  public static final String      CONFIG_FILE_NAME      = "colorpolicy.cfg";

  /**
   * Default constructor.
   */
  public ColorPolicyConfigImplem(String confPath) {
    super();
    setName(NAME);
    _confPath = EZFileUtils.terminatePath(confPath);
  }

  /**
   * Set on/off color transparency.
   * 
   * @param useCT
   *          true of false
   */
  public void useColorTransparency(boolean useCT) {
    _useTransparency = useCT;
  }

  /**
   * Check if color transparency is used.
   * 
   * @return true or false
   */
  public boolean isUsingColorTransparency() {
    return _useTransparency;
  }

  /**
   * Set on/off anti-alias.
   * 
   * @param useAA
   *          true of false
   */
  public void useAntialias(boolean useAA) {
    _useAntialias = useAA;
  }

  /**
   * Check if anti-alias is used.
   * 
   * @return true of false
   */
  public boolean isUsingAntialias() {
    return _useAntialias;
  }

  /**
   * Return the default color to use to display features.
   * 
   * @return a Color
   */
  public Color getDefFeatureClr() {
    return _defFeatClr;
  }

  /**
   * Utility method used to create a HitColorPolicyAtom.
   */
  private HitColorPolicyAtom createClrAtom(String key) {
    HitColorPolicyAtom atom = null;
    String thresh, clr;

    thresh = this.getProperty(HLC_CLASS + key + THRESH_SUFFIX);
    clr = this.getProperty(HLC_CLASS + key + CLR_SUFFIX);
    try {
      atom = new HitColorPolicyAtom();
      atom.setThreshold(Double.valueOf(thresh));
      atom.setColor(convertColor(clr, ","));
    } catch (Exception ex) {
      EZLogger.warn("Invalid HitColorPolicyAtom: " + key + ": " + ex);
    }
    return atom;
  }

  /**
   * Utility method used to create a HitColorPolicyAtom table.
   */
  private void initHLCPolicyTable() {
    String items;
    String[] itemsKeys;
    HitColorPolicyAtom atom;
    int i;

    // read classes
    items = this.getProperty(HLC_CLASSES);
    itemsKeys = CoreUtil.tokenize(items);
    _hlcField = Integer.valueOf(this.getProperty(HLC_FIELD));
    _hitListColorPolicy = new HitColorPolicyAtom[itemsKeys.length];
    for (i = 0; i < itemsKeys.length; i++) {
      atom = createClrAtom(itemsKeys[i]);
      if (atom == null) {
        _hitListColorPolicy = null;
        return;
      }
      _hitListColorPolicy[i] = atom;
    }
  }

  /**
   * Utility method used to create a HitQualityPolicyAtom.
   */
  private HitQualityPolicyAtom createQualAtom(String key) {
    HitQualityPolicyAtom atom = null;
    String thresh, code;
    int idx;

    thresh = this.getProperty(HLQ_CLASS + key + THRESH_SUFFIX);
    code = this.getProperty(HLQ_CLASS + key + QUAL_SUFFIX);
    try {
      atom = new HitQualityPolicyAtom();
      atom.setThreshold(Double.valueOf(thresh));
      idx = Integer.valueOf(code);
      atom.setIconId(idx);
      atom.setQualityIcon((idx < QUALITY_SMILEY.length ? QUALITY_SMILEY[idx]
          : null));
    } catch (Exception ex) {
      EZLogger.warn("Invalid HitQualityPolicyAtom: " + key + ": " + ex);
    }
    return atom;
  }

  /**
   * Utility method used to create a HitQualityPolicyAtom table.
   */
  private void initHLQPolicyTable() {
    String items;
    String[] itemsKeys;
    HitQualityPolicyAtom atom;
    int i;

    // read classes
    items = this.getProperty(HLQ_CLASSES);
    itemsKeys = CoreUtil.tokenize(items);
    _hlqField = Integer.valueOf(this.getProperty(HLQ_FIELD));
    _hitListQualityPolicy = new HitQualityPolicyAtom[itemsKeys.length];
    for (i = 0; i < itemsKeys.length; i++) {
      atom = createQualAtom(itemsKeys[i]);
      if (atom == null) {
        _hitListQualityPolicy = null;
        return;
      }
      _hitListQualityPolicy[i] = atom;
    }
  }

  /**
   * Upload color policy from a stream.
   * 
   * @param inStream
   *          the input stream to read color configuration
   */
  public void load(InputStream inStream) throws IOException {
    super.load(inStream);
    String val;

    val = getProperty(TRANSPARENCY_KEY);
    if (val != null) {
      _useTransparency = val.equalsIgnoreCase("true");
    }
    val = getProperty(ANTIALIAS_KEY);
    if (val != null) {
      _useAntialias = val.equalsIgnoreCase("true");
    }

    val = getProperty(MSA_REVERSE_VIDEO_KEY);
    if (val != null) {
      _useInvVideo = val.equalsIgnoreCase("true");
    }
    val = getProperty(TABLE_BK_COLOR_KEY);
    if (val != null) {
      BK_COLOR = convertColor(val, ",");
    }

    val = getProperty(DEF_FEAT_CLR);
    if (val != null) {
      _defFeatClr = convertColor(val, ",");
    } else {
      _defFeatClr = Color.DARK_GRAY;
    }
    _defFeatClr = new Color(_defFeatClr.getRed(), _defFeatClr.getGreen(),
        _defFeatClr.getBlue(), 128);
    if (getProperty(HLC_CLASSES) != null) {
      initHLCPolicyTable();
    }
    if (getProperty(HLQ_CLASSES) != null) {
      initHLQPolicyTable();
    }
  }

  /**
   * Convert color (R,G,B) to Color object.
   */
  private Color convertColor(String szColor, String sep) {
    StringTokenizer tokenizer;
    String token;
    Color clr;
    int r, g, b;

    tokenizer = new StringTokenizer(szColor, sep);
    if (tokenizer.hasMoreTokens() == false)
      return Color.black;
    token = tokenizer.nextToken();
    r = Integer.valueOf(token).intValue();
    if (tokenizer.hasMoreTokens() == false)
      return Color.black;
    token = tokenizer.nextToken();
    g = Integer.valueOf(token).intValue();
    if (tokenizer.hasMoreTokens() == false)
      return Color.black;
    token = tokenizer.nextToken();
    b = Integer.valueOf(token).intValue();
    clr = new Color(r, g, b);
    return clr;
  }

  private double getValue(SRHsp hsp, int field) {
    SRHspScore score;

    double value;
    score = hsp.getScores();
    switch (field) {
      case BITS_FIELD:
        value = score.getBitScore();
        break;
      case EVAL_FIELD:
        value = score.getEvalue();
        break;
      case IDENT_FIELD:
        value = score.getIdentityP();
        break;
      case SIMIL_FIELD:
        value = score.getPositiveP();
        break;
      case GAPS_FIELD:
        value = score.getGapsP();
        break;
      case COVERAGE_FIELD:
        value = hsp.getQueryCoverage();
        break;
      case HCOVERAGE_FIELD:
        value = hsp.getHitCoverage();
        break;
      case SCORE_FIELD:
        value = score.getScore();
        break;
      default:
        value = score.getBitScore();
        break;
    }
    return value;
  }

  /**
   * Given a bitScore return the color defined by this color policy.
   * 
   * @param hsp
   *          a SRHsp object
   * @param alpha
   *          use alpha transparency or not
   * 
   * @return a Color
   */
  public Color getHitColor(SRHsp hsp, boolean alpha) {
    HitColorPolicyAtom atom;
    int i;
    double value;

    if (_hitListColorPolicy == null || _hitListColorPolicy.length == 0)
      return Color.BLACK;
    value = getValue(hsp, _hlcField);
    for (i = 0; i < _hitListColorPolicy.length; i++) {
      atom = _hitListColorPolicy[i];
      if (value >= atom.getThreshold()) {
        return atom.getColor(alpha);
      }
    }
    return (Color.black);
  }

  /**
   * Given a bitScore return the quality defined by this color policy.
   * 
   * @param hsp
   *          a SRHsp object
   * 
   * @return a quality value
   */
  public int getQualityValue(SRHsp hsp) {
    HitQualityPolicyAtom atom;
    int i;
    double value;

    if (_hitListQualityPolicy == null || _hitListQualityPolicy.length == 0)
      return 0;
    value = getValue(hsp, _hlqField);
    for (i = 0; i < _hitListQualityPolicy.length; i++) {
      atom = _hitListQualityPolicy[i];
      if (value >= atom.getThreshold()) {
        return _hitListQualityPolicy.length - i;
      }
    }
    return (0);
  }

  /**
   * Given a bitScore returns the quality icon defined by this color policy.
   * 
   * @param hsp
   *          a SRHsp object
   * 
   * @return a quality icon
   */
  public ImageIcon getQualityIcon(SRHsp hsp) {
    HitQualityPolicyAtom atom;
    int i;
    double value;

    if (_hitListQualityPolicy == null || _hitListQualityPolicy.length == 0)
      return null;
    value = getValue(hsp, _hlqField);
    for (i = 0; i < _hitListQualityPolicy.length; i++) {
      atom = _hitListQualityPolicy[i];
      if (value >= atom.getThreshold()) {
        return atom.getQualityIcon();
      }
    }
    return (QUALITY_SMILEY[QUALITY_SMILEY.length - 1]);
  }

  /**
   * Return the field identifier to use to get colors. Returned value is one of
   * XXX_FIELD. Using such ID can be used to query the array FIELDS contained in
   * this class to get a human readable representation of this ID.
   */
  public int getFieldForColor() {
    return _hlcField;
  }

  /**
   * Set the field identifier to use to get colors. 
   * 
   * @param val one of
   * XXX_FIELD. Using such ID can be used to query the array FIELDS contained in
   * this class to get a human readable representation of this ID.
   */
  public void setFieldForColor(int val) {
    _hlcField = val;
  }

  /**
   * Return the field identifier to use to get quality icons. Returned value is
   * one of XXX_FIELD. Using such ID can be used to query the array FIELDS
   * contained in this class to get a human readable representation of this ID.
   */
  public int getFieldForQuality() {
    return _hlqField;
  }

  /**
   * Set the field identifier to use to get quality icons. 
   * 
   * @param val
   * one of XXX_FIELD. Using such ID can be used to query the array FIELDS
   * contained in this class to get a human readable representation of this ID.
   */
  public void setFieldForQuality(int val) {
    _hlqField = val;
  }

  /**
   * Return the full color policy.
   * 
   * @return a color policy instance
   */
  public HitColorPolicyAtom[] getHitListColorPolicy() {
    return _hitListColorPolicy;
  }

  /**
   * Set a full color policy.
   * 
   * @param listColorPolicy a color policy instance
   */
  public void setHitListColorPolicy(HitColorPolicyAtom[] listColorPolicy) {
    _hitListColorPolicy = listColorPolicy;
  }

  /**
   * Return the full quality policy.
   * 
   * @return a quality policy instance
   */
  public HitQualityPolicyAtom[] getHitListQualityPolicy() {
    return _hitListQualityPolicy;
  }

  /**
   * Set a full quality policy.
   * 
   * @param listQualityPolicy a quality policy instance
   */
  public void setHitListQualityPolicy(HitQualityPolicyAtom[] listQualityPolicy) {
    _hitListQualityPolicy = listQualityPolicy;
  }

  /**
   * Dump the current color policy within this class logger.
   */
  public void dumpConfig() {
    int i;

    if (_hitListColorPolicy == null || _hitListColorPolicy.length == 0) {
      EZLogger.info("Color Policy: config empty");
    } else {
      EZLogger.info("Color Policy: " + FIELDS[_hlcField]);
      for (i = 0; i < _hitListColorPolicy.length; i++) {
        EZLogger.info(_hitListColorPolicy[i].toString());
      }
    }
    if (_hitListQualityPolicy == null || _hitListQualityPolicy.length == 0) {
      EZLogger.info("Quality Policy: config empty");
    } else {
      EZLogger.info("Quality Policy: " + FIELDS[_hlqField]);
      for (i = 0; i < _hitListQualityPolicy.length; i++) {
        EZLogger.info(_hitListQualityPolicy[i].toString());
      }
    }
  }

  private String getClrRepr(Color clr) {
    StringBuffer szBuf;
    szBuf = new StringBuffer();
    szBuf.append(clr.getRed());
    szBuf.append(",");
    szBuf.append(clr.getGreen());
    szBuf.append(",");
    szBuf.append(clr.getBlue());
    return szBuf.toString();
  }

  /**
   * Figure out whether or not to use inverse video mode.
   * 
   * @param useRV true or false
   */
  public void setUseInverseVideo(boolean useRV) {
    _useInvVideo = useRV;
  }

  /**
   * Figure out whether or not to use inverse video mode.
   * 
   * @return true or false.
   */
  public boolean useInverseVideo() {
    return _useInvVideo;
  }

  /**
   * Save the content of this ColorConfigPolicy in its configuration file.
   */
  public void save() throws IOException {
    FileOutputStream fos = null;
    fos = new FileOutputStream(_confPath + CONFIG_FILE_NAME);
    try {
      save(fos);
    } catch (IOException e) {
      throw e;
    } finally {
      IOUtils.closeQuietly(fos);
    }
  }

  /**
   * Save the content of this ColorConfigPolicy. This method terminates with a
   * flush on the OutputStream AND close it.
   * 
   * @param os the output stream
   */
  private void save(OutputStream os) throws IOException {
    HitColorPolicyAtom atomClr;
    HitQualityPolicyAtom atomQual;
    PrintWriter writer;
    String id;
    int i;

    writer = new PrintWriter(new OutputStreamWriter(os));

    // save HitColorPolicyTable name
    if (_hitListColorPolicy != null) {
      writer.print(HLC_CLASSES);
      writer.print("=");
      for (i = 0; i < _hitListColorPolicy.length; i++) {
        writer.print("c" + (i + 1));
        if ((i + 1) < _hitListColorPolicy.length) {
          writer.print(",");
        }
      }
      writer.print("\n");
      writer.print(HLC_FIELD);
      writer.print("=");
      writer.print(_hlcField);
      writer.print("\n");
      for (i = 0; i < _hitListColorPolicy.length; i++) {
        atomClr = _hitListColorPolicy[i];
        id = "c" + String.valueOf(i + 1);
        writer.print(HLC_CLASS + id + THRESH_SUFFIX + "=");
        writer.print(atomClr.getThreshold());
        writer.print("\n");
        writer.print(HLC_CLASS + id + CLR_SUFFIX + "=");
        writer.print(getClrRepr(atomClr.getColor(false)));
        writer.print("\n");
      }
    }
    writer.print("\n");
    // save HitQualityPolicyTable name
    if (_hitListQualityPolicy != null) {
      writer.print(HLQ_CLASSES);
      writer.print("=");
      for (i = 0; i < _hitListQualityPolicy.length; i++) {
        writer.print("c" + (i + 1));
        if ((i + 1) < _hitListQualityPolicy.length) {
          writer.print(",");
        }
      }
      writer.print("\n");
      writer.print(HLQ_FIELD);
      writer.print("=");
      writer.print(_hlqField);
      writer.print("\n");
      for (i = 0; i < _hitListQualityPolicy.length; i++) {
        atomQual = _hitListQualityPolicy[i];
        id = "c" + String.valueOf(i + 1);
        writer.print(HLQ_CLASS + id + THRESH_SUFFIX + "=");
        writer.print(atomQual.getThreshold());
        writer.print("\n");
        writer.print(HLQ_CLASS + id + QUAL_SUFFIX + "=");
        writer.print(atomQual.getIconId());
        writer.print("\n");
      }
    }
    writer.print("\n");
    // save reverse video usage
    writer.print(MSA_REVERSE_VIDEO_KEY);
    writer.print("=");
    writer.print(useInverseVideo() ? "true" : "false");
    writer.print("\n\n");
    // save table bk color
    writer.print(TABLE_BK_COLOR_KEY);
    writer.print("=");
    writer.print(getClrRepr(BK_COLOR));
    writer.print("\n\n");
    // save transparency usage
    writer.print(TRANSPARENCY_KEY);
    writer.print("=");
    writer.print(_useTransparency ? "true" : "false");
    writer.print("\n\n");
    writer.flush();
    writer.close();
  }
}
