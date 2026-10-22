import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class SmartAttendanceUI extends JFrame {
    private AttendanceManager attendanceManager;
    private final String TEACHER_PASSWORD = "admin123";

    private final Color PRIMARY_COLOR = new Color(15, 23, 42);
    private final Color ACCENT_COLOR = new Color(14, 116, 144);
    private final Color BUTTON_COLOR = new Color(29, 78, 216);
    private final Color SUCCESS_COLOR = new Color(22, 163, 74);
    private final Color DANGER_COLOR = new Color(220, 38, 38);
    private final Color BG_LIGHT = new Color(248, 250, 252);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_DARK = new Color(15, 23, 42);
    private final Color TEXT_MUTED = new Color(71, 85, 105);
    private final Color BORDER_COLOR = new Color(226, 232, 240);

    private Timer autoSyncTimer;
    private JLabel dashboardStudentCountLabel;
    private JLabel totalStudentsValLabel;
    private JLabel eligibleValLabel;
    private JLabel defaultersValLabel;
    private JLabel avgPctValLabel;

    public SmartAttendanceUI() {
        this.attendanceManager = new AttendanceManager();

        setTitle("Smart Attendance Management System - NIET Greater Noida");
        setMinimumSize(new Dimension(850, 600));
        setSize(1000, 700);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initAutoSyncEngine();
        showMainMenu();
    }

    private void initAutoSyncEngine() {
        autoSyncTimer = new Timer(2500, e -> {
            boolean reloaded = attendanceManager.checkAndAutoSync();
            if (reloaded) {
                updateDashboardLabels();
            }
        });
        autoSyncTimer.start();
    }

    private void updateDashboardLabels() {
        int total = attendanceManager.getStudents().size();
        int eligible = attendanceManager.getEligibleStudents().size();
        int defaulters = attendanceManager.getDefaulters().size();
        double avg = attendanceManager.getAverageAttendancePercentage();

        if (dashboardStudentCountLabel != null) {
            dashboardStudentCountLabel.setText("Total Registered Students: " + total);
        }
        if (totalStudentsValLabel != null) {
            totalStudentsValLabel.setText(String.valueOf(total));
        }
        if (eligibleValLabel != null) {
            eligibleValLabel.setText(String.valueOf(eligible));
        }
        if (defaultersValLabel != null) {
            defaultersValLabel.setText(String.valueOf(defaulters));
        }
        if (avgPctValLabel != null) {
            avgPctValLabel.setText(String.format("%.1f%%", avg));
        }
    }

    private static class ModernButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color pressedBg;

        public ModernButton(String text, Color bg) {
            super(text);
            this.normalBg = bg;
            this.hoverBg = createHoverColor(bg);
            this.pressedBg = bg.darker();
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));
        }

        private static Color createHoverColor(Color c) {
            int r = Math.min(255, c.getRed() + 25);
            int g = Math.min(255, c.getGreen() + 25);
            int b = Math.min(255, c.getBlue() + 25);
            return new Color(r, g, b);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color currentBg = normalBg;
            if (getModel().isPressed()) {
                currentBg = pressedBg;
            } else if (getModel().isRollover()) {
                currentBg = hoverBg;
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            g2.setColor(normalBg.darker());
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }

    private JButton createStyledButton(String text, Color bg) {
        return new ModernButton(text, bg);
    }

    private JTextField createStyledTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setForeground(TEXT_DARK);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(TEXT_DARK);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return tf;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setBackground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        table.getTableHeader().setPreferredSize(new Dimension(table.getTableHeader().getWidth(), 38));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setOpaque(true);
                lbl.setBackground(PRIMARY_COLOR);
                lbl.setForeground(Color.WHITE);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 1, new Color(51, 65, 85)),
                        BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
                return lbl;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    c.setForeground(TEXT_DARK);
                } else {
                    c.setBackground(new Color(219, 234, 254));
                    c.setForeground(TEXT_DARK);
                }
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                }
                return c;
            }
        });
    }

    private void showMainMenu() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(26, 20, 26, 20));

        JLabel collegeLabel = new JLabel("NOIDA INSTITUTE OF ENGINEERING AND TECHNOLOGY", SwingConstants.CENTER);
        collegeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        collegeLabel.setForeground(new Color(186, 230, 253));

        JLabel titleLabel = new JLabel("SMART ATTENDANCE MANAGEMENT SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        headerPanel.add(collegeLabel);
        headerPanel.add(titleLabel);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(BG_LIGHT);

        JPanel buttonCard = new JPanel(new GridLayout(3, 1, 16, 16));
        buttonCard.setBackground(BG_LIGHT);
        buttonCard.setPreferredSize(new Dimension(480, 220));

        JButton teacherBtn = createStyledButton("Faculty / Teacher Login", BUTTON_COLOR);
        JButton studentBtn = createStyledButton("Student Portal", ACCENT_COLOR);
        JButton exitBtn = createStyledButton("Exit System", DANGER_COLOR);

        teacherBtn.addActionListener(e -> handleTeacherLogin());
        studentBtn.addActionListener(e -> showStudentLogin());
        exitBtn.addActionListener(e -> {
            if (autoSyncTimer != null) autoSyncTimer.stop();
            System.exit(0);
        });

        buttonCard.add(teacherBtn);
        buttonCard.add(studentBtn);
        buttonCard.add(exitBtn);

        centerWrapper.add(buttonCard, new GridBagConstraints());

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    private void handleTeacherLogin() {
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        JLabel prompt = new JLabel("Enter Faculty / Admin Security Password:");
        prompt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        prompt.setForeground(TEXT_DARK);
        panel.add(prompt);
        panel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Faculty Authentication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String enteredPassword = new String(passwordField.getPassword());
            if (ValidationHelper.checkPassword(enteredPassword, TEACHER_PASSWORD)) {
                showTeacherDashboard();
            } else {
                JOptionPane.showMessageDialog(this, "Authentication failed! Incorrect security password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createKpiCard(String title, String val, String subtitle, Color accent) {
        JPanel card = new JPanel(new BorderLayout(4, 6));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        new EmptyBorder(14, 18, 14, 18)
                )
        ));

        JLabel titleLbl = new JLabel(title.toUpperCase());
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        JLabel valLbl = new JLabel(val);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(accent.darker());

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);

        if (title.contains("ENROLLED")) totalStudentsValLabel = valLbl;
        else if (title.contains("ELIGIBLE")) eligibleValLabel = valLbl;
        else if (title.contains("DEFAULTER")) defaultersValLabel = valLbl;
        else if (title.contains("AVERAGE")) avgPctValLabel = valLbl;

        return card;
    }

    private JPanel createActionCard(String tag, String title, String desc, JButton btn, Color theme) {
        JPanel card = new JPanel(new BorderLayout(10, 12));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel tagLbl = new JLabel(tag);
        tagLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tagLbl.setForeground(theme);
        tagLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(TEXT_DARK);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><body style='width: 320px; color: #475569;'>" + desc + "</body></html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(tagLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(descLbl);

        card.add(textPanel, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(btn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private void showTeacherDashboard() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BG_LIGHT);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(PRIMARY_COLOR);
        topHeader.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 3, 3));
        titleGrp.setOpaque(false);
        JLabel nietLbl = new JLabel("NOIDA INSTITUTE OF ENGINEERING AND TECHNOLOGY (NIET)");
        nietLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nietLbl.setForeground(new Color(186, 230, 253));

        JLabel titleLbl = new JLabel("Faculty & Administration Attendance Portal");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);

        titleGrp.add(nietLbl);
        titleGrp.add(titleLbl);

        JPanel rightGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightGrp.setOpaque(false);
        JLabel dateBadge = new JLabel("Session 2026-27 | " + new SimpleDateFormat("EEE, dd MMM yyyy").format(new Date()));
        dateBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateBadge.setForeground(new Color(224, 242, 254));

        JButton firebaseBadgeBtn = createStyledButton(
                attendanceManager.isFirebaseConfigured() ? "🔥 Firebase: Active" : "🔥 Connect Firebase",
                attendanceManager.isFirebaseConfigured() ? SUCCESS_COLOR : new Color(217, 119, 6)
        );
        firebaseBadgeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        firebaseBadgeBtn.addActionListener(e -> showFirebaseConfigDialog());

        JButton logoutBtn = createStyledButton("Logout", new Color(71, 85, 105));
        logoutBtn.addActionListener(e -> showMainMenu());

        rightGrp.add(firebaseBadgeBtn);
        rightGrp.add(dateBadge);
        rightGrp.add(logoutBtn);

        topHeader.add(titleGrp, BorderLayout.WEST);
        topHeader.add(rightGrp, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(BG_LIGHT);
        center.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel kpiStrip = new JPanel(new GridLayout(1, 4, 14, 0));
        kpiStrip.setOpaque(false);
        kpiStrip.add(createKpiCard("Total Enrolled", String.valueOf(attendanceManager.getStudents().size()), "Registered Students", BUTTON_COLOR));
        kpiStrip.add(createKpiCard("Exam Eligible (>=75%)", String.valueOf(attendanceManager.getEligibleStudents().size()), "In Safe Zone", SUCCESS_COLOR));
        kpiStrip.add(createKpiCard("Critical Defaulters (<75%)", String.valueOf(attendanceManager.getDefaulters().size()), "Short Attendance Warning", DANGER_COLOR));
        kpiStrip.add(createKpiCard("Batch Average", String.format("%.1f%%", attendanceManager.getAverageAttendancePercentage()), "Class Lecture Average", ACCENT_COLOR));

        center.add(kpiStrip, BorderLayout.NORTH);

        JPanel actionGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        actionGrid.setOpaque(false);

        JButton markAttendanceBtn = createStyledButton("Open Attendance Sheet ->", BUTTON_COLOR);
        JButton manageStudentsBtn = createStyledButton("Manage Roster ->", ACCENT_COLOR);
        JButton viewReportsBtn = createStyledButton("Open Reports & Export ->", new Color(30, 41, 59));
        JButton viewDefaultersBtn = createStyledButton("View Defaulters List ->", DANGER_COLOR);

        markAttendanceBtn.addActionListener(e -> showBatchAttendanceDialog());
        manageStudentsBtn.addActionListener(e -> showManageStudentsDialog());
        viewReportsBtn.addActionListener(e -> showReportsDialog(false));
        viewDefaultersBtn.addActionListener(e -> showReportsDialog(true));

        actionGrid.add(createActionCard("BATCH ATTENDANCE", "1. Mark Daily Attendance",
                "Record daily lecture attendance. Fast batch checklist with Select All, Deselect, Invert and instant save to disk.",
                markAttendanceBtn, BUTTON_COLOR));

        actionGrid.add(createActionCard("STUDENT RECORDS", "2. Manage Students Roster",
                "Register new admissions, edit student names and lecture counts, or permanently remove records with duplicate prevention.",
                manageStudentsBtn, ACCENT_COLOR));

        actionGrid.add(createActionCard("ANALYTICS & EXPORT", "3. View Full Reports & Print",
                "Search, filter by eligibility status, sort records, print official NIET examination slips, or export full roster to Excel (.csv).",
                viewReportsBtn, new Color(30, 41, 59)));

        actionGrid.add(createActionCard("CRITICAL ALERTS", "4. View Defaulters List (< 75%)",
                "Identify at-risk students, view exact number of consecutive classes required to reach 75%, and generate official notices.",
                viewDefaultersBtn, DANGER_COLOR));

        center.add(actionGrid, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CARD_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(10, 24, 10, 24)
        ));
        JLabel statusText = new JLabel(attendanceManager.getStorageStatus() + " | Auto-Sync Active (Every 2.5s)");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusText.setForeground(TEXT_MUTED);
        footer.add(statusText, BorderLayout.WEST);

        JButton syncSettingsBtn = createStyledButton("⚙ Firebase Cloud DB Settings", new Color(14, 116, 144));
        syncSettingsBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        syncSettingsBtn.setPreferredSize(new Dimension(210, 32));
        syncSettingsBtn.addActionListener(e -> showFirebaseConfigDialog());
        footer.add(syncSettingsBtn, BorderLayout.EAST);

        dashboardPanel.add(topHeader, BorderLayout.NORTH);
        dashboardPanel.add(center, BorderLayout.CENTER);
        dashboardPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(dashboardPanel);
        revalidate();
        repaint();
    }

    private void showFirebaseConfigDialog() {
        FirebaseConfig config = FirebaseConfig.getInstance();

        JDialog dialog = new JDialog(this, "Firebase Cloud Database Settings", true);
        dialog.setSize(920, 700);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PRIMARY_COLOR);
        topBar.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("Firebase Cloud Database Configuration");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("REST API Cloud Database Module • Zero External Dependencies • Paste your 6 Firebase Console credentials below");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(186, 230, 253));

        JPanel headerText = new JPanel(new GridLayout(2, 1, 4, 4));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(sub);
        topBar.add(headerText, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BG_LIGHT);
        centerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel statusCard = new JPanel(new BorderLayout(8, 8));
        statusCard.setBackground(CARD_BG);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel statusTitle = new JLabel("CURRENT DATABASE STATUS:");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusTitle.setForeground(TEXT_MUTED);

        JLabel statusDetail = new JLabel(config.isConfigured() ?
                "● Firebase Cloud DB Active [Project: " + config.getProjectId() + "]" :
                "○ Local CSV Fallback Mode (Firebase Not Configured)");
        statusDetail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusDetail.setForeground(config.isConfigured() ? SUCCESS_COLOR : new Color(217, 119, 6));

        statusCard.add(statusTitle, BorderLayout.NORTH);
        statusCard.add(statusDetail, BorderLayout.CENTER);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(CARD_BG);
        formGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(14, 18, 14, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(6, 6, 6, 6);

        JTextField tfProjectId = createStyledTextField(24);
        JTextField tfApiKey = createStyledTextField(24);
        JTextField tfAuthDomain = createStyledTextField(24);
        JTextField tfStorageBucket = createStyledTextField(24);
        JTextField tfSenderId = createStyledTextField(24);
        JTextField tfAppId = createStyledTextField(24);
        JTextField tfDbUrl = createStyledTextField(24);
        JTextField tfAuthToken = createStyledTextField(24);

        tfProjectId.setText(config.getProjectId().equals("YOUR_PROJECT_ID") ? "" : config.getProjectId());
        tfApiKey.setText(config.getApiKey().equals("YOUR_API_KEY") ? "" : config.getApiKey());
        tfAuthDomain.setText(config.getAuthDomain().contains("YOUR_PROJECT_ID") ? "" : config.getAuthDomain());
        tfStorageBucket.setText(config.getStorageBucket().contains("YOUR_PROJECT_ID") ? "" : config.getStorageBucket());
        tfSenderId.setText(config.getMessagingSenderId().equals("YOUR_MESSAGING_SENDER_ID") ? "" : config.getMessagingSenderId());
        tfAppId.setText(config.getAppId().equals("YOUR_APP_ID") ? "" : config.getAppId());
        tfDbUrl.setText(config.getDatabaseUrl().contains("YOUR_PROJECT_ID") ? "" : config.getDatabaseUrl());
        tfAuthToken.setText(config.getAuthToken());

        tfProjectId.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String pid = tfProjectId.getText().trim();
                if (!pid.isEmpty()) {
                    if (tfDbUrl.getText().trim().isEmpty() || tfDbUrl.getText().contains("-default-rtdb")) {
                        tfDbUrl.setText("https://" + pid + "-default-rtdb.firebaseio.com");
                    }
                    if (tfAuthDomain.getText().trim().isEmpty() || tfAuthDomain.getText().contains(".firebaseapp.com")) {
                        tfAuthDomain.setText(pid + ".firebaseapp.com");
                    }
                    if (tfStorageBucket.getText().trim().isEmpty() || tfStorageBucket.getText().contains(".appspot.com")) {
                        tfStorageBucket.setText(pid + ".appspot.com");
                    }
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        String[][] fields = {
                {"1. Project ID (*):", "Firebase project identifier (e.g. smart-attendance-niet)"},
                {"2. Web API Key (*):", "Firebase Web API key from Project Settings"},
                {"3. Auth Domain:", "Authentication domain (e.g. project.firebaseapp.com)"},
                {"4. Storage Bucket:", "Storage bucket name (e.g. project.appspot.com)"},
                {"5. Messaging Sender ID:", "Cloud messaging sender ID (numeric)"},
                {"6. App ID:", "Firebase Web App ID (e.g. 1:123456:web:...)"},
                {"Realtime Database URL:", "Endpoint (https://<project>-default-rtdb.firebaseio.com)"},
                {"Database Secret / Auth (Optional):", "Leave empty for public test rules (.read: true, .write: true)"}
        };

        JTextField[] textFields = {tfProjectId, tfApiKey, tfAuthDomain, tfStorageBucket, tfSenderId, tfAppId, tfDbUrl, tfAuthToken};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.35;
            JLabel lbl = new JLabel(fields[i][0]);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(TEXT_DARK);
            formGrid.add(lbl, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.65;
            formGrid.add(textFields[i], gbc);
        }

        JScrollPane formScroll = new JScrollPane(formGrid);
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.getVerticalScrollBar().setUnitIncrement(14);

        centerPanel.add(statusCard);
        centerPanel.add(Box.createVerticalStrut(12));
        centerPanel.add(formScroll);

        JPanel bottomBar = new JPanel(new BorderLayout(10, 10));
        bottomBar.setBackground(CARD_BG);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);

        JButton testBtn = createStyledButton("Test Connection", ACCENT_COLOR);
        JButton pushBtn = createStyledButton("Push CSV to Cloud", BUTTON_COLOR);
        JButton pullBtn = createStyledButton("Pull Cloud to CSV", new Color(15, 118, 110));

        leftActions.add(testBtn);
        leftActions.add(pushBtn);
        leftActions.add(pullBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        JButton saveBtn = createStyledButton("Save Configuration", SUCCESS_COLOR);
        JButton resetBtn = createStyledButton("Clear / Revert", DANGER_COLOR);
        JButton closeBtn = createStyledButton("Close", new Color(71, 85, 105));

        rightActions.add(saveBtn);
        rightActions.add(resetBtn);
        rightActions.add(closeBtn);

        bottomBar.add(leftActions, BorderLayout.WEST);
        bottomBar.add(rightActions, BorderLayout.EAST);

        testBtn.addActionListener(e -> {
            testBtn.setEnabled(false);
            testBtn.setText("Testing...");
            new Thread(() -> {
                FirebaseConfig temp = new FirebaseConfig();
                temp.setProjectId(tfProjectId.getText().trim());
                temp.setApiKey(tfApiKey.getText().trim());
                temp.setAuthDomain(tfAuthDomain.getText().trim());
                temp.setStorageBucket(tfStorageBucket.getText().trim());
                temp.setMessagingSenderId(tfSenderId.getText().trim());
                temp.setAppId(tfAppId.getText().trim());
                temp.setDatabaseUrl(tfDbUrl.getText().trim());
                temp.setAuthToken(tfAuthToken.getText().trim());

                boolean reachable = FirebaseManager.testConnection(temp);
                SwingUtilities.invokeLater(() -> {
                    testBtn.setEnabled(true);
                    testBtn.setText("🔍 Test Connection");
                    if (reachable) {
                        JOptionPane.showMessageDialog(dialog,
                                "Firebase Connection Successful!\nEndpoint reached: " + temp.getDatabaseUrl(),
                                "Connection Succeeded", JOptionPane.INFORMATION_MESSAGE);
                        statusDetail.setText("● Firebase Reachable [Project: " + temp.getProjectId() + "]");
                        statusDetail.setForeground(SUCCESS_COLOR);
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "Unable to reach Firebase Realtime Database!\nPlease verify:\n1. Project ID is correct\n2. Realtime Database is created in Firebase Console\n3. Database rules allow access:\n   {\".read\": true, \".write\": true}",
                                "Connection Failed", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).start();
        });

        saveBtn.addActionListener(e -> {
            config.setProjectId(tfProjectId.getText().trim());
            config.setApiKey(tfApiKey.getText().trim());
            config.setAuthDomain(tfAuthDomain.getText().trim());
            config.setStorageBucket(tfStorageBucket.getText().trim());
            config.setMessagingSenderId(tfSenderId.getText().trim());
            config.setAppId(tfAppId.getText().trim());
            config.setDatabaseUrl(tfDbUrl.getText().trim());
            config.setAuthToken(tfAuthToken.getText().trim());

            boolean saved = config.saveToFile();
            if (saved) {
                JOptionPane.showMessageDialog(dialog,
                        "Configuration saved to firebase-config.json!\nMode: " +
                                (config.isConfigured() ? "Firebase Cloud DB Active" : "Local CSV Fallback"),
                        "Configuration Saved", JOptionPane.INFORMATION_MESSAGE);
                statusDetail.setText(config.isConfigured() ?
                        "● Firebase Cloud DB Active [Project: " + config.getProjectId() + "]" :
                        "○ Local CSV Fallback Mode (Firebase Not Configured)");
                statusDetail.setForeground(config.isConfigured() ? SUCCESS_COLOR : new Color(217, 119, 6));
                updateDashboardLabels();
                showTeacherDashboard();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save configuration to disk!", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pushBtn.addActionListener(e -> {
            if (!config.isConfigured()) {
                JOptionPane.showMessageDialog(dialog, "Please save your Firebase Project ID first!", "Not Configured", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int count = attendanceManager.getStudents().size();
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Upload " + count + " student record(s) from local storage to Firebase Cloud Database?",
                    "Confirm Cloud Upload", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                pushBtn.setEnabled(false);
                pushBtn.setText("Uploading...");
                new Thread(() -> {
                    boolean ok = attendanceManager.syncToFirebase();
                    SwingUtilities.invokeLater(() -> {
                        pushBtn.setEnabled(true);
                        pushBtn.setText("⬆ Push CSV -> Cloud");
                        if (ok) {
                            JOptionPane.showMessageDialog(dialog, "Successfully uploaded " + count + " records to Firebase!", "Upload Complete", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Failed to upload to Firebase. Please check connection and database rules.", "Upload Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }).start();
            }
        });

        pullBtn.addActionListener(e -> {
            if (!config.isConfigured()) {
                JOptionPane.showMessageDialog(dialog, "Please save your Firebase Project ID first!", "Not Configured", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Download student records from Firebase and update local system?",
                    "Confirm Cloud Sync", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                pullBtn.setEnabled(false);
                pullBtn.setText("Downloading...");
                new Thread(() -> {
                    boolean ok = attendanceManager.syncFromFirebase();
                    SwingUtilities.invokeLater(() -> {
                        pullBtn.setEnabled(true);
                        pullBtn.setText("⬇ Pull Cloud -> CSV");
                        if (ok) {
                            int count = attendanceManager.getStudents().size();
                            JOptionPane.showMessageDialog(dialog, "Successfully synchronized " + count + " records from Firebase!", "Sync Complete", JOptionPane.INFORMATION_MESSAGE);
                            updateDashboardLabels();
                            showTeacherDashboard();
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Failed to download from Firebase or cloud database is empty.", "Sync Notice", JOptionPane.WARNING_MESSAGE);
                        }
                    });
                }).start();
            }
        });

        resetBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Clear Firebase credentials and revert to local CSV storage?",
                    "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tfProjectId.setText("");
                tfApiKey.setText("");
                tfAuthDomain.setText("");
                tfStorageBucket.setText("");
                tfSenderId.setText("");
                tfAppId.setText("");
                tfDbUrl.setText("");
                tfAuthToken.setText("");
                config.setProjectId("");
                config.setApiKey("");
                config.setAuthDomain("");
                config.setStorageBucket("");
                config.setMessagingSenderId("");
                config.setAppId("");
                config.setDatabaseUrl("");
                config.setAuthToken("");
                config.saveToFile();
                statusDetail.setText("○ Local CSV Mode (Firebase Cleared)");
                statusDetail.setForeground(new Color(217, 119, 6));
                updateDashboardLabels();
                showTeacherDashboard();
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        dialog.add(topBar, BorderLayout.NORTH);
        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(bottomBar, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showBatchAttendanceDialog() {
        ArrayList<Student> list = attendanceManager.getStudents();
        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No registered students found to mark attendance!", "Empty Records",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Mark Daily Attendance - Batch Mode", true);
        dialog.setSize(880, 580);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PRIMARY_COLOR);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Daily Attendance Register - Select Present Students");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        togglePanel.setOpaque(false);

        JButton selectAllBtn = createStyledButton("Select All", BUTTON_COLOR);
        JButton deselectAllBtn = createStyledButton("Deselect All", new Color(100, 116, 139));
        JButton invertBtn = createStyledButton("Invert", ACCENT_COLOR);

        togglePanel.add(selectAllBtn);
        togglePanel.add(deselectAllBtn);
        togglePanel.add(invertBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(togglePanel, BorderLayout.EAST);

        String[] columnNames = {"Roll No", "Student Name", "Held", "Attended", "Current %", "Present Today"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Boolean.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        for (Student s : list) {
            model.addRow(new Object[]{
                    s.getRollNo(),
                    s.getName(),
                    s.getTotalClasses(),
                    s.getAttendedClasses(),
                    String.format("%.1f%%", s.getPercentage()),
                    Boolean.TRUE
            });
        }

        JTable table = new JTable(model);
        styleTable(table);

        DefaultTableCellRenderer rollRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(new Color(30, 41, 59));
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.LEFT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer percentRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                String s = (value != null) ? value.toString() : "0%";
                try {
                    double p = Double.parseDouble(s.replace("%", "").trim());
                    Color textCol = (p >= 75.0) ? new Color(21, 128, 61) : new Color(220, 38, 38);
                    if (!isSelected) {
                        comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    } else {
                        comp.setBackground(new Color(219, 234, 254));
                    }
                    comp.setForeground(textCol);
                } catch (Exception ex) {
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(rollRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(nameRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(percentRenderer);

        table.getColumnModel().getColumn(5).setCellRenderer((tbl, value, isSelected, hasFocus, r, c) -> {
            JCheckBox cb = new JCheckBox();
            cb.setHorizontalAlignment(SwingConstants.CENTER);
            cb.setSelected(value != null && (Boolean) value);
            cb.setOpaque(true);
            if (!isSelected) {
                cb.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
            } else {
                cb.setBackground(new Color(219, 234, 254));
            }
            return cb;
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        JLabel liveCountLabel = new JLabel("Present: " + list.size() + " | Absent: 0 (Total: " + list.size() + ")");
        liveCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        liveCountLabel.setForeground(TEXT_DARK);

        Runnable updateCount = () -> {
            int pres = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean val = (Boolean) model.getValueAt(i, 5);
                if (val != null && val) pres++;
            }
            int abs = model.getRowCount() - pres;
            liveCountLabel.setText("Present: " + pres + " | Absent: " + abs + " (Total: " + model.getRowCount() + ")");
        };

        model.addTableModelListener(e -> updateCount.run());

        selectAllBtn.addActionListener(e -> {
            for (int i = 0; i < model.getRowCount(); i++) model.setValueAt(Boolean.TRUE, i, 5);
            updateCount.run();
        });

        deselectAllBtn.addActionListener(e -> {
            for (int i = 0; i < model.getRowCount(); i++) model.setValueAt(Boolean.FALSE, i, 5);
            updateCount.run();
        });

        invertBtn.addActionListener(e -> {
            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean cur = (Boolean) model.getValueAt(i, 5);
                model.setValueAt(cur != null && !cur, i, 5);
            }
            updateCount.run();
        });

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(BG_LIGHT);
        bottomBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonGroup.setOpaque(false);

        JButton submitBtn = createStyledButton("Submit & Save Attendance", SUCCESS_COLOR);
        JButton cancelBtn = createStyledButton("Cancel", new Color(100, 116, 139));

        submitBtn.addActionListener(e -> {
            int rowCount = model.getRowCount();
            boolean[] attendance = new boolean[rowCount];
            int presentCount = 0;

            for (int i = 0; i < rowCount; i++) {
                Boolean isPresent = (Boolean) model.getValueAt(i, 5);
                attendance[i] = (isPresent != null && isPresent);
                if (attendance[i]) presentCount++;
            }

            int absentCount = rowCount - presentCount;

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    String.format("Confirm attendance submission?\n\nPresent: %d\nAbsent: %d\nTotal: %d\n\nAll records will increment 'Total Classes' by 1.",
                            presentCount, absentCount, rowCount),
                    "Confirm Batch Submission", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = attendanceManager.markBatchAttendance(attendance);
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                            "Attendance marked and synchronized successfully!\nTotal Students Updated: " + rowCount,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    updateDashboardLabels();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update attendance records.", "I/O Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonGroup.add(submitBtn);
        buttonGroup.add(cancelBtn);

        bottomBar.add(liveCountLabel, BorderLayout.WEST);
        bottomBar.add(buttonGroup, BorderLayout.EAST);

        dialog.add(topBar, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomBar, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showManageStudentsDialog() {
        JDialog dialog = new JDialog(this, "Manage Student Records", true);
        dialog.setSize(880, 560);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PRIMARY_COLOR);
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Student Roster Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        topBar.add(title, BorderLayout.WEST);

        String[] columns = {"Roll No", "Student Name", "Held", "Attended", "Percentage", "Defaulter?"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Runnable loadData = () -> {
            model.setRowCount(0);
            for (Student s : attendanceManager.getStudents()) {
                model.addRow(new Object[]{
                        s.getRollNo(),
                        s.getName(),
                        s.getTotalClasses(),
                        s.getAttendedClasses(),
                        String.format("%.1f%%", s.getPercentage()),
                        s.isDefaulter() ? "YES (<75%)" : "NO"
                });
            }
        };

        loadData.run();

        JTable table = new JTable(model);
        styleTable(table);

        DefaultTableCellRenderer rollRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(new Color(30, 41, 59));
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.LEFT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer percentRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                String s = (value != null) ? value.toString() : "0%";
                try {
                    double p = Double.parseDouble(s.replace("%", "").trim());
                    Color textCol = (p >= 75.0) ? new Color(21, 128, 61) : new Color(220, 38, 38);
                    if (!isSelected) {
                        comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    } else {
                        comp.setBackground(new Color(219, 234, 254));
                    }
                    comp.setForeground(textCol);
                } catch (Exception ex) {
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer defaulterRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                String valStr = (value != null) ? value.toString() : "";
                boolean isDef = valStr.contains("YES");
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                }
                comp.setForeground(isDef ? new Color(220, 38, 38) : new Color(21, 128, 61));
                return comp;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(rollRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(nameRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(percentRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(defaulterRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        buttonPanel.setBackground(BG_LIGHT);

        JButton addBtn = createStyledButton("+ Add Student", BUTTON_COLOR);
        JButton editBtn = createStyledButton("Edit Selected", ACCENT_COLOR);
        JButton deleteBtn = createStyledButton("Delete Selected", DANGER_COLOR);
        JButton closeBtn = createStyledButton("Close", new Color(100, 116, 139));

        addBtn.addActionListener(e -> showAddStudentInline(dialog, loadData));

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a student from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int roll = (Integer) model.getValueAt(selectedRow, 0);
            Student s = attendanceManager.findStudentByRoll(roll);
            if (s != null) {
                showEditStudentDialog(dialog, s, loadData);
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Please select a student to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int roll = (Integer) model.getValueAt(selectedRow, 0);
            String name = (String) model.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Are you sure you want to permanently delete:\n" +
                            "Roll: " + roll + " - " + name + "?\nThis action cannot be undone.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    attendanceManager.removeStudent(roll);
                    JOptionPane.showMessageDialog(dialog, "Student removed successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadData.run();
                    updateDashboardLabels();
                } catch (StudentNotFoundException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(closeBtn);

        dialog.add(topBar, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showAddStudentInline(JDialog parent, Runnable onRefresh) {
        JTextField rollField = createStyledTextField(12);
        JTextField nameField = createStyledTextField(12);
        JTextField totalField = createStyledTextField(12);
        totalField.setText("0");
        JTextField attendedField = createStyledTextField(12);
        attendedField.setText("0");

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 10));
        panel.add(new JLabel("Roll Number (Positive Integer):"));
        panel.add(rollField);
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Initial Total Classes:"));
        panel.add(totalField);
        panel.add(new JLabel("Initial Attended Classes:"));
        panel.add(attendedField);

        int res = JOptionPane.showConfirmDialog(parent, panel, "Add New Student Record", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String rollStr = rollField.getText().trim();
            String nameStr = nameField.getText().trim();
            String totalStr = totalField.getText().trim();
            String attendedStr = attendedField.getText().trim();

            if (!ValidationHelper.isPositiveInteger(rollStr)) {
                JOptionPane.showMessageDialog(parent, "Roll Number must be a positive integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int roll = Integer.parseInt(rollStr);

            if (!ValidationHelper.isValidStudentName(nameStr)) {
                JOptionPane.showMessageDialog(parent, "Please enter a valid student name (at least 2 letters)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationHelper.isNonNegativeInteger(totalStr) || !ValidationHelper.isNonNegativeInteger(attendedStr)) {
                JOptionPane.showMessageDialog(parent, "Classes held and attended must be non-negative integers!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int total = Integer.parseInt(totalStr);
            int attended = Integer.parseInt(attendedStr);

            if (!ValidationHelper.isValidAttendancePair(attended, total)) {
                JOptionPane.showMessageDialog(parent, "Attended classes cannot exceed total classes conducted!", "Logic Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                attendanceManager.registerStudent(roll, nameStr, total, attended);
                JOptionPane.showMessageDialog(parent, "Student " + nameStr + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                onRefresh.run();
                updateDashboardLabels();
            } catch (DuplicateStudentException | InvalidAttendanceDataException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditStudentDialog(JDialog parent, Student student, Runnable onRefresh) {
        JTextField nameField = createStyledTextField(14);
        nameField.setText(student.getName());
        JTextField totalField = createStyledTextField(14);
        totalField.setText(String.valueOf(student.getTotalClasses()));
        JTextField attendedField = createStyledTextField(14);
        attendedField.setText(String.valueOf(student.getAttendedClasses()));

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 10));
        panel.add(new JLabel("Roll Number (Read-only):"));
        JLabel rollDisplay = new JLabel(String.valueOf(student.getRollNo()));
        rollDisplay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rollDisplay.setForeground(PRIMARY_COLOR);
        panel.add(rollDisplay);
        panel.add(new JLabel("Student Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Total Classes Conducted:"));
        panel.add(totalField);
        panel.add(new JLabel("Attended Classes:"));
        panel.add(attendedField);

        int res = JOptionPane.showConfirmDialog(parent, panel, "Edit Student - Roll " + student.getRollNo(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            String nameStr = nameField.getText().trim();
            String totalStr = totalField.getText().trim();
            String attendedStr = attendedField.getText().trim();

            if (!ValidationHelper.isValidStudentName(nameStr)) {
                JOptionPane.showMessageDialog(parent, "Please enter a valid student name!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationHelper.isNonNegativeInteger(totalStr) || !ValidationHelper.isNonNegativeInteger(attendedStr)) {
                JOptionPane.showMessageDialog(parent, "Classes must be non-negative numbers!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int total = Integer.parseInt(totalStr);
            int attended = Integer.parseInt(attendedStr);
            if (!ValidationHelper.isValidAttendancePair(attended, total)) {
                JOptionPane.showMessageDialog(parent, "Attended classes cannot exceed total classes!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                attendanceManager.modifyStudent(student.getRollNo(), nameStr, total, attended);
                JOptionPane.showMessageDialog(parent, "Student record updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);
                onRefresh.run();
                updateDashboardLabels();
            } catch (StudentNotFoundException | InvalidAttendanceDataException ex) {
                JOptionPane.showMessageDialog(parent, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showReportsDialog(boolean initialDefaulterOnly) {
        ArrayList<Student> allStudents = attendanceManager.getStudents();
        if (allStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No student records available!", "Empty Records", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Attendance Reports & Eligibility Tracking", true);
        dialog.setSize(1040, 640);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        topPanel.setBackground(PRIMARY_COLOR);
        topPanel.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel topRow1 = new JPanel(new BorderLayout(10, 0));
        topRow1.setOpaque(false);

        JLabel title = new JLabel("NOIDA INSTITUTE OF ENGINEERING AND TECHNOLOGY (NIET) - Attendance Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        topRow1.add(title, BorderLayout.WEST);

        JPanel topRow2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRow2.setOpaque(false);

        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setForeground(Color.WHITE);
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField searchField = createStyledTextField(12);

        JComboBox<String> sortCombo = new JComboBox<>(new String[]{
                "Sort: Roll No",
                "Sort: Name (A-Z)",
                "Sort: Attendance % (Low-High)",
                "Sort: Attendance % (High-Low)"
        });
        sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton allBtn = createStyledButton("All", BUTTON_COLOR);
        JButton defaultersOnlyBtn = createStyledButton("Defaulters (<75%)", DANGER_COLOR);
        JButton eligibleOnlyBtn = createStyledButton("Eligible (>=75%)", SUCCESS_COLOR);

        topRow2.add(searchIcon);
        topRow2.add(searchField);
        topRow2.add(sortCombo);
        topRow2.add(allBtn);
        topRow2.add(defaultersOnlyBtn);
        topRow2.add(eligibleOnlyBtn);

        topPanel.add(topRow1);
        topPanel.add(topRow2);

        String[] columns = {"Roll No", "Student Name", "Attended", "Total Classes", "Percentage", "Eligibility Status", "Advisory Note"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        final int[] currentFilterMode = {initialDefaulterOnly ? 1 : 0};

        Runnable refreshTable = () -> {
            model.setRowCount(0);
            ArrayList<Student> source;
            if (currentFilterMode[0] == 1) {
                source = attendanceManager.getDefaulters();
            } else if (currentFilterMode[0] == 2) {
                source = attendanceManager.getEligibleStudents();
            } else {
                source = attendanceManager.getStudents();
            }

            int sortIndex = sortCombo.getSelectedIndex();
            if (sortIndex == 0) {
                Collections.sort(source);
            } else if (sortIndex == 1) {
                source.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
            } else if (sortIndex == 2) {
                source.sort(Comparator.comparingDouble(Student::getPercentage));
            } else if (sortIndex == 3) {
                source.sort((s1, s2) -> Double.compare(s2.getPercentage(), s1.getPercentage()));
            }

            String q = searchField.getText().trim().toLowerCase();
            for (Student s : source) {
                if (q.isEmpty() || String.valueOf(s.getRollNo()).contains(q) || s.getName().toLowerCase().contains(q)) {
                    model.addRow(new Object[]{
                            s.getRollNo(),
                            s.getName(),
                            s.getAttendedClasses(),
                            s.getTotalClasses(),
                            String.format("%.2f%%", s.getPercentage()),
                            s.getStatus(),
                            s.getAdvisoryMessage()
                    });
                }
            }
        };

        refreshTable.run();

        sortCombo.addActionListener(e -> refreshTable.run());

        allBtn.addActionListener(e -> {
            currentFilterMode[0] = 0;
            refreshTable.run();
        });

        defaultersOnlyBtn.addActionListener(e -> {
            currentFilterMode[0] = 1;
            refreshTable.run();
        });

        eligibleOnlyBtn.addActionListener(e -> {
            currentFilterMode[0] = 2;
            refreshTable.run();
        });

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTable.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTable.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTable.run(); }
        });

        JTable table = new JTable(model);
        styleTable(table);

        DefaultTableCellRenderer rollRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(new Color(30, 41, 59));
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.LEFT);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer percentRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                String s = (value != null) ? value.toString() : "0%";
                try {
                    double p = Double.parseDouble(s.replace("%", "").trim());
                    Color textCol = (p >= 75.0) ? new Color(21, 128, 61) : new Color(220, 38, 38);
                    if (!isSelected) {
                        comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    } else {
                        comp.setBackground(new Color(219, 234, 254));
                    }
                    comp.setForeground(textCol);
                } catch (Exception ex) {
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        DefaultTableCellRenderer advisoryRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                setHorizontalAlignment(JLabel.LEFT);
                setFont(new Font("Segoe UI", Font.PLAIN, 12));
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
                if (!isSelected) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    comp.setForeground(TEXT_DARK);
                } else {
                    comp.setBackground(new Color(219, 234, 254));
                    comp.setForeground(TEXT_DARK);
                }
                return comp;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(rollRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(nameRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(percentRenderer);

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, r, c);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String text = (value != null) ? value.toString() : "";
                if (!isSelected) {
                    lbl.setBackground(r % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                } else {
                    lbl.setBackground(new Color(219, 234, 254));
                }
                if (text.contains("Eligible")) {
                    lbl.setForeground(new Color(21, 128, 61));
                } else if (text.contains("Defaulter") || text.contains("Critical")) {
                    lbl.setForeground(new Color(220, 38, 38));
                } else {
                    lbl.setForeground(TEXT_DARK);
                }
                return lbl;
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(advisoryRenderer);

        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);
        table.getColumnModel().getColumn(6).setPreferredWidth(230);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(BG_LIGHT);
        bottomBar.setBorder(new EmptyBorder(12, 18, 12, 18));

        int totalCount = allStudents.size();
        int defaulterCount = attendanceManager.getDefaulters().size();
        int eligibleCount = totalCount - defaulterCount;
        double avgPct = attendanceManager.getAverageAttendancePercentage();

        JLabel summaryLabel = new JLabel(String.format("Total: %d | Eligible: %d | Defaulters: %d | Batch Avg: %.1f%%",
                totalCount, eligibleCount, defaulterCount, avgPct));
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryLabel.setForeground(TEXT_DARK);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionGroup.setOpaque(false);

        JButton exportExcelBtn = createStyledButton("Export to Excel (.csv)", SUCCESS_COLOR);
        JButton printBtn = createStyledButton("Print / PDF Report", BUTTON_COLOR);
        JButton closeBtn = createStyledButton("Close", new Color(100, 116, 139));

        exportExcelBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("NIET_Attendance_Report_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv"));
            int userChoice = chooser.showSaveDialog(dialog);
            if (userChoice == JFileChooser.APPROVE_OPTION) {
                File target = chooser.getSelectedFile();
                if (!target.getName().toLowerCase().endsWith(".csv")) {
                    target = new File(target.getAbsolutePath() + ".csv");
                }
                boolean ok = FileManager.exportToExcelCSV(target, attendanceManager.getStudents(), "Semester Attendance Report");
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                            "Excel report successfully generated!\nFile location:\n" + target.getAbsolutePath(),
                            "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to write export file.", "Export Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        printBtn.addActionListener(e -> {
            try {
                MessageFormat header = new MessageFormat("NOIDA INSTITUTE OF ENGINEERING AND TECHNOLOGY (NIET) - Attendance Report");
                MessageFormat footer = new MessageFormat("Smart Attendance Management System | Page - {0}");
                boolean complete = table.print(JTable.PrintMode.FIT_WIDTH, header, footer);
                if (complete) {
                    JOptionPane.showMessageDialog(dialog, "Printing job successfully completed or sent to printer queue.",
                            "Print Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (PrinterException pe) {
                JOptionPane.showMessageDialog(dialog, "Printing failed: " + pe.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> dialog.dispose());

        actionGroup.add(exportExcelBtn);
        actionGroup.add(printBtn);
        actionGroup.add(closeBtn);

        bottomBar.add(summaryLabel, BorderLayout.WEST);
        bottomBar.add(actionGroup, BorderLayout.EAST);

        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomBar, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showStudentLogin() {
        JTextField inputField = createStyledTextField(15);
        JPanel panel = new JPanel(new GridLayout(2, 1, 6, 6));
        JLabel prompt = new JLabel("Enter Student Roll Number to check attendance:");
        prompt.setFont(new Font("Segoe UI", Font.BOLD, 13));
        prompt.setForeground(TEXT_DARK);
        panel.add(prompt);
        panel.add(inputField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Student Portal Access", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) return;

        String input = inputField.getText().trim();
        if (!ValidationHelper.isPositiveInteger(input)) {
            JOptionPane.showMessageDialog(this, "Invalid Roll Number! Must be a positive integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int roll = Integer.parseInt(input);
        Student s = attendanceManager.findStudentByRoll(roll);

        if (s == null) {
            JOptionPane.showMessageDialog(this,
                    "Student with Roll Number " + roll + " not found in records!\nPlease contact faculty or administrator.",
                    "Record Not Found", JOptionPane.ERROR_MESSAGE);
            return;
        }

        showStudentDashboard(s);
    }

    private void showStudentDashboard(Student s) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(PRIMARY_COLOR);
        topHeader.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel titleGrp = new JPanel(new GridLayout(2, 1, 3, 3));
        titleGrp.setOpaque(false);
        JLabel nietLbl = new JLabel("NOIDA INSTITUTE OF ENGINEERING AND TECHNOLOGY (NIET)");
        nietLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nietLbl.setForeground(new Color(186, 230, 253));

        JLabel titleLbl = new JLabel("Student Attendance & Examination Eligibility Portal");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLbl.setForeground(Color.WHITE);

        titleGrp.add(nietLbl);
        titleGrp.add(titleLbl);

        JPanel rightGrp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightGrp.setOpaque(false);
        JLabel dateBadge = new JLabel("Academic Session 2026-27 | " + new SimpleDateFormat("EEE, dd MMM yyyy").format(new Date()));
        dateBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateBadge.setForeground(new Color(224, 242, 254));

        JButton logoutBtn = createStyledButton("Back to Main Menu", new Color(71, 85, 105));
        logoutBtn.addActionListener(e -> showMainMenu());

        rightGrp.add(dateBadge);
        rightGrp.add(logoutBtn);

        topHeader.add(titleGrp, BorderLayout.WEST);
        topHeader.add(rightGrp, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(BG_LIGHT);
        center.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel studentContainer = new JPanel();
        studentContainer.setLayout(new BoxLayout(studentContainer, BoxLayout.Y_AXIS));
        studentContainer.setOpaque(false);

        JPanel profileCard = new JPanel(new BorderLayout(16, 10));
        profileCard.setBackground(CARD_BG);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JPanel profileInfo = new JPanel(new GridLayout(3, 1, 4, 4));
        profileInfo.setOpaque(false);

        JLabel nameLbl = new JLabel(s.getName());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nameLbl.setForeground(TEXT_DARK);

        JLabel rollLbl = new JLabel("University Roll Number: " + s.getRollNo() + "  |  Department of Computer Science & Engineering");
        rollLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rollLbl.setForeground(ACCENT_COLOR);

        JLabel instLbl = new JLabel("Semester Examination Verification & Official Attendance Record");
        instLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        instLbl.setForeground(TEXT_MUTED);

        profileInfo.add(nameLbl);
        profileInfo.add(rollLbl);
        profileInfo.add(instLbl);

        profileCard.add(profileInfo, BorderLayout.CENTER);

        boolean isEligible = !s.isDefaulter();
        JPanel statusBadgePanel = new JPanel(new GridBagLayout());
        statusBadgePanel.setOpaque(false);
        JLabel mainBadge = new JLabel(isEligible ? "  ELIGIBLE FOR SEMESTER EXAM  " : "  CRITICAL / SHORT ATTENDANCE  ");
        mainBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        mainBadge.setOpaque(true);
        mainBadge.setBackground(isEligible ? new Color(220, 252, 231) : new Color(254, 226, 226));
        mainBadge.setForeground(isEligible ? SUCCESS_COLOR : DANGER_COLOR);
        mainBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isEligible ? SUCCESS_COLOR : DANGER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        statusBadgePanel.add(mainBadge);
        profileCard.add(statusBadgePanel, BorderLayout.EAST);

        JPanel metricsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsRow.setOpaque(false);
        metricsRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 110));

        metricsRow.add(createKpiCard("Lectures Held", String.valueOf(s.getTotalClasses()), "Total Conducted in Term", BUTTON_COLOR));
        metricsRow.add(createKpiCard("Lectures Attended", String.valueOf(s.getAttendedClasses()), "Physical Presence Count", ACCENT_COLOR));
        metricsRow.add(createKpiCard("Attendance Percentage", String.format("%.2f%%", s.getPercentage()), "Threshold Requirement: 75.0%", isEligible ? SUCCESS_COLOR : DANGER_COLOR));
        metricsRow.add(createKpiCard("Eligibility Status", isEligible ? "Eligible" : "Critical (<75%)", "Academic Clearance", isEligible ? SUCCESS_COLOR : DANGER_COLOR));

        JPanel advisoryCard = new JPanel(new BorderLayout(14, 10));
        Color advBg = isEligible ? new Color(240, 253, 244) : new Color(254, 242, 242);
        Color advBorder = isEligible ? new Color(187, 247, 208) : new Color(254, 202, 202);
        Color advText = isEligible ? new Color(22, 101, 52) : new Color(153, 27, 27);

        advisoryCard.setBackground(advBg);
        advisoryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(advBorder, 1),
                new EmptyBorder(18, 22, 18, 22)
        ));

        JLabel advTitle = new JLabel("Official Attendance Advisory & Examination Forecast");
        advTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        advTitle.setForeground(advText);

        JLabel advMsg = new JLabel(s.getAdvisoryMessage());
        advMsg.setFont(new Font("Segoe UI", Font.BOLD, 17));
        advMsg.setForeground(advText);

        advisoryCard.add(advTitle, BorderLayout.NORTH);
        advisoryCard.add(advMsg, BorderLayout.CENTER);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        actionRow.setOpaque(false);

        JButton printSlipBtn = createStyledButton("Print Official Attendance Slip", BUTTON_COLOR);
        JButton switchStudentBtn = createStyledButton("Check Another Roll Number", ACCENT_COLOR);
        JButton backBtn = createStyledButton("Back to Main Menu", new Color(100, 116, 139));

        printSlipBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "==============================================================\n" +
                            "NOIDA INSTITUTE OF ENGINEERING AND TECHNOLOGY (NIET), GREATER NOIDA\n" +
                            "SMART ATTENDANCE MANAGEMENT SYSTEM - OFFICIAL ATTENDANCE SLIP\n" +
                            "==============================================================\n" +
                            "Student Name: " + s.getName() + "\n" +
                            "Roll Number: " + s.getRollNo() + "\n" +
                            "Classes Attended: " + s.getAttendedClasses() + " / " + s.getTotalClasses() + "\n" +
                            "Percentage: " + String.format("%.2f%%", s.getPercentage()) + "\n" +
                            "Status: " + s.getStatus() + "\n" +
                            "Advisory Note: " + s.getAdvisoryMessage() + "\n" +
                            "Generated On: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n" +
                            "==============================================================\n" +
                            "(Official examination verification record)",
                    "Print Preview / Official Attendance Slip", JOptionPane.INFORMATION_MESSAGE);
        });

        switchStudentBtn.addActionListener(e -> showStudentLogin());
        backBtn.addActionListener(e -> showMainMenu());

        actionRow.add(printSlipBtn);
        actionRow.add(switchStudentBtn);
        actionRow.add(backBtn);

        studentContainer.add(profileCard);
        studentContainer.add(Box.createVerticalStrut(16));
        studentContainer.add(metricsRow);
        studentContainer.add(Box.createVerticalStrut(16));
        studentContainer.add(advisoryCard);
        studentContainer.add(Box.createVerticalStrut(20));
        studentContainer.add(actionRow);

        center.add(studentContainer, BorderLayout.NORTH);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CARD_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                new EmptyBorder(10, 24, 10, 24)
        ));
        JLabel statusText = new JLabel("Official Examination Record | Noida Institute of Engineering and Technology (NIET) | Secure Academic Portal");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusText.setForeground(TEXT_MUTED);
        footer.add(statusText, BorderLayout.WEST);

        mainPanel.add(topHeader, BorderLayout.NORTH);
        mainPanel.add(center, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new SmartAttendanceUI().setVisible(true);
        });
    }
}

