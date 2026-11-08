package velora;

import javax.swing.*;

public final class Main {
    private Main() { }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) { /* default is fine */ }
            new WalletStudio().setVisible(true);
        });
    }
}
