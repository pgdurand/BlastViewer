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

import javax.swing.ImageIcon;

/**
 * This class stores information related to a single quality policy. Such a
 * policy corresponds to a bit score and its associated quality icon.
 * 
 * @author Patrick G. Durand
 */
public class HitQualityPolicyAtom {
  private double    _threshold;
  private int       _iconId;
  private ImageIcon _qualityIcon;

  /**
   * Default constructor.
   */
  public HitQualityPolicyAtom() {
  }

  /**
   * Constructor.
   * 
   * @param threshold
   *          threshold
   * @param iconId
   *          icon id
   * @param qIcon
   *          quality icon.
   */
  public HitQualityPolicyAtom(double threshold, int iconId, ImageIcon qIcon) {
    setThreshold(threshold);
    setQualityIcon(qIcon);
    setIconId(iconId);
  }

  /**
   * Return the threshold.
   * 
   * @return a threshold
   */
  public double getThreshold() {
    return _threshold;
  }

  /**
   * Set the threshold.
   * 
   * @param val
   *          threshold
   */
  public void setThreshold(double val) {
    _threshold = val;
  }

  /**
   * Return the quality icon
   * 
   * @return an icon
   * */
  public ImageIcon getQualityIcon() {
    return _qualityIcon;
  }

  /**
   * Set the quality icon
   * 
   * @param icon
   *          an icon
   * */
  public void setQualityIcon(ImageIcon icon) {
    _qualityIcon = icon;
  }

  public int getIconId() {
    return _iconId;
  }

  /**
   * Set the icon identifier.
   * 
   * @param id
   *          an identifier
   */
  public void setIconId(int id) {
    _iconId = id;
  }

  public String toString() {
    StringBuffer szBuf;
    szBuf = new StringBuffer();
    szBuf.append(_threshold);
    szBuf.append(": [");
    szBuf.append(_iconId);
    szBuf.append("]");
    return szBuf.toString();
  }
}
