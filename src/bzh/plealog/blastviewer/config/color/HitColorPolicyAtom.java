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

/**
 * This class stores information related to a single color policy. Such a policy
 * corresponds to a bit score and its associated color.
 * 
 * @author Patrick G. Durand
 */
public class HitColorPolicyAtom {
  private double _threshold;
  private Color  _clr          = Color.BLACK;
  private Color  _clrWithAlpha = Color.black;

  /**
   * Default constructor.
   */
  public HitColorPolicyAtom() {
  }

  /**
   * Constructor with a threshold and its associated color.
   */
  public HitColorPolicyAtom(long threshold, Color clr) {
    setThreshold(threshold);
    setColor(clr);
  }

  /**
   * Return the bit score.
   */
  public double getThreshold() {
    return _threshold;
  }

  /**
   * Set the bit score.
   */
  public void setThreshold(double val) {
    _threshold = val;
  }

  /**
   * Return the color.
   */
  public Color getColor(boolean alpha) {
    if (alpha)
      return _clrWithAlpha;
    else
      return _clr;
  }

  /**
   * Set the color.
   * 
   * @param clr
   *          a color without alpha channel. This method will create a
   *          corresponding color with an alpha channel that can be retrieved
   *          using this getColor method.
   */
  public void setColor(Color clr) {
    if (clr == null)
      _clr = Color.BLACK;
    else
      _clr = clr;
    _clrWithAlpha = new Color(_clr.getRed(), _clr.getGreen(), _clr.getBlue(),
        ColorPolicyConfigImplem.TRANSPARENCY_FACTOR);
  }

  /**
   * Return a string representation of this color policy.
   * 
   * */
  public String getClrRepr() {
    StringBuffer szBuf;
    szBuf = new StringBuffer();
    szBuf.append(_clr.getRed());
    szBuf.append(",");
    szBuf.append(_clr.getGreen());
    szBuf.append(",");
    szBuf.append(_clr.getBlue());
    return szBuf.toString();
  }

  /**
   * Return a string representation of this color policy.
   * 
   * */
  public String toString() {
    StringBuffer szBuf;
    szBuf = new StringBuffer();
    szBuf.append(_threshold);
    szBuf.append(": [");
    szBuf.append(getClrRepr());
    szBuf.append("]");
    return szBuf.toString();
  }
}
