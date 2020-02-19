package org.jungrapht.samples.util;

import com.google.common.base.Strings;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

public class ForceAtlas2ConfigurationPanel extends JPanel {

//    public ForceAtlas2ConfigurationPanel() {
//        JTextField repulsionText = new JTextField("100", 15);
//        repulsionText.setBorder(new TitledBorder("Repulsion"));
//
//        JTextField attractionText = new JTextField("4", 15);
//        attractionText.setBorder(new TitledBorder("Max V per Layer"));
//
//        JRadioButton useLinLogButton = new JRadioButton("Use LinLog");
//
//        JRadioButton attractionByWeights = new JRadioButton("Attraction By Weights");
//
//        JRadioButton dissuadeHubs = new JRadioButton("Dissuade Hubs");
//
//        JTextField weightsDeltaText = new JTextField("4", 15);
//        weightsDeltaText.setBorder(new TitledBorder("Weights Delta"));
//
//        JTextField globalSwingText = new JTextField("4", 15);
//        globalSwingText.setBorder(new TitledBorder("Global Swing"));
//
//        JTextField globalTraceText = new JTextField("4", 15);
//        globalTraceText.setBorder(new TitledBorder("Global Trace"));
//
//        JTextField speedText = new JTextField("4", 15);
//        speedText.setBorder(new TitledBorder("Speed"));
//
//        JTextField toleranceText = new JTextField("4", 15);
//        toleranceText.setBorder(new TitledBorder("Tolerance"));
//
//        JTextField linkProbabilityText = new JTextField("0.2", 15);
//        linkProbabilityText.setBorder(new TitledBorder("Link Probabilty"));
//        JTextField randomSeedText = new JTextField("7", 15);
//        randomSeedText.setBorder(new TitledBorder("Random Seed"));
//
//        JTextField maxLevelCrossText = new JTextField("23", 15);
//        maxLevelCrossText.setBorder(new TitledBorder("Max Level Cross"));
//
//        JPanel graphPropertyPanel = new JPanel(new GridLayout(2, 2));
//        graphPropertyPanel.add(layersText);
//        graphPropertyPanel.add(maxVerticesPerLayerText);
//        graphPropertyPanel.add(linkProbabilityText);
//        graphPropertyPanel.add(randomSeedText);
//        JPanel floater = new JPanel();
//        floater.setBorder(new TitledBorder("Graph Generator"));
//        floater.add(graphPropertyPanel);
//        ActionListener action =
//                e -> {
//                    int layers = 9;
//                    if (!Strings.isNullOrEmpty(layersText.getText())) {
//                        try {
//                            layers = Integer.parseInt(layersText.getText());
//                        } catch (Exception ex) {
//                        }
//                    }
//                    int maxVerticesPerLayer = 3;
//                    if (!Strings.isNullOrEmpty(layersText.getText())) {
//                        try {
//                            maxVerticesPerLayer = Integer.parseInt(maxVerticesPerLayerText.getText());
//                        } catch (Exception ex) {
//                        }
//                    }
//                    double linkProbability = 0.2;
//                    if (!Strings.isNullOrEmpty(linkProbabilityText.getText())) {
//                        try {
//                            linkProbability = Double.parseDouble(linkProbabilityText.getText());
//                        } catch (Exception ex) {
//                        }
//                    }
//                    long randomSeed = System.currentTimeMillis();
//                    if (!Strings.isNullOrEmpty(randomSeedText.getText())) {
//                        try {
//                            randomSeed = Long.parseLong(randomSeedText.getText());
//                        } catch (Exception ex) {
//                        }
//                    }
//                    resetGraph(layers, maxVerticesPerLayer, linkProbability, randomSeed);
//                };
//        layersText.addActionListener(action);
//        maxVerticesPerLayerText.addActionListener(action);
//        linkProbabilityText.addActionListener(action);
//        randomSeedText.addActionListener(action);
//    }

}
