package classes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
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
public class TreeVLMC extends Display {

    private static final String tree = "tree";
    private static final String treeNodes = "tree.nodes";
    private static final String treeEdges = "tree.edges";
    private static final String linear = "linear";
    private static LabelRenderer m_nodeRenderer;
    private EdgeRenderer m_edgeRenderer;
    private String m_label = "label";
    private static FileParser fileParser;
    private static Graph graph;
    private static TreeVLMC treeVLMC;

    public TreeVLMC(Graph g, String label) {

        super(new Visualization());
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
        NodeLinkTreeLayout treeLayout = new NodeLinkTreeLayout(tree);
        treeLayout.setOrientation(Constants.ORIENT_TOP_BOTTOM);
        m_vis.putAction("treeLayout", treeLayout);

        CollapsedSubtreeLayout subLayout = new CollapsedSubtreeLayout(tree);
        subLayout.setLayoutBounds(m_rclip);
        m_vis.putAction("subLayout", subLayout);

        // create the filtering and layout
        ActionList filter = new ActionList();
        filter.add(new TreeRootAction(tree));
        filter.add(fonts);
        filter.add(treeLayout);
        filter.add(subLayout);
        filter.add(textColor);
        filter.add(nodeColor);
        filter.add(edgeColor);
        //setando cor da ponta da flecha
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
        setSize(1360, 750);
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
    }

    public static void main(String argv[]) {
        String label = "id";
        LegendTitle legend = null;

        UILib.setPlatformLookAndFeel();

        JFrame frame = new JFrame("V L M C");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(demo(label));
        frame.pack();
        frame.setVisible(true);

        setGraphicsImageFactory(37, 21);

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

        treeVLMC.setBackgroundImage(freeChart
                .createBufferedImage(frame.getWidth(), frame.getHeight() - 55),
                true, true);
        

    }

    public static JPanel demo() {
        return demo("id");
    }

    public static JPanel demo(final String label) {
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
            e.printStackTrace();
        }
        return demo(graph, label);
    }

    public static JPanel demo(Graph g, final String label) {
        // create a new tree view
        treeVLMC = new TreeVLMC(g, label);
        Visualization vis = treeVLMC.getVisualization();


        // create a search panel for the tree map
        SearchQueryBinding sq = new SearchQueryBinding(
                (Table) vis.getGroup(treeNodes), "name",
                (SearchTupleSet) vis.getGroup(Visualization.SEARCH_ITEMS));
        JSearchPanel search = sq.createSearchPanel();
        search.setShowResultCount(true);
        search.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 0));
        search.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 11));

        final JFastLabel title = new JFastLabel("                 ");
        title.setPreferredSize(new Dimension(350, 20));
        title.setVerticalAlignment(SwingConstants.BOTTOM);
        title.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));

        treeVLMC.addControlListener(new ControlAdapter() {
            public void itemEntered(VisualItem item, MouseEvent e) {
                if (item.canGetString(label)) {
                    title.setText(item.getString("name"));
                }
            }

            public void itemExited(VisualItem item, MouseEvent e) {
                title.setText(null);
            }
        });

        Box box = new Box(BoxLayout.X_AXIS);
        box.add(Box.createHorizontalStrut(10));
        box.add(title);
        box.add(Box.createHorizontalGlue());
        box.add(search);
        box.add(Box.createHorizontalStrut(3));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(treeVLMC, BorderLayout.CENTER);
        panel.add(box, BorderLayout.SOUTH);

        Color BACKGROUND = Color.WHITE;
        Color FOREGROUND = Color.DARK_GRAY;
        UILib.setColor(panel, BACKGROUND, FOREGROUND);

        return panel;
    }

    private static void setGraphicsImageFactory(int width, int height) {
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
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Switch the root of the tree by requesting a new spanning tree at the
     * desired root
     */
    public static class TreeRootAction extends GroupAction {

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
    public static class NodeColorAction extends ColorAction {

        public NodeColorAction(String group) {
            super(group, VisualItem.FILLCOLOR, ColorLib.rgba(255, 255, 255, 0));
            add("_hover", ColorLib.gray(220, 230));
            add("ingroup('_search_')", ColorLib.rgb(255, 190, 190));
            add("ingroup('_focus_')", ColorLib.rgb(198, 229, 229));
        }
    } // end of inner class NodeColorAction

    public static class EdgeColorAction extends ColorAction {

        public EdgeColorAction(String group) {
            super(group, VisualItem.STROKECOLOR, ColorLib.rgb(0, 0, 255));
        }
    }

    /**
     * Set node text colors
     */
    public static class TextColorAction extends ColorAction {

        public TextColorAction(String group) {
            super(group, VisualItem.TEXTCOLOR, ColorLib.gray(0));
            add("_hover", ColorLib.rgb(255, 0, 0));
        }
    }
}