package com.nebula.app.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/** Bright-star catalog (J2000) and constellation stick figures. */
public final class StarCatalog {
    private StarCatalog() {}

    /** name, RA hours, Dec degrees, magnitude */
    public static final Object[][] STARS = {
        {"Betelgeuse",5.919,7.407,0.5},{"Rigel",5.242,-8.202,0.12},{"Bellatrix",5.419,6.350,1.64},
        {"Mintaka",5.533,-0.299,2.2},{"Alnilam",5.603,-1.202,1.69},{"Alnitak",5.679,-1.943,1.77},
        {"Saiph",5.796,-9.670,2.06},{"Meissa",5.585,9.934,3.4},
        {"Sirius",6.752,-16.716,-1.46},{"Adhara",6.977,-28.972,1.5},{"Wezen",7.140,-26.393,1.84},
        {"Mirzam",6.378,-17.956,1.98},{"Procyon",7.655,5.225,0.34},{"Gomeisa",7.452,8.289,2.9},
        {"Aldebaran",4.599,16.509,0.85},{"Elnath",5.438,28.608,1.65},{"Zeta Tau",5.627,21.143,3.0},
        {"Castor",7.577,31.888,1.58},{"Pollux",7.755,28.026,1.14},{"Alhena",6.629,16.399,1.9},
        {"Capella",5.278,45.998,0.08},
        {"Regulus",10.139,11.967,1.35},{"Denebola",11.818,14.572,2.1},{"Algieba",10.333,19.842,2.0},
        {"Dubhe",11.062,61.751,1.79},{"Merak",11.031,56.382,2.37},{"Phecda",11.897,53.695,2.4},
        {"Megrez",12.257,57.033,3.3},{"Alioth",12.900,55.960,1.77},{"Mizar",13.399,54.925,2.27},
        {"Alkaid",13.792,49.313,1.86},{"Polaris",2.530,89.264,2.0},
        {"Schedar",0.675,56.537,2.2},{"Caph",0.153,59.150,2.3},{"Gamma Cas",0.945,60.717,2.5},
        {"Ruchbah",1.430,60.235,2.7},{"Segin",1.907,63.670,3.4},
        {"Vega",18.616,38.784,0.03},{"Altair",19.846,8.868,0.77},{"Deneb",20.690,45.280,1.25},
        {"Sadr",20.370,40.257,2.2},{"Albireo",19.512,27.960,3.1},{"Gienah",20.770,33.970,2.5},
        {"Arcturus",14.261,19.182,-0.05},{"Spica",13.420,-11.161,0.97},
        {"Antares",16.490,-26.432,1.06},{"Shaula",17.560,-37.104,1.6},{"Dschubba",16.005,-22.622,2.3},{"Sargas",17.622,-42.998,1.9},
        {"Acrux",12.443,-63.099,0.77},{"Gacrux",12.519,-57.113,1.6},{"Mimosa",12.795,-59.689,1.25},{"Delta Cru",12.252,-58.749,2.8},
        {"Rigil Kent",14.660,-60.835,-0.27},{"Hadar",14.064,-60.373,0.6},
        {"Canopus",6.399,-52.696,-0.74},{"Achernar",1.629,-57.237,0.46},{"Fomalhaut",22.961,-29.622,1.16},
        {"Markab",23.079,15.205,2.5},{"Scheat",23.063,28.083,2.4},{"Algenib",0.220,15.184,2.8},{"Alpheratz",0.139,29.090,2.1},
        {"Mirfak",3.405,49.861,1.8},{"Mirach",1.162,35.621,2.1},{"Alcyone",3.790,24.105,2.9}
    };

    public static final String[][] LINES = {
        {"Meissa","Betelgeuse"},{"Meissa","Bellatrix"},{"Betelgeuse","Alnitak"},{"Bellatrix","Mintaka"},
        {"Alnitak","Alnilam"},{"Alnilam","Mintaka"},{"Alnitak","Saiph"},{"Mintaka","Rigel"},
        {"Dubhe","Merak"},{"Merak","Phecda"},{"Phecda","Megrez"},{"Megrez","Dubhe"},{"Megrez","Alioth"},{"Alioth","Mizar"},{"Mizar","Alkaid"},
        {"Caph","Schedar"},{"Schedar","Gamma Cas"},{"Gamma Cas","Ruchbah"},{"Ruchbah","Segin"},
        {"Sirius","Mirzam"},{"Sirius","Wezen"},{"Wezen","Adhara"},
        {"Castor","Pollux"},{"Pollux","Alhena"},{"Regulus","Algieba"},{"Algieba","Denebola"},
        {"Deneb","Sadr"},{"Sadr","Albireo"},{"Sadr","Gienah"},
        {"Dschubba","Antares"},{"Antares","Shaula"},{"Shaula","Sargas"},
        {"Acrux","Gacrux"},{"Mimosa","Delta Cru"},
        {"Markab","Scheat"},{"Scheat","Alpheratz"},{"Alpheratz","Algenib"},{"Algenib","Markab"},
        {"Aldebaran","Elnath"},{"Aldebaran","Zeta Tau"}
    };

