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
package bzh.plealog.blastviewer.util;

public class HTTPEngineException extends RuntimeException {

  private static final long serialVersionUID = 4332507260541550768L;

  private int httpCode;
  private String url;
  
  /** 1001: unable to create response file, i.e. the file in which HTTPBasicEngine 
   * writes server answer.
   */
  public static final int HTTPEX_TMP_FILE_ERROR = 1001;
  /** 1002: unable to write in response file.
   */
  public static final int HTTPEX_WRITE_FILE_ERROR = 1002;
  
  /** 1003: time out.
   */
  public static final int HTTPEX_TIMEOUT = 1003;

  @SuppressWarnings("unused")
  private HTTPEngineException() {  }

  public HTTPEngineException(String message, String url, int httpcode) {
    super(message);
    this.url = url;
    this.httpCode = httpcode;
  }

  /**
   * Return the URL responsible for the exception.
   */
  public String getUrl(){
    return url;
  }
  
  /**
   * Return standard HTTP code. In addition, we define some additional values using
   * HTTPEX_xxx constants. 
   * */
  public int getHttpCode() {
    return httpCode;
  }

  /**
   * Figures out whether or not we have a problem with response file.
   */
  public boolean isTmpFileError(){
    return httpCode>=1000;
  }

  /**
   * Figures out whether or not we have an HTTP error code of class 5xx.
   */
  public boolean isServerError(){
    return httpCode>=500;
  }
  /**
   * Figures out whether or not we have an HTTP error code 400.
   */
  public boolean isBadRequest(){
    return httpCode==400;
  }
  /**
   * Figures out whether or not we have an HTTP error code 404.
   */
  public boolean isWrongUrl(){
    return httpCode==400;
  }
}
