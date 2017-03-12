/* Copyright (C) 2008-2017 Patrick G. Durand
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
package bzh.plealog.blastviewer.phylo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import bzh.plealog.bioinfo.api.data.sequence.BankSequenceInfo;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHSP;
import bzh.plealog.bioinfo.ui.blast.core.BlastHitHspImplem;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListEvent;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListListener;
import bzh.plealog.bioinfo.ui.blast.event.BlastHitListSupport;
import bzh.plealog.bioinfo.ui.blast.hittable.BlastHitTable;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.plealog.genericapp.api.EZEnvironment;

import epos.model.tree.Tree;
import epos.model.tree.TreeNode;

/**
 * This class adds to a PhyloPanel the links with Blast hits data.
 * 
 * @author Patrick G. Durand
 */
public class HspPhyloPanel extends PhyloPanel {
  private static final long serialVersionUID = -8277487034283772644L;
  private MyBlastHitListListener         _bhhListener;
  private BlastHitListSupport            _updateSpport;
  private Map<String, BlastHitHspImplem> _nodeBhhMap;
  private boolean                        _bLockSelection;
  private List<String>                   _lastHspID;
  private JLabel                         _header;
  private BlastHitTable                  _hitList;
  private JMenuItem                      _seqIdLblBox;
  private JMenuItem                      _seqDefLblBox;
  private JMenuItem                      _orgFullLblBox;
  private JMenuItem                      _orgMedLblBox;
  private JMenuItem                      _orgCompLblBox;
  private LabelIDType                    _lblType;
  private int                            _maxLabelSize = 50;

  protected static enum LabelIDType {
    SEQID, DEFINITION, ORGANISM_FULL, ORGANISM_MEDIUM, ORGANISM_COMPACT
  };

  private static final MessageFormat HEADER_FORMATTER = new MessageFormat(
                                                          BVMessages
                                                              .getString("PhyloPanel.header"));

  private static final String        PROP8            = "phylo.label.type";
  private static final String        PROP9            = "phylo.label.size";

  /**
   * Constructor.
   */
  public HspPhyloPanel() {
    super();
    _bhhListener = new MyBlastHitListListener();
  }

  protected void prepareDefaults() {
    super.prepareDefaults();
    String val;

    val = EZEnvironment.getApplicationProperty(PROP8);
    if (val != null) {
      if (val.equals(LabelIDType.SEQID.name()))
        _lblType = LabelIDType.SEQID;
      else if (val.equals(LabelIDType.DEFINITION.name()))
        _lblType = LabelIDType.DEFINITION;
      else if (val.equals(LabelIDType.ORGANISM_FULL.name()))
        _lblType = LabelIDType.ORGANISM_FULL;
      else if (val.equals(LabelIDType.ORGANISM_MEDIUM.name()))
        _lblType = LabelIDType.ORGANISM_MEDIUM;
      else if (val.equals(LabelIDType.ORGANISM_COMPACT.name()))
        _lblType = LabelIDType.ORGANISM_COMPACT;
      else
        _lblType = LabelIDType.SEQID;
    } else {
      _lblType = LabelIDType.SEQID;
    }

    val = EZEnvironment.getApplicationProperty(PROP9);
    if (val != null) {
      try {
        _maxLabelSize = Integer.valueOf(val);
      } catch (NumberFormatException e) {
        _maxLabelSize = 50;
      }
    }
  }

  protected void setTree(Tree t) {
    super.setTree(t);
    ArrayList<TreeNode> lotn;
    List<String> ids = getSelectedNodes();
    ArrayList<String> ids2;

    if (ids == null)
      ids = _lastHspID;
    if (ids == null)
      return;
    ids2 = new ArrayList<String>();
    for (String id : ids) {
      if (_nodeMap.containsKey(id)) {
        ids2.add(id);
      }
    }
    if (ids2.isEmpty())
      return;
    _lastHspID = ids2;
    _bLockSelection = true;
    lotn = new ArrayList<TreeNode>();
    for (String id : ids2) {
      lotn.add(_nodeMap.get(id));
    }
    setSelectedNodes(lotn);
    _bLockSelection = false;
  }

  // to be call AFTER setTree()
  /**
   * Sets a Map of BlastHitHsp data. In this map, the keys are the Tree Node
   * Ids. This method must be call after a call to setTree().
   */
  public void setMap(Map<String, BlastHitHspImplem> nodeBhhMap) {
    _nodeBhhMap = nodeBhhMap;
    if (_nodeBhhMap == null)
      return;
    BlastHitHspImplem bhh;
    if (_lastHspID != null && _lastHspID.size() == 1) {
      bhh = _nodeBhhMap.get(_lastHspID.get(0));
      _hitList.setDataModel(new BlastHitHspImplem[] { bhh });
    } else {
      _hitList.resetDataModel();
    }
  }

  public void setMSAInfo(String msg) {
    _header.setText(msg);
    _header.setVisible(true);
  }

