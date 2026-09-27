package lms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * A text field plus a small calendar button that opens a popup month
 * grid, so a date can be picked instead of always typed by hand. The
 * text field stays editable -- typing a valid date (per the given
 * formatter) still works and keeps the two in sync.
 */
@SuppressWarnings("serial")
public class DatePickerField extends JPanel {

    private final JTextField field;
    private final DateTimeFormatter format;

    // Fires after the field's date changes, however it changed (typed,
    // or picked from the popup) -- e.g. AddEditMemberPanel uses this on
    // the Join Date field to recompute the default End Date.
    private Runnable onDateChanged = () -> { };

    public DatePickerField(DateTimeFormatter format) {
        this.format = format;
        setOpaque(false);
        setLayout(new BorderLayout(4, 0));

        field = new JTextField();
        field.setFont(Theme.FONT_FIELD);
        field.setBorder(new CompoundBorder(new LineBorder(Theme.DIVIDER, 1), new EmptyBorder(2, 6, 2, 6)));
        field.addActionListener(e -> onDateChanged.run());
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                onDateChanged.run();
            }
        });
        add(field, BorderLayout.CENTER);

        JButton calendarButton = new JButton(RowIcons.calendar(14, Theme.TEXT_PRIMARY));
        calendarButton.setToolTipText("Pick a date");
        calendarButton.setFocusPainted(false);
        calendarButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        calendarButton.setBackground(Theme.CARD_BG);
        calendarButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calendarButton.setPreferredSize(new Dimension(22, 10));
        calendarButton.addActionListener(e -> showPopup(calendarButton));
        add(calendarButton, BorderLayout.EAST);
    }

    /** Called after the date changes, from typing or from the popup. */
    public void setOnDateChanged(Runnable listener) {
        this.onDateChanged = listener;
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String text) {
        field.setText(text);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        field.setEnabled(enabled);
    }

    /** The field's current text parsed as a date, or null if it isn't one. */
    public LocalDate getDate() {
        try {
            return LocalDate.parse(field.getText().trim(), format);
        } catch (DateTimeParseException | NullPointerException ex) {
            return null;
        }
    }

    public void setDate(LocalDate date) {
        field.setText(date == null ? "" : format.format(date));
    }

    private void showPopup(JButton anchor) {
        LocalDate current = getDate();
        YearMonth month = current != null ? YearMonth.from(current) : YearMonth.now();

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new LineBorder(Theme.DIVIDER, 1));
        popup.setLayout(new BorderLayout());
        popup.add(buildCalendar(month, current, popup), BorderLayout.CENTER);
        popup.show(anchor, anchor.getWidth() - 210, anchor.getHeight());
    }

    /** Rebuilt fresh every time the popup opens or the month is navigated. */
    private JPanel buildCalendar(YearMonth month, LocalDate selected, JPopupMenu popup) {
        JPanel root = new JPanel(new BorderLayout(0, 4));
        root.setBackground(Theme.CARD_BG);
        root.setBorder(new EmptyBorder(6, 6, 6, 6));

        JLabel monthLabel = new JLabel(
            month.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.US) + " " + month.getYear(),
            SwingConstants.CENTER);
        monthLabel.setFont(Theme.FONT_CARD_TITLE);
        monthLabel.setForeground(Theme.TEXT_PRIMARY);

        JButton prev = navButton(RowIcons.triangle(9, false, Theme.TEXT_PRIMARY));
        JButton next = navButton(RowIcons.triangle(9, true, Theme.TEXT_PRIMARY));
        prev.addActionListener(e -> refreshPopup(popup, month.minusMonths(1), selected));
        next.addActionListener(e -> refreshPopup(popup, month.plusMonths(1), selected));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(prev, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        grid.setOpaque(false);
        for (String dow : new String[] {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"}) {
            JLabel dowLabel = new JLabel(dow, SwingConstants.CENTER);
            dowLabel.setFont(Theme.FONT_CARD_MUTED);
            dowLabel.setForeground(Theme.TEXT_MUTED);
            grid.add(dowLabel);
        }

        LocalDate firstOfMonth = month.atDay(1);
        int leadingBlanks = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday-first grid
        for (int i = 0; i < leadingBlanks; i++) {
            grid.add(new JLabel(""));
        }
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(Theme.FONT_LABEL);
            dayButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
            dayButton.setFocusPainted(false);
            dayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            boolean isSelected = date.equals(selected);
            dayButton.setBackground(isSelected ? Theme.NAVY : Theme.CARD_BG);
            dayButton.setForeground(isSelected ? Color.WHITE : Theme.TEXT_PRIMARY);
            dayButton.setBorder(new LineBorder(date.equals(today) ? Theme.BLUE_ACCENT : Theme.DIVIDER, 1));
            dayButton.addActionListener(e -> {
                setDate(date);
                popup.setVisible(false);
                onDateChanged.run();
            });
            grid.add(dayButton);
        }
        root.add(grid, BorderLayout.CENTER);

        return root;
    }

    private JButton navButton(javax.swing.Icon icon) {
        JButton b = new JButton(icon);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Swaps the popup's content for a different month without closing it. */
    private void refreshPopup(JPopupMenu popup, YearMonth newMonth, LocalDate selected) {
        popup.removeAll();
        popup.add(buildCalendar(newMonth, selected, popup), BorderLayout.CENTER);
        popup.revalidate();
        popup.repaint();
    }
}
