package org.jungrapht.samples.util;

import com.google.common.base.Strings;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.jungrapht.visualization.VisualizationModel;
import org.jungrapht.visualization.layout.algorithms.ForceAtlas2LayoutAlgorithm;
import org.jungrapht.visualization.layout.algorithms.repulsion.BarnesHutFA2Repulsion;

public class ForceAtlas2ControlPanel<V, E> extends JPanel {

  VisualizationModel<V, E> visualizationModel;
  BarnesHutFA2Repulsion.Builder<V> repulsion = BarnesHutFA2Repulsion.builder();
  ForceAtlas2LayoutAlgorithm.Builder<V, ?, ?> builder = ForceAtlas2LayoutAlgorithm.builder();

  public ForceAtlas2ControlPanel(VisualizationModel<V, E> visualizationModel) {
    super(new GridLayout(0, 3));
    setBorder(new TitledBorder("ForceAtlas2 Configuration"));
    this.visualizationModel = visualizationModel;

    JTextField repulsionText = new JTextField("100.0", 15);
    repulsionText.setBorder(new TitledBorder("Repulsion"));

    JRadioButton useLinLogButton = new JRadioButton("Use LinLog");
    this.add(useLinLogButton);

    JRadioButton attractionByWeights = new JRadioButton("Attraction By Weights");

    JRadioButton dissuadeHubs = new JRadioButton("Dissuade Hubs");

    JTextField weightsDeltaText = new JTextField("1.0", 15);
    weightsDeltaText.setBorder(new TitledBorder("Weights Delta"));

    JTextField gravityKText = new JTextField("5.0", 15);
    gravityKText.setBorder(new TitledBorder("GravityK"));

    JTextField maxIterationsText = new JTextField("1000", 15);
    maxIterationsText.setBorder(new TitledBorder("Max Iterations"));

    JTextField toleranceText = new JTextField("1.0", 15);
    toleranceText.setBorder(new TitledBorder("Tolerance"));
    ActionListener action =
        e -> {
          if (!Strings.isNullOrEmpty(repulsionText.getText())) {
            try {
              repulsion.repulsionK(Double.parseDouble(repulsionText.getText()));
            } catch (Exception ex) {
            }
          }

          if (!Strings.isNullOrEmpty(weightsDeltaText.getText())) {
            try {
              builder.delta(Double.parseDouble(weightsDeltaText.getText()));
            } catch (Exception ex) {
            }
          }
          if (!Strings.isNullOrEmpty(maxIterationsText.getText())) {
            try {
              builder.maxIterations(Integer.parseInt(maxIterationsText.getText()));
            } catch (Exception ex) {
            }
          }
          if (!Strings.isNullOrEmpty(gravityKText.getText())) {
            try {
              builder.gravityK(Double.parseDouble(gravityKText.getText()));
            } catch (Exception ex) {
            }
          }

          builder.linLog(useLinLogButton.isSelected());
          builder.attractionByWeights(attractionByWeights.isSelected());
          builder.dissuadeHubs(dissuadeHubs.isSelected());
          builder.repulsionContractBuilder(repulsion);
          visualizationModel.setLayoutAlgorithm(builder.build());
        };
    repulsionText.addActionListener(action);
    useLinLogButton.addActionListener(action);
    attractionByWeights.addActionListener(action);
    dissuadeHubs.addActionListener(action);
    weightsDeltaText.addActionListener(action);
    gravityKText.addActionListener(action);
    maxIterationsText.addActionListener(action);
    toleranceText.addActionListener(action);

    this.add(attractionByWeights);
    this.add(repulsionText);
    this.add(weightsDeltaText);
    this.add(useLinLogButton);
    this.add(gravityKText);
    this.add(toleranceText);
    this.add(dissuadeHubs);
    this.add(maxIterationsText);

    JButton resetDefaultsButton = new JButton("Reset Defaults");
    resetDefaultsButton.addActionListener(action);
    resetDefaultsButton.addActionListener(
        e -> {
          repulsionText.setText("100.0");
          useLinLogButton.setSelected(false);
          attractionByWeights.setSelected(false);
          dissuadeHubs.setSelected(false);
          weightsDeltaText.setText("1.0");
          gravityKText.setText("5.0");
          maxIterationsText.setText("1000");
          toleranceText.setText("1.0");
        });
    this.add(resetDefaultsButton);
  }
}