  public void setMSAInfo(int nHsp, int from, int to) {
    _header.setText(HEADER_FORMATTER.format(new Object[] { nHsp, from, to }));
    _header.setVisible(true);
  }

  /**
   * Register to the central notification system handling selection of hits on
   * the various viewers.
   */
  public void registerHitListSupport(BlastHitListSupport us) {
    _updateSpport = us;
    us.addBlastHitListListener(_bhhListener);
  }

  /**
   * Resets the viewer content.
   */
  public void clear() {
    super.clear();
    _hitList.resetDataModel();
    _nodeBhhMap = null;
    _header.setText("");
    _header.setVisible(false);
  }

  protected JComponent getHeaderPanel() {
    JPanel pnl;

    _header = new JLabel();
    _header.setOpaque(false);
    _header.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
    pnl = new JPanel(new BorderLayout());
    pnl.add(_header, BorderLayout.CENTER);
    pnl.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    return pnl;
  }

  protected JComponent getFooterPanel() {
    JPanel pnl, pnl2;

    pnl = new JPanel(new BorderLayout());
    pnl2 = new JPanel(new BorderLayout());
    _hitList = getBlastHitList();
    Dimension dim = new Dimension(50, 150);
    _hitList.setMinimumSize(dim);
    _hitList.setPreferredSize(dim);
    pnl.add(new JLabel(BVMessages.getString("HspPhyloPanel.table.header")),
        BorderLayout.NORTH);
    pnl.add(_hitList, BorderLayout.CENTER);
    // pnl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    pnl.setOpaque(true);
    // pnl.setBackground(new Color(255,255,228));
    pnl2.add(pnl, BorderLayout.CENTER);
    pnl2.setBorder(UIManager.getDefaults().getBorder("TextField.border"));
    return pnl2;
  }

  protected void nodeSelected(List<String> ids) {
    if (_nodeBhhMap == null)
      return;
    if (_bLockSelection)
      return;
    List<BlastHitHSP> hits;
    BlastHitHSP bhh;
    BlastHitHSP abhh[];

    if (ids == null) {
      _hitList.resetDataModel();
      hits = null;
    } else {
      hits = new ArrayList<BlastHitHSP>();
      // check if we have the a bhh object for each node
      for (String id : ids) {
        bhh = _nodeBhhMap.get(id);
        if (bhh != null)
          hits.add(bhh);
      }
      if (hits.isEmpty()) {
        hits = null;
      } else {
        // set data in the Hit List table. Only single selection
        // is accepted here
        if (hits.size() == 1) {
          abhh = new BlastHitHSP[hits.size()];
          _hitList.setDataModel(hits.toArray(abhh));
        } else {
          _hitList.resetDataModel();
        }
      }
    }
    _updateSpport.fireHitChange(new BlastHitListEvent(HspPhyloPanel.this, hits,
        BlastHitListEvent.HIT_CHANGED));
  }

  protected BlastHitTable getBlastHitList() {
    BlastHitTable bl;

    bl = new BlastHitTable("msap");//ConfigManager.getHitTableFactory().createViewer();

    return bl;
  }

  protected void enableActions(boolean enable) {
    super.enableActions(enable);
    _seqIdLblBox.setEnabled(enable);
    _seqDefLblBox.setEnabled(enable);
    _orgFullLblBox.setEnabled(enable);
    _orgMedLblBox.setEnabled(enable);
    _orgCompLblBox.setEnabled(enable);
  }

  protected JPopupMenu getOptionPopupMenu() {
    JPopupMenu mnu;
    JMenu mn;

    mnu = super.getOptionPopupMenu();
    _seqIdLblBox = createMnu(BVMessages.getString("PhyloPanel.label.seqId"));
    _seqIdLblBox.addActionListener(new ShowLabelAction(LabelIDType.SEQID));
    _seqDefLblBox = createMnu(BVMessages.getString("PhyloPanel.label.seqDef"));
    _seqDefLblBox
        .addActionListener(new ShowLabelAction(LabelIDType.DEFINITION));
    _orgFullLblBox = createMnu(BVMessages.getString("PhyloPanel.label.org1"));
    _orgFullLblBox.addActionListener(new ShowLabelAction(
        LabelIDType.ORGANISM_FULL));
    _orgMedLblBox = createMnu(BVMessages.getString("PhyloPanel.label.org2"));
    _orgMedLblBox.addActionListener(new ShowLabelAction(
        LabelIDType.ORGANISM_MEDIUM));
    _orgCompLblBox = createMnu(BVMessages.getString("PhyloPanel.label.org3"));
    _orgCompLblBox.addActionListener(new ShowLabelAction(
        LabelIDType.ORGANISM_COMPACT));

    mn = new JMenu(BVMessages.getString("PhyloPanel.label.lbl"));
    mn.setFont(MNU_DEF_FNT);
    mn.add(_seqIdLblBox);
    mn.add(_seqDefLblBox);
    mn.add(_orgFullLblBox);
    mn.add(_orgMedLblBox);
    mn.add(_orgCompLblBox);

    mnu.add(mn);

    return mnu;
  }

