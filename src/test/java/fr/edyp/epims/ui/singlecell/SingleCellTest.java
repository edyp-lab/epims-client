/*
 * Copyright (C) 2024
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

package fr.edyp.epims.ui.singlecell;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.edyp.epims.json.CellenOneManipJson;
import fr.edyp.epims.json.CellenOneRunJson;
import fr.edyp.epims.ui.panels.singlecell.CellenOneManipsPanel;
import fr.edyp.epims.ui.renderers.IntegerTableCellRenderer;
import fr.edyp.epims.ui.panels.model.CellenOneManipTableModel;
import fr.edyp.epims.ui.panels.model.CellenOneRunTableModel;
import org.junit.Assert;
import org.junit.Test;

import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SingleCellTest {

    @Test
    public void testJsonSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse("2026-09-24 14:30:00");

        ArrayList<String> sources1 = new ArrayList<>(List.of("1A1"));
        ArrayList<String> plates1 = new ArrayList<>(List.of("P1"));
        ArrayList<Point> plateSizes1 = new ArrayList<>(List.of(new Point(8, 12)));
        CellenOneRunJson run1 = new CellenOneRunJson("Run_R1", sources1, plates1, plateSizes1, 48);

        ArrayList<String> sources2 = new ArrayList<>(Arrays.asList("1A1", "A2"));
        ArrayList<String> plates2 = new ArrayList<>(Arrays.asList("P1", "P2"));
        ArrayList<Point> plateSizes2 = new ArrayList<>(Arrays.asList(new Point(8, 12), new Point(8, 12)));
        CellenOneRunJson run2 = new CellenOneRunJson("Run_R2", sources2, plates2, plateSizes2, 62);

        ArrayList<CellenOneRunJson> runs = new ArrayList<>();
        runs.add(run1);
        runs.add(run2);

        CellenOneManipJson manip = new CellenOneManipJson("20260225_MLE15_MANIP", date, "SingleCell sample isolation", runs);

        String json = mapper.writeValueAsString(manip);
        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("20260225_MLE15_MANIP"));
        Assert.assertTrue(json.contains("Run_R1"));
        Assert.assertTrue(json.contains("Run_R2"));

        CellenOneManipJson deserialized = mapper.readValue(json, CellenOneManipJson.class);
        Assert.assertEquals("20260225_MLE15_MANIP", deserialized.getName());
        Assert.assertEquals("SingleCell sample isolation", deserialized.getInformation());
        Assert.assertNotNull(deserialized.getRuns());
        Assert.assertEquals(2, deserialized.getRuns().size());

        CellenOneRunJson dRun1 = deserialized.getRuns().get(0);
        Assert.assertEquals("Run_R1", dRun1.getName());
        Assert.assertEquals(List.of("1A1"), dRun1.getSources());
        Assert.assertEquals(List.of("P1"), dRun1.getPlates());
        Assert.assertEquals(1, dRun1.getPlateSizes().size());
        Assert.assertEquals(8, dRun1.getPlateSizes().get(0).x);
        Assert.assertEquals(12, dRun1.getPlateSizes().get(0).y);
        Assert.assertEquals(Integer.valueOf(48), dRun1.getSamplesCount());

        CellenOneRunJson dRun2 = deserialized.getRuns().get(1);
        Assert.assertEquals("Run_R2", dRun2.getName());
        Assert.assertEquals(List.of("1A1", "A2"), dRun2.getSources());
        Assert.assertEquals(List.of("P1", "P2"), dRun2.getPlates());
        Assert.assertEquals(2, dRun2.getPlateSizes().size());
        Assert.assertEquals(8, dRun2.getPlateSizes().get(0).x);
        Assert.assertEquals(12, dRun2.getPlateSizes().get(0).y);
        Assert.assertEquals(8, dRun2.getPlateSizes().get(1).x);
        Assert.assertEquals(12, dRun2.getPlateSizes().get(1).y);
        Assert.assertEquals(Integer.valueOf(62), dRun2.getSamplesCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCellenOneRunJsonMismatchedPlatesAndPlateSizes() {
        ArrayList<String> sources = new ArrayList<>(List.of("1A1"));
        ArrayList<String> plates = new ArrayList<>(Arrays.asList("P1", "P2"));
        ArrayList<Point> plateSizes = new ArrayList<>(List.of(new Point(8, 12)));
        new CellenOneRunJson("Run_Invalid", sources, plates, plateSizes, 48);
    }

    @Test
    public void testManipTableModel() throws Exception {
        CellenOneManipTableModel model = new CellenOneManipTableModel();
        Assert.assertEquals(3, model.getColumnCount());
        Assert.assertEquals("Name", model.getColumnName(CellenOneManipTableModel.COLTYPE_NAME));
        Assert.assertEquals("Date", model.getColumnName(CellenOneManipTableModel.COLTYPE_DATE));
        Assert.assertEquals("Information", model.getColumnName(CellenOneManipTableModel.COLTYPE_INFORMATION));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse("2026-09-24 10:00:00");
        CellenOneManipJson manip = new CellenOneManipJson("ManipA", date, "Info test", new ArrayList<>());

        List<CellenOneManipJson> list = new ArrayList<>();
        list.add(manip);
        model.setValues(list);

        Assert.assertEquals(1, model.getRowCount());
        Assert.assertEquals("ManipA", model.getValueAt(0, CellenOneManipTableModel.COLTYPE_NAME));
        Assert.assertEquals("2026-09-24", model.getValueAt(0, CellenOneManipTableModel.COLTYPE_DATE));
        Assert.assertEquals("Info test", model.getValueAt(0, CellenOneManipTableModel.COLTYPE_INFORMATION));
    }

    @Test
    public void testRunTableModel() {
        CellenOneRunTableModel model = new CellenOneRunTableModel();
        Assert.assertEquals(5, model.getColumnCount());
        Assert.assertEquals("Name", model.getColumnName(CellenOneRunTableModel.COLTYPE_NAME));
        Assert.assertEquals("Sources", model.getColumnName(CellenOneRunTableModel.COLTYPE_SOURCES));
        Assert.assertEquals("Plates", model.getColumnName(CellenOneRunTableModel.COLTYPE_PLATES));
        Assert.assertEquals("Plates size", model.getColumnName(CellenOneRunTableModel.COLTYPE_PLATES_SIZE));
        Assert.assertEquals("Samples Count", model.getColumnName(CellenOneRunTableModel.COLTYPE_SAMPLES_COUNT));

        ArrayList<String> sources = new ArrayList<>(Arrays.asList("1A1", "A2"));
        ArrayList<String> plates = new ArrayList<>(Arrays.asList("P1", "P2"));
        ArrayList<Point> plateSizes = new ArrayList<>(Arrays.asList(new Point(8, 12), new Point(8, 12)));
        CellenOneRunJson run = new CellenOneRunJson("Run_Alpha", sources, plates, plateSizes, 120);

        List<CellenOneRunJson> list = new ArrayList<>();
        list.add(run);
        model.setRuns(list);

        Assert.assertEquals(1, model.getRowCount());
        Assert.assertEquals("Run_Alpha", model.getValueAt(0, CellenOneRunTableModel.COLTYPE_NAME));
        Assert.assertEquals("1A1, A2", model.getValueAt(0, CellenOneRunTableModel.COLTYPE_SOURCES));
        Assert.assertEquals("P1, P2", model.getValueAt(0, CellenOneRunTableModel.COLTYPE_PLATES));
        Assert.assertEquals("8x12, 8x12", model.getValueAt(0, CellenOneRunTableModel.COLTYPE_PLATES_SIZE));
        Assert.assertEquals(Integer.valueOf(120), model.getValueAt(0, CellenOneRunTableModel.COLTYPE_SAMPLES_COUNT));
        Assert.assertTrue(model.getRenderer(0, CellenOneRunTableModel.COLTYPE_SAMPLES_COUNT) instanceof IntegerTableCellRenderer);
    }

    @Test
    public void testSingleCellPanelInitialization() {
        CellenOneManipsPanel panel = CellenOneManipsPanel.getPanel();
        Assert.assertNotNull(panel);
        panel.reinit();
    }
}
