package classes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DefaultPieDataset;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.GroupAction;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.PolarLocationAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.FontAction;
import prefuse.action.layout.CollapsedSubtreeLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.HoverActionControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JSearchPanel;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.TreeDepthItemSorter;

/**
 *
 * @author fabinhosano
 */
public final class Visualize extends Display {

    private final String tree = "tree";
    private final String treeNodes = "tree.nodes";
    private final String treeEdges = "tree.edges";
    private final String linear = "linear";
    private LabelRenderer m_nodeRenderer;
    private EdgeRenderer m_edgeRenderer;
    private String m_label = "label";
    private FileParser fileParser;
    private Graph graph;
    private JPanel jPanel;
    private RadialTreeLayout radialTreeLayout;
    private NodeLinkTreeLayout treeLayout;
    private ActionList filter;
    private JRadioButton radioButtonRadial;
    private JRadioButton radioButtonTree;

    public Visualize() {
        super(new Visualization());

        UILib.setPlatformLookAndFeel();

        String label = "id";
        demo(label);

        setGraphicsImageFactory(37, 21);
        setBackgroundPanel();
    }

    public JPanel demo() {
        return demo("id");
    }

    public JPanel demo(final String label) {
        Table data = new Table();

        try {
            data.addColumn("id", int.class);
            data.addColumn("name", String.class);

            graph = new Graph(data, false);

            fileParser = new FileParser();
            fileParser.readProbabilistcModelFile();

            //Map para verificar nós inseridos.
            Map<String, Node> nos = new HashMap<>();
            Node noRaiz = null;

            int j = 2;

            //inserindo nó raiz;
            for (int i = 0; i < fileParser.getGraphicalData().size(); ++i) {
                //verificando se o nó é a raiz;
                if ("".equals(fileParser.getGraphicalData().get(i).getPathNode())) {
                    //inserindo nó raiz;
                    noRaiz = graph.addNode();
                    noRaiz.set("id", 1);
                    noRaiz.set("name", "Raiz");
                    //inserindo nó no map de nós
                    nos.put(fileParser.getGraphicalData().get(i).getPathNode(), noRaiz);
                    break; // encontrou o nó raiz
                }
            }

            //inserindo restante dos nós
            for (int i = 0; i < fileParser.getGraphicalData().size(); ++i) {
                //verificando se o nó não é a raiz
                if (!"".equals(fileParser.getGraphicalData().get(i).getPathNode())) {
                    //inserindo nó;
                    Node node = graph.addNode();
                    node.set("id", j);
                    node.set("name", fileParser.getGraphicalData().get(i).getPathNode());

                    //inserindo nó no map de nós
                    nos.put(fileParser.getGraphicalData().get(i).getPathNode(), node);

                    //atualizando indice dos nós
                    ++j;
                }
            }

            //percorrendo nós do map e criando á estrutra da árvore
            for (Map.Entry<String, Node> paths : nos.entrySet()) {
                // pegando dados do nó a ser ligado
                String path = paths.getKey();
                Node node = paths.getValue();

                for (Map.Entry<String, Node> caminhos : nos.entrySet()) {
                    // pegando dados do nó que poderá receber o nó ligado
                    String caminho = caminhos.getKey();
                    Node n = caminhos.getValue();

                    //verificação se o nó é filho da raiz
                    if (path.length() <= 1) {
                        //identificou o nó como filho da raiz e o ligou ao pai
                        graph.addEdge(noRaiz, node);
                        break; //inseriu aresta entre os nós
                    } else {
                        //verifica se o path é o filho do nó caminho especificado
                        if (caminho.equals(path.substring(0, path.length() - 1))) {
                            //identificou o nó path como filho do nó caminho
                            graph.addEdge(n, node);
                            break; //inseriu aresta entre os nós
                        }
                    }
                }
            }

        } catch (Exception e) {
            //e.printStackTrace();
        }

        return demo(graph, label);
    }