  private class MyBlastHitListListener implements BlastHitListListener {
    /**
     * Given a name and a number, returns an identifier.
     */
    private String getKey(String name, int num) {
      return name + "_" + num;
    }

    public void hitChanged(BlastHitListEvent e) {
      TreeNode tn;
      List<BlastHitHSP> hits;
      ArrayList<TreeNode> lotn = null;
      ArrayList<BlastHitHSP> lbhh = null;
      BlastHitHSP abhh[];
      String key;

      hits = e.getHitHsps();

      if (e.getSource() == HspPhyloPanel.this)
        return;
      // first, we check what we have received
      if (hits == null || hits.isEmpty()) {
        _lastHspID = null;
      } else {
        // always keep 'in mind' the Blast Hits selected somewhere else in the
        // application. In this way, when we set a new tree, we can
        // automatically
        // select the corresponding nodes. See setTree() and setMap() methods.
        _lastHspID = new ArrayList<String>();
        for (BlastHitHSP bhh : hits) {
          key = getKey(bhh.getHit().getHitAccession(), bhh.getHspNum());
          _lastHspID.add(key);
        }
      }
      if (_nodeMap == null || _nodeBhhMap == null || _phyloViewer == null)
        return;
      _bLockSelection = true;
      if (_lastHspID != null && !_lastHspID.isEmpty()) {
        // do the selection, after a test to figure out if the
        // Blast Hit ids can be located in the currently displayed tree.
        lotn = new ArrayList<TreeNode>();
        lbhh = new ArrayList<BlastHitHSP>();
        for (String id : _lastHspID) {
          tn = _nodeMap.get(id);
          if (tn == null)
            continue;
          lotn.add(tn);
          lbhh.add(_nodeBhhMap.get(id));
        }

        setSelectedNodes(lotn.isEmpty() ? null : lotn);
        // set some data in the Hit List table only when we have a single
        // hit selected
        if (lbhh.size() > 1) {
          _hitList.resetDataModel();
        } else {
          abhh = new BlastHitHSP[lbhh.size()];
          abhh = lbhh.toArray(abhh);
          _hitList.setDataModel(abhh);
        }
      } else {
        setSelectedNodes(null);
        _hitList.resetDataModel();
      }
      _bLockSelection = false;
    }
  }

  private String getOrganismLbl(String org, LabelIDType type) {
    StringTokenizer tokenizer;
    StringBuffer buf;
    String token;

    if (type == LabelIDType.ORGANISM_FULL)
      return org;
    tokenizer = new StringTokenizer(org, " ");
    if (tokenizer.countTokens() == 1)
      return org;
    buf = new StringBuffer();
    // handle Genus
    token = tokenizer.nextToken();
    if (type == LabelIDType.ORGANISM_COMPACT) {
      buf.append(token.charAt(0));// First char of genus
      buf.append(". ");
    } else {
      buf.append(token);// full genus
      buf.append(" ");
    }
    // handle species
    token = tokenizer.nextToken();
    buf.append(token);
    buf.append(" ");
    return buf.toString();
  }

  protected void setTreeNodeLabels(Tree t) {
    TreeNode tn;
    String id;
    BlastHitHSP bhh;
    BankSequenceInfo si;

    tn = t.getRoot();
    for (TreeNode tn2 : tn.depthFirstIterator()) {
      if (tn2.isLeaf()) {
        switch (_lblType) {
          case SEQID:
            tn2.setLabel(PhyloUtils.getNodeId(tn2));
            break;
          case DEFINITION:
            id = PhyloUtils.getNodeId(tn2);
            bhh = _nodeBhhMap.get(id);
            if (bhh == null)
              continue;
            id = bhh.getHit().getHitDef();
            if (id.length() > _maxLabelSize)
              id = id.substring(0, _maxLabelSize);
            tn2.setLabel(id);
            break;
          case ORGANISM_FULL:
          case ORGANISM_MEDIUM:
          case ORGANISM_COMPACT:
            id = PhyloUtils.getNodeId(tn2);
            bhh = _nodeBhhMap.get(id);
            if (bhh == null)
              continue;
            si = bhh.getHit().getSequenceInfo();
            if (si != null && si.getOrganism() != null)
              id = getOrganismLbl(si.getOrganism(), _lblType);
            else
              id = "?";
            if (id.length() > _maxLabelSize)
              id = id.substring(0, _maxLabelSize);
            tn2.setLabel(id);
            break;
        }
      }
    }
  }

  private class ShowLabelAction implements ActionListener {
    private LabelIDType idType;

    public ShowLabelAction(LabelIDType idType) {
      this.idType = idType;
    }

    public void actionPerformed(ActionEvent event) {
      if (_lockDisplay)
        return;
      _lockDisplay = true;
      _lblType = idType;
      EZEnvironment.setApplicationProperty(PROP8, _lblType.name());
      setTreeNodeLabels(_tree);
      _lockDisplay = false;
      setTree(_tree);
    }
  }
}
