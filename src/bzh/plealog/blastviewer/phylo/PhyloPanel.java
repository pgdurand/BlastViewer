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
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import bzh.plealog.bioinfo.api.data.sequence.DSequenceAlignment;
import bzh.plealog.blastviewer.resources.BVMessages;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.plealog.genericapp.api.EZEnvironment;
import com.plealog.genericapp.ui.common.ComponentPrintManagerAction;
import com.plealog.genericapp.ui.common.ImageManagerAction;
import com.plealog.genericapp.ui.common.OptionMenuButton;
import com.plealog.genericapp.ui.common.SearchField;

import epos.algo.construction.aglomerative.AglomerativeClustering;
import epos.algo.construction.nj.NJTree;
import epos.model.graph.methods.DFS;
import epos.model.matrix.DistanceMatrixModel;
import epos.model.tree.Tree;
import epos.model.tree.TreeNode;
import epos.ui.view.ppane.PowerPane;
import epos.ui.view.ppane.View.Antialiasing;
import epos.ui.view.treeview.ColorManager;
import epos.ui.view.treeview.ColorStyle;
import epos.ui.view.treeview.ImmutableException;
import epos.ui.view.treeview.TreeContent;
import epos.ui.view.treeview.TreeView;
import epos.ui.view.treeview.components.ComponentManager;
import epos.ui.view.treeview.components.NodeComponent;
import epos.ui.view.treeview.components.SelectionListener;
import epos.ui.view.treeview.components.SelectionManager;
import epos.ui.view.treeview.layouts.CircularLayout;
import epos.ui.view.treeview.layouts.DendogramLayout;
import epos.ui.view.treeview.renderer.NodeSelectRenderer;
import epos.ui.view.treeview.renderer.ZoomMode;

/**
 * This class implements a phylogenetic tree viewer relying on the EPOS library.
 * 
 * @author Patrick G. Durand
 */
