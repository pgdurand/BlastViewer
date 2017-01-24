package bzh.plealog.blastviewer.config;

import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.file.EZFileTypes;

/**
 * Defines some file extension of general purpose for the whole software.
 * 
 * @author Patrick G. Durand
 */
public class FileExtension {

  public static final String MSF_FEXT = "msf";
  public static final String FAS_FEXT = "fas";
  public static final String PHY_FEXT = "phy";
  public static final String NEX_FEXT = "nex";
  public static final String NEW_FEXT = "new";
  public static final String FAS_A_FEXT = "faa";//Fasta aligned
  public static final String FAS_NA_FEXT = "fna";//Fasta not aligned

  public static void initialize() {
    EZFileTypes.registerFileType(NEX_FEXT,
        BVMessages.getString("DDFileTypes.nex.name"));
    EZFileTypes.registerFileType(NEW_FEXT,
        BVMessages.getString("DDFileTypes.new.name"));
    EZFileTypes.registerFileType(MSF_FEXT,
        BVMessages.getString("DDFileTypes.msf.name"));
    EZFileTypes.registerFileType(PHY_FEXT,
        BVMessages.getString("DDFileTypes.phy.name"));
    EZFileTypes.registerFileType(FAS_A_FEXT,
        BVMessages.getString("DDFileTypes.fasa.name"));
    EZFileTypes.registerFileType(FAS_NA_FEXT,
        BVMessages.getString("DDFileTypes.fasna.name"));
  }
}
