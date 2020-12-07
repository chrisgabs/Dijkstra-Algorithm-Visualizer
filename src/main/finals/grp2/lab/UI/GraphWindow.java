package main.finals.grp2.lab.UI;

import jdk.dynalink.linker.LinkerServices;
import main.finals.grp2.lab.Graph;
import main.finals.grp2.util.ArrayList;
import main.finals.grp2.util.Dictionary;
import main.finals.grp2.util.Queue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.EmptyStackException;
import java.util.Stack;

// COMMITS: WINDOW CENTERED, LAYOUT FIXES, PROMPT FILE SELECTION (NO FUNCTIONALITY)



/**
 * Main structure:
 * Frame -> mainPanel -> control and visual panel V
 *                                                -> control panel
 *                                                      -> inputPanel -> local components
 *                                                      -> tablePanel -> local components
 *                                                      -> actionPanel -> local components
 *                                                -> control panel
 *                                                      -> TODO: insert Graph Visualizer
 **/
public class GraphWindow {

    protected JFrame mainFrame;

    // main panel for all components
    protected JPanel mainPanel, titleBarPanel;

    // sub-panels for controls and visualizer
    protected JPanel controlPanel;
    protected JPanel visualPanel;
    GraphVisualizerCanvas graphCanvas;

    // sub panels for controlPanel;
    protected JPanel inputPanel, tablePanel, actionPanel;

    // panels in tablePanel (input and output);
    protected JPanel inputTablePanel, pathwayTablePanel;

    // button controllers
    protected JButton inputFileButton;
    protected final JButton[] actionButtons = new JButton[]{
            new JButton("Play"),
            new JButton("<<"),
            new JButton("<"),
            new JButton(">"),
            new JButton(">>"),
            new JButton("Set")
    };

    protected JLabel inputLabel, pathwayLabel, algorithmLabel;

    protected JPanel inputMainPanel, pathwayMainPanel;

    protected JPanel inputLabelPanel, pathwayLabelPanel;

    protected JTable inputTable, pathwayTable;

    protected JScrollPane inputTableScrollPane, pathwayTableScrollPane;

    protected JPanel fromToPanel, algoLabelPanel, algoSelectionPanel, playPanel, stepPanel;

    protected JTextField fromField, toField;

    protected JComboBox<String> algoSelectionBox;

    protected File textFile;

    // THEME: Bento
    protected Color mainColor = new Color(0x2D394D);
    protected Color secondaryColor = new Color(0x4A768D);
    protected Color accentColor = new Color(0xF87A90);
    protected Color mainForeground = Color.WHITE;
    protected Color secondaryForeground = Color.BLACK;

    VisualizerThread visualizerThread;
    private boolean paused = true;
    private Graph graph;
    Queue<Dictionary.Node<Graph.Vertex, Graph.Vertex>> pathQueue;
    private Stack<Dictionary.Node<Graph.Vertex, Graph.Vertex>> pathToShowStack;
    private ArrayList<Dictionary.Node<Graph.Vertex, Graph.Vertex>> pathShownList;
    private String from;
    private String to;

    public GraphWindow() {
        // init title bar
//        initTitleBarPanel();

        // init tables
        initInputTablePanel();
        initPathwayTablePanel();

        //init control panel
        initInputPanel();
        initTablePanel();
        initActionPanel();

        // init two main sub panels
        initControlPanel();
        initVisualPanel();

        // controls
        setActionButtonsActionListeners();

        // init main panel after creating all needed components
        initMainPanel();

        initTheme();

        // only load the mainframe after inserting all components
        initMainFrame();

    }

    /**
     * Do not add any components into the main frame directly.
     * Add the component to its corresponding panel instead.
     */
    protected void initMainFrame() {
        mainFrame = new JFrame();

        mainFrame.add(mainPanel);

        mainFrame.validate();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        mainFrame.setUndecorated(true);
        mainFrame.pack();
        mainFrame.setResizable(true);
        mainFrame.setTitle("Graph Visualizer");
        mainFrame.setSize(new Dimension(1500, 800));
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((d.width / 2) - mainFrame.getWidth() / 2, (d.height / 2) - mainFrame.getHeight() / 2);
        mainFrame.setVisible(true);
    }

