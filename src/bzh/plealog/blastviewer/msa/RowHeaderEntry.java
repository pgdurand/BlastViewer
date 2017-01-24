/* Copyright (C) 2006-2017 Patrick G. Durand
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
package bzh.plealog.blastviewer.msa;

import bzh.plealog.bioinfo.ui.blast.core.BlastHitHSP;


/**
 * This is the basic data element of a row of the MSA viewer. It aims at
 * maintaining a link between a row (a sequence of the MSA) and the
 * corresponding Blast HSP.
 * 
 * @author Patrick G. Durand
 * */
public class RowHeaderEntry {
  private String            name;
  private BlastHitHSP bhh;

  /**
   * Constructor.
   * 
   * @param name name to display as a row header of the MSA
   * @param bhh corresponding Blast HSP
   * */
  public RowHeaderEntry(String name, BlastHitHSP bhh) {
    super();
    this.name = name;
    this.bhh = bhh;
  }

  public String getName() {
    return name;
  }

  public BlastHitHSP getBhh() {
    return bhh;
  }

}