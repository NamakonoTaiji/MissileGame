import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelManager {
    private JPanel panel;
    private List<JLabel> labels;
    private JTextArea logTextArea;
    private JScrollPane scrollPane;

    public LabelManager(JPanel panel) {
        this.panel = panel;
        this.labels = new ArrayList<>();
        panel.setLayout(null); // パネルのレイアウトを無効にする

        // ログ用テキストエリアの作成
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);

        // 透明度を設定するために背景色にアルファ値を設定
        logTextArea.setBackground(new Color(255, 255, 255, 128)); // 白色の半透明
        logTextArea.setOpaque(false); // 不透明にしない

        // フォントサイズを設定
        logTextArea.setFont(new Font("Serif", Font.PLAIN, 15)); // ここでフォントサイズを設定

        scrollPane = new JScrollPane(logTextArea);
        scrollPane.setBounds(panel.getWidth() - 610, panel.getHeight() - 110, 600, 100); // サイズと位置を設定

        // スクロールペインの背景を透明に設定
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        panel.add(scrollPane);

        // パネルのリサイズリスナーを追加
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resizeComponents();
            }
        });
    }

    public void addLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel();
        label.setText(text);
        label.setFont(new Font("Serif", Font.PLAIN, 22));
        label.setForeground(Color.BLACK);
        label.setBounds(x, y, width, height);
        labels.add(label);
        panel.add(label);
        panel.revalidate();
        panel.repaint();
    }

    public void updateLabel(int index, String text) {
        if (index >= 0 && index < labels.size()) {
            labels.get(index).setText(text);
            panel.repaint();
        }
    }

    public void removeLabel(int index) {
        if (index >= 0 && index < labels.size()) {
            panel.remove(labels.get(index));
            labels.remove(index);
            panel.revalidate();
            panel.repaint();
        }
    }

    public void addLogMessage(String message) {
        logTextArea.append(message + "\n");
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength()); // 自動スクロール
    }

    private void resizeComponents() {
        scrollPane.setBounds(panel.getWidth() - 610, panel.getHeight() - 110, 600, 100);
        panel.revalidate();
        panel.repaint();
    }
}