    protected void initTheme() {
        setBackgrounds();
        setForegrounds();
        setButtons();
    }

    protected void setBackgrounds() {
        controlPanel.setBackground(mainColor);
        visualPanel.setBackground(secondaryColor);

        inputPanel.setBackground(mainColor);
        tablePanel.setBackground(mainColor);
        actionPanel.setBackground(mainColor);

        inputMainPanel.setBackground(mainColor);
        pathwayMainPanel.setBackground(mainColor);

        inputTablePanel.setBackground(mainColor);
        pathwayTablePanel.setBackground(mainColor);

        inputLabelPanel.setBackground(mainColor);
        pathwayLabelPanel.setBackground(mainColor);

        fromToPanel.setBackground(mainColor);
        algoLabelPanel.setBackground(mainColor);
        algoSelectionPanel.setBackground(mainColor);
        playPanel.setBackground(mainColor);
        stepPanel.setBackground(mainColor);

        inputTable.setBackground(secondaryColor);
        inputTable.getTableHeader().setBackground(accentColor);
        pathwayTable.setBackground(secondaryColor);
        pathwayTable.getTableHeader().setBackground(accentColor);

        algoSelectionBox.setBackground(secondaryColor);
    }

    protected void setForegrounds() {
        inputLabel.setForeground(mainForeground);
        pathwayLabel.setForeground(mainForeground);
        algorithmLabel.setForeground(mainForeground);

        inputTable.getTableHeader().setForeground(mainForeground);
        pathwayTable.getTableHeader().setForeground(mainForeground);

        algoSelectionBox.setForeground(mainForeground);

    }

    protected void setButtons() {
        inputFileButton.setBackground(accentColor);
        inputFileButton.setForeground(mainForeground);
        inputFileButton.setFocusPainted(false);
        inputFileButton.setBorder(new EmptyBorder(5, 0, 5, 0));

        for (JButton button : actionButtons) {
            button.setBackground(secondaryColor);
            button.setForeground(mainForeground);
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        actionButtons[0].setBackground(accentColor);
    }

    protected void initMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

//        mainPanel.add(titleBarPanel);
        mainPanel.add(controlPanel);
        mainPanel.add(visualPanel);
    }