    public JPanel demo(Graph g, final String label) {
        // create a new radial tree view
        m_label = label;

        // -- set up visualization --
        //m_vis.add(tree, g);
        //m_vis.setInteractive(treeEdges, null, false);

        // -- set up renderers --
        m_nodeRenderer = new LabelRenderer("name", "id");
        m_nodeRenderer.setRenderType(AbstractShapeRenderer.RENDER_TYPE_FILL);
        m_nodeRenderer.setHorizontalAlignment(Constants.CENTER);
        m_nodeRenderer.setRoundedCorner(8, 8);

        // -- set up edge types --
        m_edgeRenderer = new EdgeRenderer(Constants.EDGE_TYPE_LINE, Constants.EDGE_ARROW_FORWARD);
        m_edgeRenderer.setArrowType(Constants.EDGE_ARROW_FORWARD);
        m_edgeRenderer.setArrowHeadSize(10, 10);


        DefaultRendererFactory rf = new DefaultRendererFactory(m_nodeRenderer, m_edgeRenderer);
        //rf.add(new InGroupPredicate(treeEdges), m_edgeRenderer);
        m_vis.setRendererFactory(rf);

        Table nodes = graph.getNodeTable();
        Table egdes = graph.getEdgeTable();

        Graph grafo = new Graph(nodes, egdes, true);
        m_vis.add(tree, grafo);

        // -- set up processing actions --

        // colors
        ItemAction nodeColor = new NodeColorAction(treeNodes);
        ItemAction textColor = new TextColorAction(treeNodes);
        m_vis.putAction("textColor", textColor);


        ItemAction edgeColor = new EdgeColorAction(treeEdges);

        FontAction fonts = new FontAction(treeNodes,
                FontLib.getFont("Tahoma", 10));
        fonts.add("ingroup('_focus_')", FontLib.getFont("Tahoma", 11));

        // recolor
        ActionList recolor = new ActionList();
        recolor.add(nodeColor);
        recolor.add(textColor);
        m_vis.putAction("recolor", recolor);

        // repaint
        ActionList repaint = new ActionList();
        repaint.add(recolor);
        repaint.add(new RepaintAction());
        m_vis.putAction("repaint", repaint);

        // animate paint change
        ActionList animatePaint = new ActionList(400);
        animatePaint.add(new ColorAnimator(treeNodes));
        animatePaint.add(new RepaintAction());
        m_vis.putAction("animatePaint", animatePaint);

        // create the tree layout action
        treeLayout = new NodeLinkTreeLayout(tree);
        treeLayout.setOrientation(Constants.ORIENT_TOP_BOTTOM);
        m_vis.putAction("treeLayout", treeLayout);

        radialTreeLayout = new RadialTreeLayout(tree);
        //treeLayout.setAngularBounds(TOP_ALIGNMENT, Math.PI);
        m_vis.putAction("radialTreeLayout", radialTreeLayout);

        CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(tree);
        m_vis.putAction("subLayout", subLayout);

        // create the filtering and layout
        filter = new ActionList();
        filter.add(new TreeRootAction(tree));
        filter.add(fonts);
        filter.add(treeLayout);
        filter.add(radialTreeLayout);
        filter.add(subLayout);
        filter.add(textColor);
        filter.add(nodeColor);
        filter.add(edgeColor);
        filter.add(new ColorAction(treeEdges, VisualItem.FILLCOLOR, ColorLib.rgb(100, 100, 100)));
        m_vis.putAction("filter", filter);

        // animated transition
        ActionList animate = new ActionList(1250);
        animate.setPacingFunction(new SlowInSlowOutPacer());
        animate.add(new QualityControlAnimator());
        animate.add(new VisibilityAnimator(tree));
        animate.add(new PolarLocationAnimator(treeNodes, linear));
        animate.add(new ColorAnimator(treeNodes));
        animate.add(new RepaintAction());
        m_vis.putAction("animate", animate);
        m_vis.alwaysRunAfter("filter", "animate");

        // ------------------------------------------------

        // initialize the display
        setSize(1365, 722);
        setItemSorter(new TreeDepthItemSorter());
        addControlListener(new DragControl());
        addControlListener(new ZoomToFitControl());
        addControlListener(new ZoomControl());
        addControlListener(new PanControl());
        addControlListener(new FocusControl(1, "filter"));
        addControlListener(new HoverActionControl("repaint"));


        // ------------------------------------------------

        // filter graph and perform layout
        m_vis.run("filter");

        // maintain a set of items that should be interpolated linearly
        // this isn't absolutely necessary, but makes the animations nicer
        // the PolarLocationAnimator should read this set and act accordingly
        m_vis.addFocusGroup(linear, new DefaultTupleSet());
        m_vis.getGroup(Visualization.FOCUS_ITEMS).addTupleSetListener(
                new TupleSetListener() {
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                TupleSet linearInterp = m_vis.getGroup(linear);
                if (add.length < 1) {
                    return;
                }
                linearInterp.clear();
                for (Node n = (Node) add[0]; n != null; n = n.getParent()) {
                    linearInterp.addTuple(n);
                }
            }
        });

