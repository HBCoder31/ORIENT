// LogoUtil.java
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class LogoUtil {

    public static JLabel createLogoLabel(int width, int height) {
        try {
            ImageIcon icon = null;

            // 1. Try loading from the classpath (if the image is inside your src/ folder)
            java.net.URL imgURL = LogoUtil.class.getResource("/OrientLogo.png");
            if (imgURL != null) {
                icon = new ImageIcon(imgURL);
            }
            // 2. Try loading from a direct file path (if it is in your project root)
            else {
                File file = new File("OrientLogo.png");
                if (file.exists()) {
                    icon = new ImageIcon(file.getAbsolutePath());
                }
            }

            // 3. Explicitly check if the icon is null or broken (width <= 0)
            if (icon == null || icon.getIconWidth() <= 0) {
                throw new Exception("Image not found or failed to load");
            }

            // Image successfully found, scale and return it
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new JLabel(new ImageIcon(img));

        } catch (Exception e) {
            System.err.println("Logo load warning: " + e.getMessage() + " - Using fallback.");
            return createFallbackLogo(width, height);
        }
    }

    private static JLabel createFallbackLogo(int width, int height) {
        JLabel fallback = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw a simple logo design
                g2d.setColor(new Color(0, 70, 140)); // Matches header background
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2d.getFontMetrics();
                String text = "ORIENT";
                int textWidth = fm.stringWidth(text);

                // Center the text
                g2d.drawString(text, (getWidth() - textWidth) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        // CRITICAL: Ensure the layout manager knows how big to draw this empty label
        fallback.setPreferredSize(new Dimension(width, height));
        return fallback;
    }
}