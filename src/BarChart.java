import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.TreeMap;
import javax.swing.JOptionPane;


public class BarChart {

    // color palette for bars
    private static final Color[] COLORS = initColors();

    private final String title; // bar chart title
    private final String xAxisLabel; // x-axis label
    private final String dataSource; // data source
    private String caption; // caption
    private TreeMap<String, Color> colorOf; // map category to color
    private static ArrayList<Bar> bars; // list of bars
    private static int numBars; // number of bars to display
    private boolean isSetMaxValue = false;
    private int maxValue = 0;

    /**
     * Creates a new bar chart with the given title, x-axis label, and data source.
     *
     * @param title      the title
     * @param xAxisLabel the x-axis label
     * @param dataSource the source of the data
     */
    public BarChart(String title, String xAxisLabel, String dataSource) {
        if (title == null)
            throw new IllegalArgumentException("name is null");
        if (xAxisLabel == null)
            throw new IllegalArgumentException("x-axis label is null");
        if (dataSource == null)
            throw new IllegalArgumentException("data source is null");
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.dataSource = dataSource;
        colorOf = new TreeMap<String, Color>();
        reset();
    }

    public String getTitle() {
        return this.title;
    }

    // initialize the colors
    private static Color[] initColors() {

        // 20 colors from https://vega.github.io/vega/docs/schemes/
        // replaced #d62728 with #d64c4c
        String[] hex20 = {
                "#aec7e8", "#c5b0d5", "#c49c94", "#dbdb8d", "#17becf",
                "#9edae5", "#f7b6d2", "#c7c7c7", "#1f77b4", "#ff7f0e",
                "#ffbb78", "#98df8a", "#d64c4c", "#2ca02c", "#9467bd",
                "#8c564b", "#ff9896", "#e377c2", "#7f7f7f", "#bcbd22",
        };

        // use 20 colors
        Color[] colors = new Color[hex20.length];
        for (int i = 0; i < hex20.length; i++)
            colors[i] = Color.decode(hex20[i]);
        return colors;
    }

    /**
     * Sets the maximum x-value of this bar chart (instead of having it set
     * automatially).
     * This method is useful if you know that the values stay within a given range.
     *
     * @param maxValue the maximum value
     */
    public void setMaxValue(int maxValue) {
        if (maxValue <= 0)
            throw new IllegalArgumentException("maximum value must be positive");
        this.isSetMaxValue = true;
        this.maxValue = maxValue;
    }

    /**
     * Sets the caption of this bar chart.
     * The caption is drawn in the lower-right part of the window.
     *
     * @param caption the caption
     */
    public void setCaption(String caption) {
        if (caption == null)
            throw new IllegalArgumentException("caption is null");
        this.caption = caption;
    }

    /**
     * Adds a bar to the bar chart.
     * The length of a bar is proportional to its value.
     * The bars are drawn from top to bottom in the order they are added.
     * All bars from the same category are drawn with the same color.
     *
     * @param name     the name of the bar
     * @param value    the value of the bar
     * @param category the category of bar
     */
    public void add(String name, int value, String category) {
        if (name == null)
            throw new IllegalArgumentException("name is null");
        if (category == null)
            throw new IllegalArgumentException("category is null");
        if (value <= 0)
            throw new IllegalArgumentException("value must be positive");

        if (!colorOf.containsKey(category)) {
            colorOf.put(category, COLORS[colorOf.size() % COLORS.length]);
        }
        bars.add(new Bar(name, value, category));
    }

    /**
     * Removes all of the bars from this bar chart (but keep the color scheme).
     * This method is convenient when drawing an animated bar chart.
     */
    public void reset() {
        bars = new ArrayList<Bar>();
        caption = "";
    }

    // compute units (multiple of 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, ...)
    // so that between 4 and 8 axes labels
    private static int getUnits(double xmax) {
        int units = 1;
        while (Math.floor(xmax / units) >= 8) {
            // hack to identify 20, 200, 2000, ...
            if (units % 9 == 2)
                units = units * 5 / 2;
            else
                units = units * 2;
        }
        return units;
    }

