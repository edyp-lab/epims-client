/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

package fr.epims.ui.common;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * Management of a default color palette for the application
 * Used by Robot Plate
 *
 * @author JM235353
 *
 */
public class CyclicColorPalette {

    public static final Color[] DEFAULT_BASE_PALETTE = {
            new Color(231, 197, 31), //yellow
            new Color(231, 113, 58), //orange
            new Color(169, 35, 59), //red bordeaux
            new Color(106, 44, 95), //purple bordeaux
            new Color(104, 71, 160), //purple blue
            new Color(98, 126, 206), //blue
            new Color(82, 120, 123), //green blue
            new Color(63, 121, 58), //green
            new Color(109, 153, 5) //green grace
    };

    public static final Color[] GROUP4_PALETTE = {
            new Color(229, 115, 115), //red 1
            new Color(129, 212, 250), //blue 1
            new Color(255, 213, 79), //yellow 1
            new Color(76, 175, 80), //Greeen 1
            new Color(183, 28, 28), //red 2
            new Color(3, 155, 229), //blue 2
            new Color(255, 179, 0), //yellow 2
            new Color(27, 94, 32), //Greeen 2
            new Color(255, 23, 68), //red 3
            new Color(1, 87, 155), //blue 3
            new Color(255, 234, 0), //yellow 3
            new Color(0, 200, 83), //Greeen 3
    };

    public static final Color GRAY_BACKGROUND = new Color(239, 236, 234);
    public static final Color GRAY_BACKGROUND_DARKER = new Color(229, 226, 224);
    public static final Color GRAY_TEXT_LIGHT = new Color(142, 136, 131);
    public static final Color GRAY_TEXT_DARK = new Color(99, 95, 93);
    public static final Color GRAY_GRID = new Color(229, 226, 224);
    public static final Color GRAY_GRID_LOG = new Color(244, 240, 238);
    public static final Color BLUE_SELECTION_ZONE = new Color(0.2f, 0.2f, 1f, 0.5f);
    public static final Color GRAY_DARK = new Color(47, 43, 42);



    /**
     * get color from the default color palette if the colorIndex is too high,
     * the color returned will be of a different brightness than those in the
     * palette
     *
     * @param colorIndex
     * @return
     */
    public static Color getColor(int colorIndex) {
        return getColor(colorIndex, DEFAULT_BASE_PALETTE);
    }

    public static Color getColorBlue(int colorIndex) {
        int q = colorIndex % GROUP4_PALETTE.length;
        return GROUP4_PALETTE[q];
    }

    /**
     * get color from the specified color palette if the colorIndex is too high,
     * the color returned will be of a different brightness than those in the
     * palette
     *
     * @param colorIndex
     * @param palette
     * @return
     */
    public static Color getColor(int colorIndex, Color[] palette) {
        int paletteSize = palette.length * 3;
        colorIndex = colorIndex % paletteSize;
        if (colorIndex < palette.length) {
            return palette[colorIndex];
        }
        int q = colorIndex / palette.length;
        int sign = ((q % 2 == 0) ? +1 : -1);
        float[] hsb = Color.RGBtoHSB(palette[colorIndex - q * palette.length].getRed(),
                palette[colorIndex - q * palette.length].getGreen(),
                palette[colorIndex - q * palette.length].getBlue(), null);
        float brightness = hsb[2] + sign * 0.17f;
        brightness = Math.max(0.0f, brightness);
        brightness = Math.min(brightness, 1.0f);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], brightness));
    }

    /**
     * Return a palette created from the default palette with additional colors
     * with a modification of the brightness
     *
     * @return
     */
    public static Color[] getPalette() {
        int paletteSize = DEFAULT_BASE_PALETTE.length * 3;
        Color[] palette = new Color[paletteSize];
        for (int i = 0; i < paletteSize; i++) {
            palette[i] = getColor(i);
        }
        return palette;
    }

    /**
     * Make a color darker.
     *
     * @param color Color to make darker.
     * @param fraction Darkness fraction. betxeen 0-1
     * @return Darker color.
     */
    public static Color getDarkerColor(Color color, double fraction) {
        int red = (int) Math.round(color.getRed() * (1.0 - fraction));
        int green = (int) Math.round(color.getGreen() * (1.0 - fraction));
        int blue = (int) Math.round(color.getBlue() * (1.0 - fraction));

        if (red < 0) {
            red = 0;
        } else if (red > 255) {
            red = 255;
        }
        if (green < 0) {
            green = 0;
        } else if (green > 255) {
            green = 255;
        }
        if (blue < 0) {
            blue = 0;
        } else if (blue > 255) {
            blue = 255;
        }

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);
    }

    /**
     * get color from the default palette with the specified transparency (alpha
     * value)
     *
     * @param colorIndex
     * @param alpha
     * @return
     */
    public static Color getColor(int colorIndex, int alpha) {
        Color c = getColor(colorIndex);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /**
     * get color from the default palette with the specified transparency (alpha
     * value)
     *
     * @param color
     * @param alpha
     * @return
     */
    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * get HTML color from the defautl palette
     *
     * @param colorIndex
     * @return
     */
    public static String getHTMLColor(int colorIndex) {
        Color c = getColor(colorIndex);
        return getHTMLColor(c);
    }

    /**
     * returns HTML color corresponding to the specified color
     *
     * @param color
     * @return
     */
    public static String getHTMLColor(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String getHTMLColoredBlock(Color color) {
        return "<font color='" + getHTMLColor(color) + "'>&#x25A0;&nbsp;</font>";
    }


    public static Color getExcelColor(int index) {
        prepareExcelColors();
        index = index % 9;
        return m_excelColorList.get(index);
    }

    public static short getExcelIndexForColor(Color c) {
        prepareExcelColors();
        Short s = m_excelColorMap.get(c);
        if (s == null) {
            return 1; // WHITE
        }
        return s;
    }
    private static void prepareExcelColors() {
        if (m_excelColorList.isEmpty()) {
            Color c;

            c = new Color(119, 158, 198);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 44);
            c = new Color(198, 119, 158);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 45);
            c = new Color(158, 119, 198);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 46);
            c = new Color(198, 158, 119);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 47);

            c = new Color(255, 255, 0);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 5);
            c = new Color(198, 158, 0);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 51);
            c = new Color(0, 255, 255);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 7);

            c = new Color(158, 198, 198);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 41);
            c = new Color(198, 198, 158);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 43); // 9 colors

            /* Old Colors
            c = new Color(153, 153, 255);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 24);
            c = new Color(51, 204, 204);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 49);
            c = new Color(204, 153, 255);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 46);
            c = new Color(255, 128, 128);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 29);
            c = new Color(0, 128, 128);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 21);
            c = new Color(0, 102, 204);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 30);
            c = new Color(255, 255, 0);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 5);
            c = new Color(0, 128, 0);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 17);
            c = new Color(128, 128, 128);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 23);
            c = new Color(255, 255, 204);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 26);
            c = new Color(128, 128, 0);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 19);
            c = new Color(204, 204, 255);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 31);
            c = new Color(255, 0, 255);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 6);
            c = new Color(0, 255, 255);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 7);
            c = new Color(153, 51, 102);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 25);
            c = new Color(192, 192, 192);  m_excelColorList.add(c); m_excelColorMap.put(c, (short) 22);// 16 colors
            */
        }
    }
    public static HashMap<Color, Short> m_excelColorMap = new HashMap();
    public static ArrayList<Color> m_excelColorList = new ArrayList();
}