    /** constellation label -> anchor star */
    public static final String[][] LABELS = {
        {"ORION","Alnilam"},{"URSA MAJOR","Megrez"},{"CASSIOPEIA","Schedar"},{"CANIS MAJOR","Wezen"},
        {"GEMINI","Castor"},{"LEO","Algieba"},{"CYGNUS","Sadr"},{"SCORPIUS","Antares"},{"CRUX","Mimosa"},
        {"PEGASUS","Alpheratz"},{"TAURUS","Aldebaran"}
    };

    /** Number of LINES belonging to each constellation, in the same order as LABELS. */
    private static final int[] LINE_COUNTS = {8, 7, 4, 3, 2, 2, 3, 3, 2, 4, 2};

    /** Short story shown when a constellation is tapped in the lens, in the same order as LABELS. */
    public static final String[] LABEL_INFO = {
        "Orion the Hunter. Find the three belt stars in a row; red Betelgeuse marks his shoulder and blue-white Rigel his foot.",
        "The Great Bear. Seven bright stars form the Big Dipper, and its two end stars point the way to Polaris.",
        "The vain queen of Greek myth. Five bright stars make a W (or M) that circles Polaris all night.",
        "The Great Dog, who follows Orion across the sky. It holds Sirius, the brightest star in the night sky.",
        "The Twins, Castor and Pollux. Two bright stars sit side by side, like brothers keeping watch.",
        "The Lion. A backwards question mark of stars, the Sickle, outlines his head with Regulus at its base.",
        "The Swan, flying down the Milky Way. Its brightest stars form the Northern Cross, with Deneb at the tail.",
        "The Scorpion, with red Antares as its glowing heart. Its curved tail dips toward the southern horizon.",
        "The Southern Cross, the smallest constellation. Sailors in the south use it to find the direction of south.",
        "Pegasus, the winged horse of Greek myth. The Great Square of four stars is his body in flight.",
        "The Bull, with fiery orange Aldebaran as its eye, charging toward Orion."
    };

    public static final int N_NAMED = STARS.length;
    public static final double[] RA = new double[N_NAMED + 260];   // degrees
    public static final double[] DEC = new double[N_NAMED + 260];
    public static final float[] MAG = new float[N_NAMED + 260];
    public static final String[] NAME = new String[N_NAMED + 260];
    private static final Map<String, Integer> INDEX = new HashMap<>();
    public static final int[][] LINE_IDX = new int[LINES.length][2];
    /** For every entry of LINES: index of its constellation (position in LABELS). */
    public static final int[] CONST_OF_LINE = new int[LINES.length];
    public static final int[] LINES_IN_CONST = LINE_COUNTS;
    public static final int[][] LABEL_IDX; public static final String[] LABEL_TEXT;

    static {
        for (int i = 0; i < N_NAMED; i++) {
            NAME[i] = (String) STARS[i][0]; RA[i] = ((Double) STARS[i][1]) * 15.0;
            DEC[i] = (Double) STARS[i][2]; MAG[i] = ((Double) STARS[i][3]).floatValue();
            INDEX.put(NAME[i], i);
        }
        Random r = new Random(42);
        for (int i = N_NAMED; i < RA.length; i++) {
            RA[i] = r.nextDouble() * 360.0; DEC[i] = Math.toDegrees(Math.asin(r.nextDouble() * 2 - 1));
            MAG[i] = 3.4f + r.nextFloat() * 1.8f; NAME[i] = null;
        }
        for (int i = 0; i < LINES.length; i++) { LINE_IDX[i][0] = INDEX.get(LINES[i][0]); LINE_IDX[i][1] = INDEX.get(LINES[i][1]); }
        int li = 0;
        for (int cIdx = 0; cIdx < LINE_COUNTS.length; cIdx++)
            for (int k = 0; k < LINE_COUNTS[cIdx]; k++) CONST_OF_LINE[li++] = cIdx;
        LABEL_IDX = new int[LABELS.length][1]; LABEL_TEXT = new String[LABELS.length];
        for (int i = 0; i < LABELS.length; i++) { LABEL_TEXT[i] = LABELS[i][0]; LABEL_IDX[i][0] = INDEX.get(LABELS[i][1]); }
    }

    /** Fills altAz[i] = {alt, az} for every star. */
    public static void project(double lat, double lon, long ms, double[][] out) {
        double jd = AstroUtil.julianDay(ms), lst = AstroUtil.lstDeg(jd, lon);
        for (int i = 0; i < RA.length; i++) out[i] = AstroUtil.raDecToAltAz(RA[i], DEC[i], lat, lst);
    }
}