    /**
     * Draws this bar chart to standard draw.
     */
    public void draw() {

        // nothing to draw
        if (bars.isEmpty())
            return;

        // set the scale of the coordinate axes
        double xmax = Double.NEGATIVE_INFINITY;
        int[] values = new int[numBars];
        for (int i = 0; i < values.length; i++) {
            values[i] = bars.get(i).getValue();
        }
        for (int value : values) {
            if (value > xmax)
                xmax = value;
        }
        if (isSetMaxValue)
            xmax = maxValue;

        StdDraw.setXscale(-0.01 * xmax, 1.2 * xmax);
        StdDraw.setYscale(-0.01 * numBars, numBars * 1.25);

        // draw title
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setFont(new Font("SansSerif", Font.BOLD, 24));
        StdDraw.text(0.6 * xmax, numBars * 1.2, title);

        // draw x-axis label
        StdDraw.setPenColor(StdDraw.GRAY);
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, 16));
        StdDraw.textLeft(0, numBars * 1.10, xAxisLabel);

        // draw axes
        int units = getUnits(xmax);
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int unit = 0; unit <= xmax; unit += units) {
            StdDraw.setPenColor(StdDraw.GRAY);
            StdDraw.text(unit, numBars * 1.02, String.format("%,d", unit));
            StdDraw.setPenColor(new Color(230, 230, 230));
            StdDraw.line(unit, 0.1, unit, numBars * 1.0);
        }

        // draw caption
        StdDraw.setPenColor(StdDraw.LIGHT_GRAY);
        if (caption.length() <= 4)
            StdDraw.setFont(new Font("SansSerif", Font.BOLD, 100));
        else if (caption.length() <= 8)
            StdDraw.setFont(new Font("SansSerif", Font.BOLD, 60));
        else
            StdDraw.setFont(new Font("SansSerif", Font.BOLD, 40));
        StdDraw.textRight(1.15 * xmax, 0.2 * numBars, caption);

        // draw data source acknowledgment
        StdDraw.setPenColor(StdDraw.LIGHT_GRAY);
        StdDraw.setFont(new Font("SansSerif", Font.PLAIN, 14));
        StdDraw.textRight(1.14 * xmax, 0.1 * numBars, dataSource);

        // draw bars
        for (int i = 0; i < numBars; i++) {
            String name = bars.get(i).getName();
            int value = values[i];
            Color color = colorOf.get(bars.get(i).getCatagory());
            StdDraw.setPenColor(color);
            StdDraw.filledRectangle(0.5 * value, numBars - i - 0.5, 0.5 * value, 0.4);
            StdDraw.setPenColor(StdDraw.BLACK);
            int fontSize = (int) Math.ceil(14 * 10.0 / numBars);
            StdDraw.setFont(new Font("SansSerif", Font.BOLD, fontSize));
            StdDraw.textRight(value - 0.01 * xmax, numBars - i - 0.5, name);
            StdDraw.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
            StdDraw.setPenColor(StdDraw.DARK_GRAY);
            StdDraw.textLeft(value + 0.01 * xmax, numBars - i - 0.5, String.format("%,d", value));
        }
    }

    // sample client
    public static void main(String[] args) {

        String[] options = { "Population of US Cities", "Population of Cities", "Population of Countries" };

        String[] optionFiles = { "cities-usa", "cities", "countries" };

        // Get user option
        String userChoice = (String) JOptionPane.showInputDialog(null, "Which Data Set?",
                "Choose Data Set", JOptionPane.QUESTION_MESSAGE,
                null, options, options[2]);

        // Convert choice into its respective file path
        String userChoicePath = "";
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(userChoice)) {
                userChoicePath = optionFiles[i];
                break;
            }
        }

        if (userChoicePath.equals("")) {
            throw new IllegalArgumentException("Cannot have an empty file path");
        }
        // initialize barchart fields
        String title;
        String xAxis;
        String source;
        BarChart chart;

        try {
            //get file 
            Scanner infile = new Scanner(new File("./data/"+userChoicePath + ".txt"));
            //read in barchart params
            title = infile.nextLine();
            xAxis = infile.nextLine();
            source = infile.nextLine();

            //get the number of bars to display from user
            infile.nextLine();
            int maxBars = Integer.parseInt(infile.nextLine());
            numBars = Integer.parseInt(JOptionPane.showInputDialog("How many bars to display? Max: " + maxBars));

            //create the chart and canvas
            chart = new BarChart(title, xAxis, source);
            StdDraw.setCanvasSize(1000, 700);
            StdDraw.enableDoubleBuffering();

            //check the number of bars is valid
            if (numBars == 0) {
                throw new IllegalArgumentException("Cannot display no bars!");
            }

            if (numBars > maxBars) {
                throw new IllegalArgumentException("Cannot display more than the maximum amount of bars!");
            }

            //while the file has more data, continue to read in the data and display it
            while (infile.hasNext()) {
                StdDraw.clear();

                processData(infile, chart, maxBars);
                infile.nextLine();

                chart.draw();
                StdDraw.show();
                chart.reset();
                StdDraw.pause(50);
            }

        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
    }

    //process the data from the file
    public static void processData(Scanner infile, BarChart chart, int maxbars) {
        for (int i = 0; i < maxbars; i++) {
            String line = infile.nextLine();
            String[] arr = line.split(",");
            chart.setCaption(arr[0]);
            //only to add country to the world cities dataset lables
            if (!chart.getTitle().equals("The most populous countries in the world from 1950 to 2100")) {
                chart.add(arr[1] + ", " + arr[2], Integer.parseInt(arr[3]), arr[4]);
            } else {
                chart.add(arr[1], Integer.parseInt(arr[3]), arr[4]);
            }
        }
        //sort the bars in decending order
        Collections.sort(bars);
        //continue if there are more lines
        if (infile.hasNext()) {
            infile.nextLine();
        }
    }
}
