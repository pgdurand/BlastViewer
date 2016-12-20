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
package bzh.plealog.blastviewer.resources;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class defines a utility class used to read the content of a resource
 * file where the GUI localized strings are saved.
 * 
 * @author Patrick G. Durand
 */
public class BVMessages {
  private static final String BUNDLE_NAME =
      BVMessages.class.getPackage().getName()+".messages";

  private static final ResourceBundle RESOURCE_BUNDLE = 
      ResourceBundle.getBundle(BUNDLE_NAME);

  /**
   * Default constructor not available. Use this class as static.
   */
  private BVMessages() {}

  /**
   * Returns a value associated to a given key.
   */
  public static String getString(String key) {
    try {
      return RESOURCE_BUNDLE.getString(key);
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
