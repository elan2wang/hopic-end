package elan.nlp.models;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Visual {

	static String[] shades = {"     ", ".    ", ":    ", ":.   ", "::   ",
        "::.  ", ":::  ", ":::. ", ":::: ", "::::.", ":::::"};

    static NumberFormat lnf = new DecimalFormat("00E0");

    /**
     * create a string representation whose gray value appears as an indicator
     * of magnitude, cf. Hinton diagrams in statistics.
     * 
     * @param d
     *            value
     * @param max
     *            maximum value
     * @return
     */
    public static String shadeDouble(double d, double max) {
        int a = (int) Math.floor(d * 10 / max + 0.5);
        if (a > 10 || a < 0) {
            String x = lnf.format(d);
            a = 5 - x.length();
            for (int i = 0; i < a; i++) {
                x += " ";
            }
            return "<" + x + ">";
        }
        return "[" + shades[a] + "]";
    }

    public static void showDocTopic(double[][] phi) {
    	System.out.println("\nDocument--Topic Associations");
    	System.out.print("d\\t\t");
        for (int i = 0; i < phi[0].length; i++) {
            System.out.print("   " + i % 10 + "    ");
        }
        System.out.println();
        for (int i = 0; i < phi.length; i++) {
            System.out.print(i + "\t");
            for (int j = 0; j < phi[i].length; j++) {
                //System.out.print(phi[i][j] + " ");
                System.out.print(shadeDouble(phi[i][j], 1) + " ");
            }
            System.out.println();
        }
    }
}