    /**
     * Custom title bar
     * Change the layout first in the initMainPanel() method before using.
     */
    protected void initTitleBarPanel() {
        titleBarPanel = new JPanel();
        titleBarPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 3));

        JButton[] titleButtons = new JButton[]{
                new JButton("X"),
                new JButton("-"),
                new JButton("_"),
        };

        for (JButton button : titleButtons) {
            buttonsPanel.add(button);
        }

        titleBarPanel.add(buttonsPanel);
    }

    /**
     * creates the instance then sets the layout to borderlayout with preferred size of 366x768 (h,w)
     * <p>
     * if you want to add a new row of components create a panel first then insert it here
     */
    protected void initControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBorder(new EmptyBorder(35, 35, 60, 35));
        controlPanel.setPreferredSize(new Dimension(400, 800)); // makes the control panel smaller

        controlPanel.add(inputPanel, BorderLayout.NORTH);
        controlPanel.add(tablePanel, BorderLayout.CENTER);
        controlPanel.add(actionPanel, BorderLayout.SOUTH);
    }

    // TODO: INSERT VISUALIZER HERE
    protected void initVisualPanel() {
        visualPanel = new JPanel();
        visualPanel.setPreferredSize(new Dimension(1100, 800)); // makes the visual panel wider than controls
    }

    // TODO: FIX FORMATTING
    protected void initInputPanel() {
        inputPanel = new JPanel();
        inputPanel.setBorder(new EmptyBorder(0,100,0,0));
        inputPanel.setLayout(new GridLayout(1, 2));

        JLabel text = new JLabel();
        text.setHorizontalAlignment(SwingConstants.LEFT);

        // components in input panel
        inputFileButton = new JButton("Browse");
        inputFileButton.addActionListener((e) -> promptFileSelection());

        inputPanel.add(text);
        inputPanel.add(inputFileButton);
    }

    // TODO: CLEAN
    protected void initTablePanel() {
        tablePanel = new JPanel();
        tablePanel.setLayout(new GridLayout(2,1));

        inputLabelPanel = new JPanel(new GridLayout(1, 1));
        inputLabelPanel.setBorder(new EmptyBorder(0,10,0,0));
        inputLabel = new JLabel("Input Table");
        inputLabel.setHorizontalAlignment(SwingConstants.LEFT);
        inputLabelPanel.add(inputLabel);

        pathwayLabelPanel = new JPanel(new GridLayout(1, 1));
        pathwayLabelPanel.setBorder(new EmptyBorder(0,10,0,0));
        pathwayLabel = new JLabel("Pathway Table");
        pathwayLabel.setHorizontalAlignment(SwingConstants.LEFT);
        pathwayLabelPanel.add(pathwayLabel);

        inputMainPanel = new JPanel();
        pathwayMainPanel = new JPanel();

        inputMainPanel.setLayout(new BoxLayout(inputMainPanel, BoxLayout.Y_AXIS));
        pathwayMainPanel.setLayout(new BoxLayout(pathwayMainPanel, BoxLayout.Y_AXIS));

        // add label panel and table panel
        inputMainPanel.add(inputLabelPanel);
        inputMainPanel.add(inputTablePanel);

        pathwayMainPanel.add(pathwayLabelPanel);
        pathwayMainPanel.add(pathwayTablePanel);

        tablePanel.add(inputMainPanel);
        tablePanel.add(pathwayMainPanel);
    }

    protected void initializeTable(JTable table, DefaultTableModel defaultTableModel, String[] columnNames,
                                 JScrollPane scrollPane) {
        defaultTableModel.setColumnIdentifiers(columnNames);
        defaultTableModel.setRowCount(0);
        table.setEnabled(false);
        table.setCellSelectionEnabled(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(10);
        table.setOpaque(true);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scrollPane.setVisible(true);
    }

    protected void initInputTablePanel() {
        inputTablePanel = new JPanel();
        inputTablePanel.setBorder(new EmptyBorder(5,0,10,0));
        inputTablePanel.setLayout(new GridLayout(1, 1));

        DefaultTableModel inputTableModel = new DefaultTableModel();
        inputTable = new JTable(inputTableModel);
        inputTable.getTableHeader().setFont(new Font("Default", Font.PLAIN, 11));
        inputTable.getTableHeader().setBorder(new LineBorder(accentColor, 1));
        inputTableScrollPane = new JScrollPane(inputTable);
        inputTableScrollPane.setBorder(new EmptyBorder(0 ,0 ,0 ,0));

        initializeTable(inputTable, inputTableModel, new String[]{"Weight", "Point A", "Point B"}, inputTableScrollPane);

        inputTablePanel.add(inputTableScrollPane);
    }

    protected void initPathwayTablePanel() {
        pathwayTablePanel = new JPanel();
        pathwayTablePanel.setBorder(new EmptyBorder(5,0,10,0));
        pathwayTablePanel.setLayout(new GridLayout(1, 1));

        DefaultTableModel pathwayTableModel = new DefaultTableModel();
        pathwayTable = new JTable(pathwayTableModel);
        pathwayTable.getTableHeader().setFont(new Font("Default", Font.PLAIN, 11));
        pathwayTable.getTableHeader().setBorder(new LineBorder(accentColor, 1));
        pathwayTableScrollPane = new JScrollPane(pathwayTable);
        pathwayTableScrollPane.setBorder(new EmptyBorder(0 ,0 ,0 ,0));

        initializeTable(pathwayTable, pathwayTableModel, new String[]{"Weight", "Point fA", "Point fB"}, pathwayTableScrollPane);

        pathwayTablePanel.add(pathwayTableScrollPane);
    }

    protected void initActionPanel() {
        actionPanel = new JPanel();
        actionPanel.setLayout(new GridLayout(5, 1));

        fromToPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        algoLabelPanel = new JPanel(new GridLayout(1, 1));
        algoSelectionPanel = new JPanel(new GridLayout(1, 1));
        playPanel = new JPanel(new GridLayout(1, 1));
        stepPanel = new JPanel(new GridLayout(1, 4, 5, 5));

        fromField = new JTextField();
        toField = new JTextField();

        algoLabelPanel.setPreferredSize(new Dimension(100, 0));
        algoSelectionPanel.setPreferredSize(new Dimension(100, 20   ));
        playPanel.setPreferredSize(new Dimension(100, 40));
        stepPanel.setPreferredSize(new Dimension(100, 40));

        algoLabelPanel.setBorder(new EmptyBorder(10, 30, 0, 30));
        algoSelectionPanel.setBorder(new EmptyBorder(0,30,5,30));
        playPanel.setBorder(new EmptyBorder(0,30,5,30));
        stepPanel.setBorder(new EmptyBorder(0,30,5,30));

        algorithmLabel = new JLabel("Algorithm");
        algorithmLabel.setHorizontalAlignment(SwingConstants.CENTER);
        algoLabelPanel.add(algorithmLabel);

        initializeAlgoSelectionBox();
        algoSelectionPanel.add(algoSelectionBox);

        setActionButtons(); // modify this to change button style

        fromToPanel.add(fromField);
        fromToPanel.add(toField);
        fromToPanel.add(actionButtons[5]); // set
        playPanel.add(actionButtons[0]); // Play/Pause button
        stepPanel.add(actionButtons[1]); // Go to start Button
        stepPanel.add(actionButtons[2]); // Step backward Button
        stepPanel.add(actionButtons[3]); // Step forward Button
        stepPanel.add(actionButtons[4]); // Go to end Button

        // top: dropdown list, middle: wide play/pause button, bottom: steppers
        actionPanel.add(fromToPanel);
        actionPanel.add(algoLabelPanel);
        actionPanel.add(algoSelectionPanel);
        actionPanel.add(playPanel);
        actionPanel.add(stepPanel);
    }

    private void initializeAlgoSelectionBox() {
        algoSelectionBox = new JComboBox<String>(); // Dropdown list
        algoSelectionBox.setFocusable(false);
        algoSelectionBox.addItem("None");
        algoSelectionBox.addItem("Depth First Search");
        algoSelectionBox.addItem("Breadth First Search");
        algoSelectionBox.addItem("Dijkstra's Shortest Path");
        algoSelectionBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String selection = algoSelectionBox.getSelectedItem()+"";
                if (selection.equals("Depth First Search") || selection.equals("Breadth First Search") ) {
                    toField.setText("");
                    toField.setEnabled(false);
                }else {
                    toField.setEnabled(true);
                }
            }
        });
    }

    private void initializePathQueue() {
        // TODO: Validate input here
        from = fromField.getText();
        to = toField.getText();
        graphCanvas.setLabels(algoSelectionBox.getSelectedItem()+"", from, to);
        graphCanvas.repaint();
        visualizerThread = new VisualizerThread();
        switch (algoSelectionBox.getSelectedItem()+"") {
            case "None":
                pathQueue = null;
                break;
            case "Depth First Search":
                pathQueue = graph.depthFirstSearch(from);
                break;
            case "Breadth First Search":
                pathQueue = graph.breadthFirstSearch(from);
                break;
            case "Dijkstra's Shortest Path":
//                pathQueue = graph.PATH FIND
                break;
            default:
                System.out.println("Combo Box Invalid Item");
                break;
        }
    }

    private void initializePathStackToShow() {
        Stack<Dictionary.Node<Graph.Vertex, Graph.Vertex>> temp = new Stack<>();
        pathToShowStack = new Stack<>();
        while (!pathQueue.isEmpty()) {
            temp.push(pathQueue.dequeue());
        }
        while (!temp.isEmpty()) {
            pathToShowStack.push(temp.pop());
        }
    }

    protected void setActionButtons() {
        for (JButton button : actionButtons) {
//            button.setBackground(Color.RED);
        }
    }

    private synchronized void setActionButtonsActionListeners() {
        actionButtons[5].addActionListener(e -> { // setFromTo
            initializePathQueue();
            initializePathStackToShow();
            pathShownList = new ArrayList<>();
            System.out.println(pathToShowStack);
        });

        actionButtons[0].addActionListener(e -> { // play
            changeMode(paused);
            if (!paused) {
                if (visualizerThread.getState().toString().equals("TERMINATED")){
                    System.out.println("Creating new thread");
                    visualizerThread = new VisualizerThread();
                    visualizerThread.start();
                }else {
                    visualizerThread.start();
                }
            }else {
                visualizerThread.interrupt();
            }
        });

        actionButtons[1].addActionListener(e -> { // skip to start
            for (int i = pathShownList.getSize(); i > -1; i--) {
                pathToShowStack.push(pathShownList.getElement(i));
                pathShownList.remove(i);
            }
            graphCanvas.setPath(pathShownList);
        });

        actionButtons[2].addActionListener(e -> { // backward
            pathToShowStack.push(pathShownList.getElement(pathShownList.getSize()-1));
            pathShownList.remove(pathShownList.getSize()-1);
            graphCanvas.setPath(pathShownList);

        });

        actionButtons[3].addActionListener(e -> { // forward
            pathShownList.insert(pathToShowStack.pop());
            graphCanvas.setPath(pathShownList);
        });

        actionButtons[4].addActionListener(e -> { // skip to end
            while (!pathToShowStack.isEmpty()) {
                pathShownList.insert(pathToShowStack.pop());
            }
            graphCanvas.setPath(pathShownList);
        });
    }

    private void promptFileSelection() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
        int choice = fileChooser.showOpenDialog(mainFrame);
        if (choice == JFileChooser.APPROVE_OPTION) { // initialize graph
            textFile = fileChooser.getSelectedFile();
            setVisualPanelProperties();
            System.out.println("CHOSEN: " + textFile.getName());
        } else {
            System.out.println("File Selection Aborted");
        }
    }

    private void setVisualPanelProperties() {
        graph = new Graph(new File("src/main/finals/grp2/lab/data/in.csv"));
        graphCanvas = new GraphVisualizerCanvas(graph, secondaryColor);
        graphCanvas.setPreferredSize(visualPanel.getPreferredSize());
        visualPanel.add(graphCanvas);
        visualPanel.repaint();
        visualPanel.revalidate();

    }

    // TODO: play pause action
    private void changeMode(boolean mode) {
        actionButtons[0].setText(mode ? "Pause" : "Play");
        this.paused = !mode;
        if (!paused) {
            inputFileButton.setEnabled(false);
            for (int i = 1; i < actionButtons.length; i++) {
                actionButtons[i].setEnabled(false);
            }
            fromField.setEnabled(false);
            toField.setEnabled(false);
            algoSelectionBox.setEnabled(false);
        } else {
            inputFileButton.setEnabled(true);
            for (int i = 1; i < actionButtons.length; i++) {
                actionButtons[i].setEnabled(true);
            }
            algoSelectionBox.setEnabled(true);
            fromField.setEnabled(true);
            toField.setEnabled(true);
        }
    }

    private class VisualizerThread extends Thread {

        @Override
        public void run() {
            while (true) {
                pathShownList.insert(pathToShowStack.pop());
                graphCanvas.setPath(pathShownList);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    break;
                }
                if (pathToShowStack.isEmpty()) {
                    changeMode(paused);
                    break;
                }
            }
        }

    }
}