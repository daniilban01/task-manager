package GUI;

import BusinessLogic.SelectionPolicy;
import Model.Server;
import Model.Task;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulationFrame extends JFrame {
    private JPanel waitingPanel;
    private JPanel queuesPanel;
    private JLabel timeLabel;
    private JLabel statsLabel;
    private JButton startButton;
    private JTextField clientsField, queuesField, maxArrivalField, minArrivalField, maxServiceField, minServiceField, timeLimitField;
    private JComboBox<SelectionPolicy> strategyComboBox;

    public JButton getStartButton() { return startButton; }
    public JTextField getClientsField() { return clientsField; }
    public JTextField getQueuesField() { return queuesField; }
    public JTextField getMinArrivalField() { return minArrivalField; }
    public JTextField getMaxArrivalField() { return maxArrivalField; }
    public JTextField getMinServiceField() { return minServiceField; }
    public JTextField getMaxServiceField() { return maxServiceField; }
    public JTextField getTimeLimitField() { return timeLimitField; }
    public JComboBox<SelectionPolicy> getStrategyComboBox() { return strategyComboBox; }


    public SimulationFrame() {
        setTitle("Queues Simulator - Real-Time View");
        setSize(1000, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(createSettingsPanel());
        topContainer.add(createNorthPanel());
        add(topContainer, BorderLayout.NORTH);

        queuesPanel = new JPanel();
        queuesPanel.setLayout(new BoxLayout(queuesPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(queuesPanel), BorderLayout.CENTER);

        add(createSouthPanel(), BorderLayout.SOUTH);
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Simulation Settings"));

        clientsField = new JTextField("12", 5);
        queuesField = new JTextField("3", 5);
        minArrivalField = new JTextField("2", 5);
        maxArrivalField = new JTextField("30", 5);
        minServiceField = new JTextField("2", 5);
        maxServiceField = new JTextField("4", 5);
        timeLimitField = new JTextField("60", 5);

        strategyComboBox = new JComboBox<>(SelectionPolicy.values());

        panel.add(createSettingGroup("Clients", clientsField));
        panel.add(createSettingGroup("Queues", queuesField));
        panel.add(createSettingGroup("Min Arrival", minArrivalField));
        panel.add(createSettingGroup("Max Arrival", maxArrivalField));
        panel.add(createSettingGroup("Min Service", minServiceField));
        panel.add(createSettingGroup("Max Service", maxServiceField));
        panel.add(createSettingGroup("Time Limit", timeLimitField));

        panel.add(createStrategyGroup("Strategy", strategyComboBox));

        return panel;
    }

    private JPanel createStrategyGroup(String labelText, JComboBox<?> comboBox) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        group.add(label);
        group.add(Box.createVerticalStrut(5));
        group.add(comboBox);

        return group;
    }

    private JPanel createNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        timeLabel = new JLabel("Time: 0", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));

        waitingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        waitingPanel.setBorder(BorderFactory.createTitledBorder("Waiting Area"));

        panel.add(timeLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(waitingPanel), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSouthPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        statsLabel = new JLabel("<html><center><b style='font-size:12px;'>STATISTICS</b><br>" +
                "Average waiting time: 0.00<br>Average service time: 0.00<br>" +
                "Peak hour: 0 with 0 clients</center></html>", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));

        startButton = new JButton("START SIMULATION");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(statsLabel);
        panel.add(startButton);

        return panel;
    }

    private JPanel createSettingGroup(String labelText, JTextField textField) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);

        group.add(label);
        group.add(Box.createVerticalStrut(5));
        group.add(textField);

        return group;
    }

    private JPanel createClientBox(Task t, Color color) {
        JPanel box = new JPanel(new GridLayout(3, 1));
        box.setPreferredSize(new Dimension(55, 45));
        box.setBackground(color);
        box.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel l1 = new JLabel("ID:" + t.getID(), SwingConstants.CENTER);
        JLabel l2 = new JLabel("A:" + t.getArrivalTime(), SwingConstants.CENTER);
        JLabel l3 = new JLabel("S:" + t.getServiceTime(), SwingConstants.CENTER);

        for (JLabel l : new JLabel[]{l1, l2, l3}) {
            l.setForeground(Color.WHITE);
            l.setFont(new Font("Arial", Font.BOLD, 9));
            box.add(l);
        }
        return box;
    }

    public void update(int time, List<Task> waiting, List<Server> servers) {
        timeLabel.setText("Time: " + time);

        waitingPanel.removeAll();
        for (Task t : waiting) {
            waitingPanel.add(createClientBox(t, new Color(76, 175, 80)));
        }

        queuesPanel.removeAll();
        for (int i = 0; i < servers.size(); i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            row.setBorder(BorderFactory.createTitledBorder("Queue " + (i + 1)));
            for (Task t : servers.get(i).getTasks()) {
                row.add(createClientBox(t, new Color(33, 150, 243)));
            }
            queuesPanel.add(row);
        }
        revalidate();
        repaint();
    }

    public void setFinalStats(String stats) {
        statsLabel.setText("<html><center><b style='font-size:12px;'>STATISTICS</b><br>"
                + stats.replace("\n", "<br>") + "</center></html>");
    }


}