        SearchTupleSet search = new PrefixSearchTupleSet();
        m_vis.addFocusGroup(Visualization.SEARCH_ITEMS, search);
        search.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
                m_vis.cancel("animatePaint");
                m_vis.run("recolor");
                m_vis.run("animatePaint");
            }
        });


        Visualization vis = getVisualization();


        // create a search panel for the tree map
        SearchQueryBinding sq = new SearchQueryBinding(
                (Table) vis.getGroup(treeNodes), "name",
                (SearchTupleSet) vis.getGroup(Visualization.SEARCH_ITEMS));
        JSearchPanel searchPanel = sq.createSearchPanel();
        searchPanel.setShowResultCount(true);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
        searchPanel.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));

        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));

        addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
                if (item.canGetString(label)) {
                    title.setText(item.getString("name"));
                }
            }

            public void itemExited(VisualItem item, MouseEvent e) {
                title.setText(null);
            }
        });

        //Botões de Rádio
        radioButtonRadial = new JRadioButton("Radial", Boolean.TRUE);
        radioButtonRadial.setMnemonic(KeyEvent.VK_P);
        radioButtonRadial.setActionCommand("Radial");
        
        radioButtonTree = new JRadioButton("Tree", Boolean.FALSE);
        radioButtonTree.setMnemonic(KeyEvent.VK_P);
        radioButtonTree.setActionCommand("Tree");

        //Group the radio buttons.
        ButtonGroup botoes = new ButtonGroup();
        botoes.add(radioButtonRadial);
        botoes.add(radioButtonTree);

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(radioButtonRadial, BorderLayout.CENTER);
        box.add(radioButtonTree, BorderLayout.CENTER);
        box.add(Box.createHorizontalStrut(320));
        box.add(searchPanel);
        box.add(Box.createHorizontalStrut(3));

        radioButtonRadial.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_vis.removeAction("filter");
                
                setSelectedRadioButtonRadial(true);
                setSelectedRadioButtonTree(false);
                
                filter.add(radialTreeLayout);
                filter.remove(treeLayout);
                
                m_vis.putAction("filter", filter);
                m_vis.run("filter");
            }
        });
        
        radioButtonTree.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                m_vis.removeAction("filter");
                
                setSelectedRadioButtonTree(true);
                setSelectedRadioButtonRadial(false);
                
                filter.add(treeLayout);
                filter.remove(radialTreeLayout);
                
                m_vis.putAction("filter", filter);
                m_vis.run("filter");
            }
        });
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(this, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);
        
        Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.DARK_GRAY;
        UILib.setColor(panel, BACKGROUND, FOREGROUND);

        setPanel(panel);

        return panel;
    }

    private void setGraphicsImageFactory(int width, int height) {
        try {
            LoadedImageFactory loadedImageFactory = new LoadedImageFactory();
            loadedImageFactory.cleanImageCache();

            HashMap<String, DefaultPieDataset> graphics = new HashMap<>();

            for (int i = 0; i < fileParser.getGraphicalData().size(); i++) {
                GraphicalData gd = fileParser.getGraphicalData().get(i);
                graphics.put(("" + (i + 1)), gd.generationDefaultPieDataset());
            }

            loadedImageFactory.setGraficos(graphics);
            loadedImageFactory.setMaxImageDimensions(width, height);

            m_nodeRenderer.setImageFactory(loadedImageFactory);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void setBackgroundPanel() {
        LegendTitle legend = null;

        //Percorrendo nós para pegar legenda do nó raiz
        for (int i = 0; i < fileParser.getGraphicalData().size(); ++i) {
            //verificando se o nó é a raiz;
            if ("".equals(fileParser.getGraphicalData().get(i).getPathNode())) {
                JFreeChart jFreeChart = ChartFactory.createPieChart3D(
                        "", fileParser.getGraphicalData().get(i)
                        .generationDefaultPieDataset(), true, true, false);
                PiePlot plotagem = (PiePlot) jFreeChart.getPlot();
                plotagem.setLabelGenerator(new StandardPieSectionLabelGenerator(
                        "{0} ({2})"));//define porcentagem no gráfico  
                plotagem.setForegroundAlpha(0.2f);
                plotagem.setLabelBackgroundPaint(new Color(220, 220, 220));
                plotagem.setBackgroundPaint(Color.white);

                legend = jFreeChart.getLegend();
            }
        }

        //Criando Legenda com a legenda do Nó Raiz
        DefaultPieDataset defaultPieDataset = new DefaultPieDataset();
        JFreeChart freeChart = ChartFactory.createPieChart3D("", defaultPieDataset);
        freeChart.addLegend(legend);

        setBackgroundImage(freeChart.createBufferedImage(
                getWidth(), getHeight() - 55), true, true);
    }

    private void setPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }

    public JPanel getPanel() {
        return jPanel;
    }

    public void setSelectedRadioButtonRadial(Boolean selected) {
        this.radioButtonRadial.setSelected(selected);
    }

    public void setSelectedRadioButtonTree(Boolean selected) {
        this.radioButtonTree.setSelected(selected);
    }
    

    // ------------------------------------------------------------------------
    /**
     * Switch the root of the tree by requesting a new spanning tree at the
     * desired root
     */
    public class TreeRootAction extends GroupAction {

        public TreeRootAction(String graphGroup) {
            super(graphGroup);
        }

        public void run(double frac) {
            TupleSet focus = m_vis.getGroup(Visualization.FOCUS_ITEMS);
            if (focus == null || focus.getTupleCount() == 0) {
                return;
            }

            Graph g = (Graph) m_vis.getGroup(m_group);
            Node f = null;
            Iterator tuples = focus.tuples();
            while (tuples.hasNext() && !g.containsTuple(f = (Node) tuples.next())) {
                f = null;
            }
            if (f == null) {
                return;
            }
            g.getSpanningTree(f);
        }
    }

    /**
     * Set node fill colors
     */
    public class NodeColorAction extends ColorAction {

        public NodeColorAction(String group) {
            super(group, VisualItem.FILLCOLOR, ColorLib.rgba(255, 255, 255, 0));
            add("_hover", ColorLib.gray(220, 230));
            add("ingroup('_search_')", ColorLib.rgb(255, 190, 190));
            add("ingroup('_focus_')", ColorLib.rgb(198, 229, 229));
        }
    } // end of inner class NodeColorAction

    public class EdgeColorAction extends ColorAction {

        public EdgeColorAction(String group) {
            super(group, VisualItem.STROKECOLOR, ColorLib.rgb(0, 0, 255));
        }
    }

    /**
     * Set node text colors
     */
    public class TextColorAction extends ColorAction {

        public TextColorAction(String group) {
            super(group, VisualItem.TEXTCOLOR, ColorLib.gray(0));
            add("_hover", ColorLib.rgb(255, 0, 0));
        }
    }
}