public abstract class PhyloPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = -7546601604839818098L;
  protected JPanel                           _phyloPanel;
  protected Map<String, TreeNode>            _nodeMap;
  protected PowerPane<TreeView, TreeContent> _phyloViewer;
  protected Tree                             _tree;
  protected String[]                         _headers;
  protected String[]                         _sequences;
  private PhyloZoomAction                    _zoomInAction;
  private PhyloZoomAction                    _zoomOutAction;
  private PhyloZoomAction                    _zoomFitAction;
  private ExportTreeAction                   _treeExporter;
  private ImageManagerAction                 _imager;
  private ComponentPrintManagerAction        _printer;
  private JCheckBoxMenuItem                  _circLayoutBox;
  private JCheckBoxMenuItem                  _dendLayoutBox;
  private JCheckBoxMenuItem                  _expandLeavesBox;
  private JCheckBoxMenuItem                  _showValuesBox;
  private JCheckBoxMenuItem                  _antiAliasBox;
  private JCheckBoxMenuItem                  _txtAntiAliasBox;
  private JCheckBoxMenuItem                  _clipLabelBox;
  private JCheckBoxMenuItem                  _dynFntResizeBox;
  private JComboBox<MethodEntry>             _methodChoice;
  private JComboBox<CorrectionEntry>         _correctionChoice;
  private TreeLayoutType                     _treeLayoutType;
  private NodeSelectionListener              _nodeSelectionListener;
  private SearchField                        _searcher;
  private String                             _lastSearch;
  private boolean                            _expandLeaves;
  private boolean                            _showValues;
  protected boolean                          _lockDisplay;
  private boolean                            _antiAlias;
  private boolean                            _txtAntiAlias;
  private boolean                            _clipLabel;
  private boolean                            _dynFntResize;
  private boolean                            _lockComputation;
  protected boolean                          _isProteic;
  private Font                               _fnt = new Font("Arial",
                                                      Font.PLAIN, 12);
  private int                                _correction;
  private int                                _method;

  protected static enum TreeLayoutType {
    DENDOGRAM, CIRCULAR
  };

  private static final int      NJ_METHOD               = 1;
  private static final int      UPGMA_METHOD            = 2;
  private static final int      WPGMA_METHOD            = 3;
  private static final int      SINGLE_LINKAGE_METHOD   = 4;
  private static final int      COMPLETE_LINKAGE_METHOD = 5;

  private static final int      KIM_CORRECTION          = 1;
  private static final int      JK_CORRECTION           = 2;

  private static final String[] METHOD_NAMES            = new String[] {
      "Neighbour Joining", "UPGMA", "WPGMA", "Single Linkage",
      "Complete Linkage"                               };

  private static final String   PROP1                   = "phylo.expand.leaves";
  private static final String   PROP2                   = "phylo.show.value";
  private static final String   PROP3                   = "phylo.anti.alias";
  private static final String   PROP4                   = "phylo.txt.anti.alias";
  private static final String   PROP5                   = "phylo.clip.lbl";
  private static final String   PROP6                   = "phylo.font.resize";
  private static final String   PROP7                   = "phylo.layout";

  protected static final Font   MNU_DEF_FNT             = new Font(
                                                            "sans-serif",
                                                            Font.PLAIN, 12);

  /**
   * Constructor.
   */
  public PhyloPanel() {
    JPanel tBarPanel, mainPanel, corrPanel;
    JComponent compo;

    _phyloPanel = new JPanel(new BorderLayout());
    mainPanel = new JPanel(new BorderLayout());
    tBarPanel = new JPanel(new BorderLayout());
    corrPanel = new JPanel(new BorderLayout());

    prepareDefaults();

    createTreeLayoutPanel();
    tBarPanel.add(createTreeLayoutPanel(), BorderLayout.WEST);
    tBarPanel.add(getToolbar(), BorderLayout.EAST);

    _nodeSelectionListener = new NodeSelectionListener();

    corrPanel.add(createMethodPanel(), BorderLayout.EAST);
    compo = getHeaderPanel();
    if (compo != null)
      corrPanel.add(compo, BorderLayout.CENTER);

    mainPanel.add(corrPanel, BorderLayout.NORTH);
    mainPanel.add(_phyloPanel, BorderLayout.CENTER);
    compo = getFooterPanel();
    if (compo != null)
      mainPanel.add(compo, BorderLayout.SOUTH);
    
    this.setLayout(new BorderLayout());
    this.add(mainPanel, BorderLayout.CENTER);
    this.add(tBarPanel, BorderLayout.SOUTH);
    enableActions(false);
  }

  private JPanel createMethodPanel() {
    JPanel pnl, pnlMethod, pnlCorrection;
    JLabel lbl1, lbl2;

    _methodChoice = new JComboBox<>();
    _methodChoice.addItem(new MethodEntry(METHOD_NAMES[0], NJ_METHOD));
    _methodChoice.addItem(new MethodEntry(METHOD_NAMES[1], UPGMA_METHOD));
    _methodChoice.addItem(new MethodEntry(METHOD_NAMES[2], WPGMA_METHOD));
    _methodChoice.addItem(new MethodEntry(METHOD_NAMES[3],
        SINGLE_LINKAGE_METHOD));
    _methodChoice.addItem(new MethodEntry(METHOD_NAMES[4],
        COMPLETE_LINKAGE_METHOD));
    _methodChoice.addActionListener(new MethodComboListener());
    pnlMethod = new JPanel(new BorderLayout());
    lbl1 = new JLabel("Method:");
    lbl1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    pnlMethod.add(lbl1, BorderLayout.WEST);
    pnlMethod.add(_methodChoice, BorderLayout.EAST);

    _correctionChoice = new JComboBox<>();
    _correctionChoice.addItem(new CorrectionEntry("Kimura", KIM_CORRECTION));
    _correctionChoice
        .addItem(new CorrectionEntry("Jukes Cantor", JK_CORRECTION));
    _correctionChoice.addActionListener(new CorrectionComboListener());
    pnlCorrection = new JPanel(new BorderLayout());
    lbl2 = new JLabel("Correction:");
    lbl2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    pnlCorrection.add(lbl2, BorderLayout.WEST);
    pnlCorrection.add(_correctionChoice, BorderLayout.EAST);

    pnl = new JPanel(new BorderLayout());
    pnl.add(pnlMethod, BorderLayout.WEST);
    pnl.add(pnlCorrection, BorderLayout.EAST);
    pnl.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    return pnl;
  }

  /**
   * Sets the data used to create and display a distance Tree.
   * 
   * @param headers
   *          the array of sequence names
   * @param sequences
   *          the array of aligne sequences
   * @param isProteic
   *          true or false
   * 
   * @throws IllegalArgumentException
   *           if headers or sequences are null, empty are have not the same
   *           size.
   */
  public void setData(String[] headers, String[] sequences, boolean isProteic)
      throws IllegalArgumentException {

    Tree t;
    if (headers == null || headers.length == 0)
      throw new IllegalArgumentException("invalid headers array");
    if (sequences == null || sequences.length == 0)
      throw new IllegalArgumentException("invalid sequences array");
    if (sequences.length != headers.length)
      throw new IllegalArgumentException("arrays not compatible");

    _headers = headers;
    _sequences = sequences;
    _isProteic = isProteic;
    if (isProteic) {
      setDefaultMethod(NJ_METHOD);
      setDefaultCorrection(KIM_CORRECTION);
    } else {
      setDefaultMethod(NJ_METHOD);
      setDefaultCorrection(JK_CORRECTION);
    }
    t = computeTree();
    setTreeNodeLabels(t);
    setTree(t);
  }

  /**
   * Sets the default correction used to compute the tree.
   */
  private void setDefaultCorrection(int corr) {
    CorrectionEntry entry;
    int i, size, sel = 0;

    size = _correctionChoice.getModel().getSize();
    for (i = 0; i < size; i++) {
      entry = (CorrectionEntry) _correctionChoice.getModel().getElementAt(i);
      if (entry.getCorr() == corr) {
        sel = i;
        break;
      }
    }
    _lockComputation = true;
    _correctionChoice.setSelectedIndex(sel);
    _lockComputation = false;
  }

  /**
   * Sets the default method used to compute the tree.
   */
  private void setDefaultMethod(int method) {
    MethodEntry entry;
    int i, size, sel = 0;

    size = _methodChoice.getModel().getSize();
    for (i = 0; i < size; i++) {
      entry = (MethodEntry) _methodChoice.getModel().getElementAt(i);
      if (entry.getMethod() == method) {
        sel = i;
        break;
      }
    }
    _lockComputation = true;
    _methodChoice.setSelectedIndex(sel);
    _lockComputation = false;
  }

  /**
   * Sets the tree to display in this viewer.
   */
  protected void setTree(Tree t) {
    EnumSet<ColorManager.NodeColorizations> styles = EnumSet
        .of(ColorManager.NodeColorizations.NODE);
    TreeView v;
    TreeContent c;
    TreeNode tn;
    NodeComponent nc;
    ComponentManager compoManager;
    ColorManager clrManager;
    ZoomMode zm;

    _tree = t;
    c = new TreeContent(_tree);
    v = new TreeView(_tree);
    v.setAntialiasing(getAntiAliasProps());
    compoManager = v.getComponentManager();
    clrManager = v.getColorManager();
    tn = _tree.getRoot();
    clrManager.setColorStyle(new ColorStyle("Black on White", Color.white,
        Color.black, Color.black, Color.cyan, Color.black, Color.red,
        Color.green, false), false);
    _nodeMap = new Hashtable<String, TreeNode>();
    // the following loop aims at setting particular graphic properties to some
    // tree nodes. For example, the Query node is set to be red.
    for (TreeNode tn2 : tn.depthFirstIterator()) {
      if (tn2.getDistanceToParent() == (-0.0)) {
        tn2.getEdgeToParent().setWeight(0.0);
      }
      if (tn2.isLeaf()) {
        _nodeMap.put(PhyloUtils.getNodeId(tn2), tn2);
        nc = compoManager.getNodesComponent(tn2);
        try {
          if (DSequenceAlignment.REFERENCE_NAME.equals(PhyloUtils
              .getNodeId(tn2)))
            clrManager.setNodeColor(nc, Color.blue.darker(), styles);
          else
            clrManager.setNodeColor(nc, Color.black, styles);
        } catch (ImmutableException e) {
        }
      }
    }
    // A PhyloPanel contains the phyloViewer which can be displayed only when we
    // have a tree.
    // This is the reason of the following code architecture.
    if (_phyloViewer == null) {
      // no viewer yet: create one
      _phyloViewer = new PowerPane<TreeView, TreeContent>(v, c, null);
      // About the next line of code:
      // Using: (Border)UIManager.get("ScrollPane.border")
      // for the border of phyloViewer may cause a crash with particular Plaf:
      // javax.swing.plaf.metal.MetalBorders$ScrollPaneBorder.paintBorder(MetalBorders.java:899)
      // The paintBorder method takes as argument a JComponent and cast it as a
      // JScrollPane
      // resulting in a ClassCastException. No solution except using a more
      // basic
      // border!
      _phyloViewer.setBorder(BorderFactory.createLineBorder(Color.gray));
      _phyloPanel.add(_phyloViewer, BorderLayout.CENTER);
      NodeSelectRenderer nsr = new NodeSelectRenderer();
      nsr.setParentPP(_phyloViewer);
      _phyloViewer.registerTool(nsr);
      nsr.setNodeSelectionEnabled(true);
      nsr.setMouseOverSelectionEnabled(true);
      zm = new ZoomMode();
      zm.setParentPP(_phyloViewer);
      _zoomInAction.initZoomSystem(zm);
      _zoomOutAction.initZoomSystem(zm);
      _zoomFitAction.initZoomSystem(zm);
    } else {
      _phyloViewer.getView().getSelectionManager()
          .removeSelectionListener(_nodeSelectionListener);
      // viewer already created: just replace the content
      _phyloViewer.setContent(c);
      _phyloViewer.setView(v);
      NodeSelectRenderer nsr = new NodeSelectRenderer();
      nsr.setParentPP(_phyloViewer);
      _phyloViewer.registerTool(nsr);
      nsr.setNodeSelectionEnabled(true);
      nsr.setMouseOverSelectionEnabled(true);
    }
    _imager.setComponent(_phyloViewer.getView());
    _printer.setComponent(_phyloViewer.getView());
    _treeExporter.setTree(_tree);
    _searcher.setText("");
    v.getSelectionManager().addSelectionListener(_nodeSelectionListener);
    enableActions(true);
    setTreeLayout();
    setExpandLeaves();
    setShowValues();
    setTreeGraphics();
  }

  /**
   * This method can be overloaded to sets some special labels on each tree
   * node. This implementation does nothing.
   */
  protected void setTreeNodeLabels(Tree t) {

  }

  private int getCorrection(boolean isProteic) {
    int correction;

    correction = DistanceMatrixModel.DISS_STRICT // compare letter strictly
                                                 // (identity)
        | DistanceMatrixModel.EXCLUDE_INT_PW // exclude internal gap
    ;
    /*
     * correction = DistanceMatrixModel.EXCLUDE_INT_PW //exclude internal gap ;
     */

    switch (_correction) {
      case JK_CORRECTION:// Nucleic: Jukes-Cantor correction
        correction = correction | DistanceMatrixModel.CORR_JC;
        break;
      case KIM_CORRECTION:// Protein: Kimura correction
      default:
        correction = correction | DistanceMatrixModel.CORR_KIM;
        break;
    }
    return correction;

  }

  protected Tree computeTree() {
    DistanceMatrixModel dmm;
    AglomerativeClustering ac;
    NJTree njt;
    Tree t;
    double[][] matrix;
    int correction;

    if (_sequences == null || _headers == null)
      return null;
    EZEnvironment.setWaitCursor();
    correction = getCorrection(_isProteic);
    dmm = new DistanceMatrixModel(_sequences, _headers, 1f, _isProteic,
        correction);
    matrix = dmm.getDistances();
    if (_method == NJ_METHOD) {
      njt = new NJTree(matrix, _headers);
      t = njt.getTree();
    } else {
      switch (_method) {
        case WPGMA_METHOD:
          ac = new AglomerativeClustering(matrix, _headers,
              AglomerativeClustering.METHOD_WPGMA);
          break;
        case SINGLE_LINKAGE_METHOD:
          ac = new AglomerativeClustering(matrix, _headers,
              AglomerativeClustering.METHOD_SINGLE_LINKAGE);
          break;
        case COMPLETE_LINKAGE_METHOD:
          ac = new AglomerativeClustering(matrix, _headers,
              AglomerativeClustering.METHOD_COMPLETE_LINKAGE);
          break;
        case UPGMA_METHOD:
        default:
          ac = new AglomerativeClustering(matrix, _headers,
              AglomerativeClustering.METHOD_UPGMA);
          break;
      }
      t = ac.getTree();
    }
    PhyloUtils.setLeafNodeId(t);
    EZEnvironment.setDefaultCursor();
    return t;
  }

  /**
   * Resets the viewer content.
   */
  public void clear() {
    if (_phyloViewer != null) {
      _phyloPanel.remove(_phyloViewer);
      _phyloViewer.getView().getSelectionManager()
          .removeSelectionListener(_nodeSelectionListener);
      _phyloViewer = null;
      _imager.setComponent(null);
      _printer.setComponent(null);
      _treeExporter.setTree(null);
    }
    _searcher.setText("");
    enableActions(false);
    _nodeSelectionListener.reset();
    _tree = null;
    _sequences = null;
    _headers = null;
    this.repaint();
  }

  /**
   * Returns the tree currently displayed in this viewer.
   */
  public Tree getTree() {
    return _tree;
  }

  protected void enableActions(boolean enable) {
    _zoomInAction.setEnabled(enable);
    _zoomOutAction.setEnabled(enable);
    _zoomFitAction.setEnabled(enable);
    _treeExporter.setEnabled(enable);
    _printer.setEnabled(enable);
    _imager.setEnabled(enable);
    _expandLeavesBox.setEnabled(enable);
    _showValuesBox.setEnabled(enable);
    _antiAliasBox.setEnabled(enable);
    _txtAntiAliasBox.setEnabled(enable);
    _clipLabelBox.setEnabled(enable);
    _dynFntResizeBox.setEnabled(enable);
    _circLayoutBox.setEnabled(enable);
    _dendLayoutBox.setEnabled(enable);
    _searcher.setEnabled(enable);
    _methodChoice.setEnabled(enable);
    _correctionChoice.setEnabled(enable);
  }

  protected void prepareDefaults() {
    String val;
    val = EZEnvironment.getApplicationProperty(PROP1);
    if (val != null)
      _expandLeaves = Boolean.valueOf(val);
    else
      _expandLeaves = true;
    val = EZEnvironment.getApplicationProperty(PROP2);
    if (val != null)
      _showValues = Boolean.valueOf(val);
    else
      _showValues = false;
    val = EZEnvironment.getApplicationProperty(PROP3);
    if (val != null)
      _antiAlias = Boolean.valueOf(val);
    else
      _antiAlias = true;
    val = EZEnvironment.getApplicationProperty(PROP4);
    if (val != null)
      _txtAntiAlias = Boolean.valueOf(val);
    else
      _txtAntiAlias = true;
    val = EZEnvironment.getApplicationProperty(PROP5);
    if (val != null)
      _clipLabel = Boolean.valueOf(val);
    else
      _clipLabel = false;
    val = EZEnvironment.getApplicationProperty(PROP6);
    if (val != null)
      _dynFntResize = Boolean.valueOf(val);
    else
      _dynFntResize = true;
    val = EZEnvironment.getApplicationProperty(PROP7);
    if (val != null) {
      if (val.equals(TreeLayoutType.CIRCULAR.name()))
        _treeLayoutType = TreeLayoutType.CIRCULAR;
      else if (val.equals(TreeLayoutType.DENDOGRAM.name()))
        _treeLayoutType = TreeLayoutType.DENDOGRAM;
      else
        _treeLayoutType = TreeLayoutType.DENDOGRAM;
    } else {
      _treeLayoutType = TreeLayoutType.DENDOGRAM;
    }
  }

  /**
   * You can overload this method if you want to add a component at the top of
   * the PhyloPanel.
   */
  protected JComponent getHeaderPanel() {
    return null;
  }

  /**
   * You can overload this method if you want to add a component at the bottom
   * of the PhyloPanel. The FooterPanel is actually located between the
   * PhyloPanel and the ToolBar.
   */
  protected JComponent getFooterPanel() {
    return null;
  }

  protected JToolBar getToolbar() {
    ImageIcon icon;
    JToolBar tBar;
    JButton btn;

    tBar = new JToolBar();
    tBar.setFloatable(false);

    // Zoom fit to window
    icon = EZEnvironment.getImageIcon("zoom_fit.png");
    if (icon != null) {
      _zoomFitAction = new PhyloZoomAction("", icon);
    } else {
      _zoomFitAction = new PhyloZoomAction(
          BVMessages.getString("PhyloZoomAction.zoomFit.btn"));
    }
    _zoomFitAction.setZoomMode(PhyloZoomAction.ZOOM_MODE.ZOOM_FIT);
    btn = tBar.add(_zoomFitAction);
    btn.setToolTipText(BVMessages.getString("PhyloZoomAction.zoomFit.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("PhyloZoomAction.zoomFit.btn"));
    // Zoom in
    icon = EZEnvironment.getImageIcon("zoom_in.png");
    if (icon != null) {
      _zoomInAction = new PhyloZoomAction("", icon);
    } else {
      _zoomInAction = new PhyloZoomAction(
          BVMessages.getString("PhyloZoomAction.zoomIn.btn"));
    }
    _zoomInAction.setZoomMode(PhyloZoomAction.ZOOM_MODE.ZOOM_IN);
    btn = tBar.add(_zoomInAction);
    btn.setToolTipText(BVMessages.getString("PhyloZoomAction.zoomIn.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("PhyloZoomAction.zoomIn.btn"));
    // Zoom out
    icon = EZEnvironment.getImageIcon("zoom_out.png");
    if (icon != null) {
      _zoomOutAction = new PhyloZoomAction("", icon);
    } else {
      _zoomOutAction = new PhyloZoomAction(
          BVMessages.getString("PhyloZoomAction.zoomOut.btn"));
    }
    _zoomOutAction.setZoomMode(PhyloZoomAction.ZOOM_MODE.ZOOM_OUT);
    btn = tBar.add(_zoomOutAction);
    btn.setToolTipText(BVMessages.getString("PhyloZoomAction.zoomOut.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("PhyloZoomAction.zoomOut.btn"));
    tBar.addSeparator();
    // export the tree
    icon = EZEnvironment.getImageIcon("save.png");
    if (icon != null) {
      _treeExporter = new ExportTreeAction("", icon);
    } else {
      _treeExporter = new ExportTreeAction(
          BVMessages.getString("PhyloManagerAction.save.btn"));
    }
    btn = tBar.add(_treeExporter);
    btn.setToolTipText(BVMessages.getString("PhyloManagerAction.save.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("PhyloManagerAction.save.btn"));
    // print an image
    icon = EZEnvironment.getImageIcon("print.png");
    if (icon != null) {
      _printer = new ComponentPrintManagerAction("", icon);
    } else {
      _printer = new ComponentPrintManagerAction(
          BVMessages.getString("PhyloManagerAction.print.btn"));
    }
    btn = tBar.add(_printer);
    btn.setToolTipText(BVMessages.getString("PhyloManagerAction.print.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("PhyloManagerAction.print.btn"));
    // save n image of the tree
    icon = EZEnvironment.getImageIcon("imager.png");
    if (icon != null) {
      _imager = new ImageManagerAction("", icon);
    } else {
      _imager = new ImageManagerAction(
          BVMessages.getString("PhyloImagerAction.save.btn"));
    }
    btn = tBar.add(_imager);
    btn.setToolTipText(BVMessages.getString("PhyloImagerAction.save.toolTip"));
    // if(AbstractConfig.showLabelForUIToolbar())
    // btn.setText(BVMessages.getString("PhyloImagerAction.save.btn"));
    return tBar;
  }

  protected JCheckBoxMenuItem createChkBoxMnu(String val) {
    JCheckBoxMenuItem item;

    item = new JCheckBoxMenuItem(val);
    item.setFont(MNU_DEF_FNT);
    return item;
  }

  protected JMenuItem createMnu(String val) {
    JMenuItem item;

    item = new JMenuItem(val);
    item.setFont(MNU_DEF_FNT);
    return item;
  }

  protected Component createTreeLayoutPanel() {
    DefaultFormBuilder builder;
    FormLayout layout;
    OptionMenuButton btn;

    _circLayoutBox = createChkBoxMnu(BVMessages
        .getString("PhyloPanel.layout.circ"));
    _circLayoutBox.setSelected(_treeLayoutType == TreeLayoutType.CIRCULAR);
    _circLayoutBox.addActionListener(new CircLayoutAction());

    _dendLayoutBox = createChkBoxMnu(BVMessages
        .getString("PhyloPanel.layout.dend"));
    _dendLayoutBox.setSelected(_treeLayoutType == TreeLayoutType.DENDOGRAM);
    _dendLayoutBox.addActionListener(new DendLayoutAction());

    _expandLeavesBox = createChkBoxMnu(BVMessages
        .getString("PhyloPanel.opt1.lbl"));
    _expandLeavesBox.setSelected(_expandLeaves);
    _expandLeavesBox.addActionListener(new ExpandLeavesAction());

    _showValuesBox = createChkBoxMnu(BVMessages
        .getString("PhyloPanel.opt2.lbl"));
    _showValuesBox.setSelected(_showValues);
    _showValuesBox.addActionListener(new ShowValuesAction());

    _antiAliasBox = createChkBoxMnu(BVMessages.getString("PhyloPanel.opt3.lbl"));
    _antiAliasBox.setSelected(_antiAlias);
    _antiAliasBox.addActionListener(new AntiAliasAction());

    _txtAntiAliasBox = createChkBoxMnu(BVMessages
        .getString("PhyloPanel.opt4.lbl"));
    _txtAntiAliasBox.setSelected(_txtAntiAlias);
    _txtAntiAliasBox.addActionListener(new TxtAntiAliasAction());

    _clipLabelBox = createChkBoxMnu(BVMessages.getString("PhyloPanel.opt5.lbl"));
    _clipLabelBox.setSelected(_clipLabel);
    _clipLabelBox.addActionListener(new ClipLabelAction());

    _dynFntResizeBox = createChkBoxMnu(BVMessages
        .getString("PhyloPanel.opt6.lbl"));
    _dynFntResizeBox.setSelected(_dynFntResize);
    _dynFntResizeBox.addActionListener(new DynFntResizeAction());

    btn = new OptionMenuButton(BVMessages.getString("PhyloPanel.options.lbl"));
    btn.setPopup(getOptionPopupMenu());

    _searcher = new SearchField();
    _searcher.setHelperText(BVMessages.getString("PhyloPanel.search.lbl"));
    _searcher.setFont(_fnt);
    _searcher.setToolTipText(BVMessages.getString("PhyloPanel.search.toolTip"));
    _searcher.addPropertyChangeListener(SearchField.PROPERTY_TEXT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            String l = evt.getNewValue().toString();
            if (l.equals(_lastSearch) || l.matches("\\s+")) {
            } else {
              setSelectedNodes(search(l));
            }
          }
        });
    layout = new FormLayout("50dlu, 10dlu, 100dlu",// , 10dlu,
                                                   // right:max(20dlu;p), 2dlu,
                                                   // 60dlu",
        "");
    builder = new DefaultFormBuilder(layout);
    builder.setDefaultDialogBorder();
    builder.append(btn);
    builder.append(_searcher);
    return builder.getContainer();
  }

  protected JPopupMenu getOptionPopupMenu() {
    JPopupMenu mnu;
    JMenu mn;

    mnu = new JPopupMenu();
    mnu.add(_expandLeavesBox);
    mnu.add(_showValuesBox);
    mn = new JMenu(BVMessages.getString("PhyloPanel.graphics.lbl"));
    mn.setFont(MNU_DEF_FNT);
    mn.add(_antiAliasBox);
    mn.add(_txtAntiAliasBox);
    // mn.add(_clipLabelBox);
    // mn.add(_dynFntResizeBox);
    mnu.add(mn);
    mn = new JMenu(BVMessages.getString("PhyloPanel.layout.lbl"));
    mn.setFont(MNU_DEF_FNT);
    mn.add(_circLayoutBox);
    mn.add(_dendLayoutBox);
    mnu.add(mn);

    return mnu;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<TreeNode> search(String patternString) {
    if (patternString == null || patternString.equals("") || _tree == null) {
      _lastSearch = "";
      return null;
    }
    _lastSearch = patternString;

    patternString = patternString.replaceAll("\\s+", "*");
    patternString = "*" + patternString + "*";
    patternString = patternString.replaceAll("\\*", "\\.\\*");
    patternString = patternString.replaceAll("\\?", "\\.");
    ArrayList<TreeNode> l = new ArrayList<TreeNode>();
    DFS dfs = new DFS(_tree);
    Iterator it = dfs.iterator(_tree.getRoot());
    Pattern p = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
    while (it.hasNext()) {
      TreeNode node = (TreeNode) it.next();
      String label = node.getLabel();
      if (label == null)
        continue;
      if (p.matcher(label).matches()) {
        l.add(node);
      }
    }
    Collections.reverse(l);
    return l;
  }

  protected void setExpandLeaves() {
    if (_phyloViewer == null)
      return;
    ((DendogramLayout) _phyloViewer.getView().getTreeLayout())
        .setExpandLeaves(_expandLeaves);
  }

  protected void setShowValues() {
    if (_phyloViewer == null)
      return;
    _phyloViewer.getView().getComponentManager().setDrawEdgeLabels(_showValues);
  }

  protected void setTreeGraphics() {
    if (_phyloViewer == null)
      return;
    _phyloViewer.getView().setAntialiasing(getAntiAliasProps());
    _phyloViewer.getView().getComponentManager()
        .setDynamicFontResizing(_dynFntResize);
    _phyloViewer.getView().getComponentManager().setClipNodeLabels(_clipLabel);
    _phyloViewer.getView().setForceBufferRefresh(true);
    _phyloViewer.getView().repaint();
  }

  protected void setTreeLayout() {
    if (_phyloViewer == null)
      return;
    switch (_treeLayoutType) {
      case CIRCULAR:
        _phyloViewer.getView().setLayout(new CircularLayout());
        break;
      case DENDOGRAM:
      default:
        _phyloViewer.getView().setLayout(new DendogramLayout());
        break;
    }
  }

  /**
   * Returns the leaf nodes currently selected on the tree.
   */
  public List<String> getSelectedNodes() {
    return _nodeSelectionListener.getNodes();
  }

  /**
   * Sets the node to be selected on the tree.
   */
  /*
   * public void setSelectedNode(TreeNode tn){ if (_phyloViewer==null) return;
   * if (tn==null){
   * _phyloViewer.getView().getSelectionManager().setSelection(null); } else{
   * ArrayList<NodeComponent> lst; lst = new ArrayList<NodeComponent>();
   * lst.add(
   * _phyloViewer.getView().getComponentManager().getNodesComponent(tn));
   * _phyloViewer.getView().getSelectionManager().setSelection(lst); } }
   */
  /**
   * Sets the node to be selected on the tree.
   */
  public void setSelectedNodes(List<TreeNode> lotn) {
    if (_phyloViewer == null)
      return;
    if (lotn == null || lotn.isEmpty()) {
      _phyloViewer.getView().getSelectionManager().setSelection(null);
    } else {
      ArrayList<NodeComponent> lst;
      lst = new ArrayList<NodeComponent>();
      for (TreeNode tn : lotn) {
        lst.add(_phyloViewer.getView().getComponentManager()
            .getNodesComponent(tn));
      }
      _phyloViewer.getView().getSelectionManager().setSelection(lst);
    }
  }

  protected class TreeLayoutComboListener implements ActionListener {
    @SuppressWarnings("rawtypes")
    public void actionPerformed(ActionEvent e) {
      JComboBox cb = (JComboBox) e.getSource();
      _lockDisplay = true;
      _expandLeavesBox.setSelected(true);
      _treeLayoutType = ((TreeLayoutEntry) cb.getSelectedItem())
          .getTreeLayoutType();
      setTreeLayout();
      _lockDisplay = false;
    }
  }

  private class TreeLayoutEntry {
    private String         _lbl;
    private TreeLayoutType _type;

    public TreeLayoutType getTreeLayoutType() {
      return _type;
    }

    public String toString() {
      return _lbl;
    }
  }

  private EnumSet<Antialiasing> getAntiAliasProps() {
    return (EnumSet.of(_antiAlias ? Antialiasing.AA_ON : Antialiasing.AA_OFF,
        _txtAntiAlias ? Antialiasing.AA_TEXT_ON : Antialiasing.AA_TEXT_OFF));
  }

  private class CircLayoutAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      if (_lockDisplay)
        return;
      if (((JCheckBoxMenuItem) event.getSource()).isSelected()) {
        _treeLayoutType = TreeLayoutType.CIRCULAR;
        EZEnvironment.setApplicationProperty(PROP7, _treeLayoutType.name());
        _lockDisplay = true;
        _dendLayoutBox.setSelected(false);
        _expandLeavesBox.setSelected(true);
        setTreeLayout();
        _lockDisplay = false;
      }
    }
  }

  private class DendLayoutAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      if (_lockDisplay)
        return;
      if (((JCheckBoxMenuItem) event.getSource()).isSelected()) {
        _treeLayoutType = TreeLayoutType.DENDOGRAM;
        EZEnvironment.setApplicationProperty(PROP7, _treeLayoutType.name());
        _lockDisplay = true;
        _circLayoutBox.setSelected(false);
        _expandLeavesBox.setSelected(true);
        setTreeLayout();
        _lockDisplay = false;
      }
    }
  }

  private class ExpandLeavesAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      _expandLeaves = ((JCheckBoxMenuItem) event.getSource()).isSelected();
      if (_lockDisplay)
        return;
      EZEnvironment.setApplicationProperty(PROP1,
          _expandLeaves ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
      setExpandLeaves();
    }
  }

  private class ShowValuesAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      _showValues = ((JCheckBoxMenuItem) event.getSource()).isSelected();
      if (_lockDisplay)
        return;
      EZEnvironment.setApplicationProperty(PROP2,
          _showValues ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
      setShowValues();
    }
  }

  private class AntiAliasAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      _antiAlias = ((JCheckBoxMenuItem) event.getSource()).isSelected();
      if (_lockDisplay)
        return;
      EZEnvironment.setApplicationProperty(PROP3,
          _antiAlias ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
      setTreeGraphics();
    }
  }

  private class TxtAntiAliasAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      _txtAntiAlias = ((JCheckBoxMenuItem) event.getSource()).isSelected();
      if (_lockDisplay)
        return;
      EZEnvironment.setApplicationProperty(PROP4,
          _txtAntiAlias ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
      setTreeGraphics();
    }
  }

  private class ClipLabelAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      _clipLabel = ((JCheckBoxMenuItem) event.getSource()).isSelected();
      if (_lockDisplay)
        return;
      EZEnvironment.setApplicationProperty(PROP5,
          _clipLabel ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
      setTreeGraphics();
    }
  }

  private class DynFntResizeAction implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      _dynFntResize = ((JCheckBoxMenuItem) event.getSource()).isSelected();
      if (_lockDisplay)
        return;
      EZEnvironment.setApplicationProperty(PROP6,
          _dynFntResize ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
      setTreeGraphics();
    }
  }

  /**
   * This is a convenient method called when nodes are selected on the Tree.
   * This avoid to add another ListenerSomething to get the result of such
   * selection.
   * 
   * @param node
   *          the selected nodes or null
   */
  protected abstract void nodeSelected(List<String> id);

  /**
   * This class listen to the node selection events. To do something with the
   * selected node catched up by this class, just implement the abstract method
   * nodeSelected(TreeNode) that is called by the class.
   */
  private class NodeSelectionListener implements SelectionListener {
    private List<String> _csn;

    public List<String> getNodes() {
      return _csn;
    }

    public void setNodes(List<String> id) {
      _csn = id;
      nodeSelected(id);
    }

    public void reset() {
      _csn = null;
    }

    public void selectionChanged(SelectionManager manager) {
      Collection<NodeComponent> selNodes;
      List<String> ids;
      TreeNode tn;

      // EPOS node selection system is quite strange. Whatever the selection
      // action
      // (add, remove, etc), the listener always provides a Collection of Nodes.
      // The
      // following code aims at locating only the newly single selected leaf
      // node.
      selNodes = manager.getSelectedNodes();

      // nothing selected: reset the node selection
      if (selNodes.isEmpty()) {
        setNodes(null);
        return;
      }
      // given the EPOS collection of nodes, only gets the leaf nodes
      ids = new ArrayList<String>();
      for (NodeComponent nc : selNodes) {
        if (nc.getNode().isLeaf()) {
          tn = nc.getNode();
          ids.add(PhyloUtils.getNodeId(tn));
        }
      }
      // no leaf nodes: reset the node selection
      if (ids.isEmpty()) {
        setNodes(null);
        return;
      }
      // propagate the selection
      setNodes(ids);
    }
  }

  protected class CorrectionComboListener implements ActionListener {
    @SuppressWarnings("rawtypes")
    public void actionPerformed(ActionEvent e) {
      JComboBox cb = (JComboBox) e.getSource();

      CorrectionEntry entry;
      entry = (CorrectionEntry) cb.getSelectedItem();
      _correction = entry.getCorr();
      if (_lockComputation)
        return;
      setTree(computeTree());
    }
  }

  protected class MethodComboListener implements ActionListener {
    @SuppressWarnings("rawtypes")
    public void actionPerformed(ActionEvent e) {
      JComboBox cb = (JComboBox) e.getSource();

      MethodEntry entry;
      entry = (MethodEntry) cb.getSelectedItem();
      _method = entry.getMethod();
      if (_lockComputation)
        return;
      setTree(computeTree());
    }
  }

  private class CorrectionEntry {
    private String _name;
    private int    _corr;

    public CorrectionEntry(String name, int corr) {
      super();
      this._name = name;
      this._corr = corr;
    }

    public int getCorr() {
      return _corr;
    }

    public String toString() {
      return _name;
    }
  }

  private class MethodEntry {
    private String _name;
    private int    _method;

    public MethodEntry(String name, int corr) {
      super();
      this._name = name;
      this._method = corr;
    }

    public int getMethod() {
      return _method;
    }

    public String toString() {
      return _name;
    }
  }
}
