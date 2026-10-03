package lms.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lms.core.Library;

/**
 * A text field plus a small calendar button that opens a popup date
 * picker. The date can only be set by picking a day from that popup --
 * the text field itself is read-only, so there's no way to type in an
 * invalid (or just plain wrong) date by hand.
 *
 * The popup has three levels, so a date far from today (a 1987 birth
 * date, say) is a handful of clicks away instead of dozens of
 * Prev-Month clicks: DAY (the usual one-month day grid) drills UP into
 * MONTH (a 12-month grid for one year) by clicking the day grid's own
 * month/year heading, and MONTH drills further UP into YEAR (a page of
 * 12 years) by clicking ITS year heading. Picking a month or year
 * drills back DOWN one level, landing on that month/year.
 *
 * Whatever's set via setMinDate()/setMaxDate() applies at every level,
 * the same way it already applied to individual days: a month or year
 * that's entirely out of range is shown, disabled, rather than hidden
 * -- so it's obvious *why* e.g. a birth date can't be set to 2030, not
 * just that 2030 silently isn't reachable.
 */
@SuppressWarnings("serial")
public class DatePickerField extends JPanel {

    // Years shown per page of the YEAR view, laid out 4 columns x 3
    // rows -- same shape as the MONTH view's 12-month grid.
    private static final int YEAR_PAGE_SIZE = 12;

    private final JTextField field;
    private final DateTimeFormatter format;
    private LocalDate minDate;
    private LocalDate maxDate;

    // Fires after a date is picked from the popup -- e.g.
    // AddEditMemberPanel uses this on the Join Date field to recompute
    // the default End Date.
    private Runnable onDateChanged = () -> { };

    public DatePickerField(DateTimeFormatter format) {
        this.format = format;
        setOpaque(false);
        setLayout(new BorderLayout(4, 0));

        field = new JTextField();
        field.setFont(Theme.FONT_FIELD);
        field.setBorder(Theme.fieldBorder());
        field.setEditable(false);
        // Not just non-editable but non-focusable, so clicking it can
        // never show a text caret -- the popup is the only way in.
        field.setFocusable(false);
        // A non-editable JTextField otherwise falls back to the L&F's
        // grayed-out "inactive" background; keep it looking like a
        // normal white field instead.
        field.setBackground(Theme.CARD_BG);
        add(field, BorderLayout.CENTER);

        JButton calendarButton = new JButton(RowIcons.calendar(14, Theme.TEXT_PRIMARY));
        calendarButton.setToolTipText("Pick a date");
        calendarButton.setFocusPainted(false);
        calendarButton.setContentAreaFilled(false);
        calendarButton.setBorder(Theme.outline());
        Theme.handCursor(calendarButton);
        calendarButton.setPreferredSize(new Dimension(24, 10));
        calendarButton.addActionListener(e -> showPopup(calendarButton));
        add(calendarButton, BorderLayout.EAST);
    }

    /** Called after the date changes, from typing or from the popup. */
    public void setOnDateChanged(Runnable listener) {
        this.onDateChanged = listener;
    }

    /** Days before this date are shown disabled in the popup (at every level). Null clears the bound. */
    public void setMinDate(LocalDate minDate) {
        this.minDate = minDate;
    }

    /** Days after this date are shown disabled in the popup (at every level). Null clears the bound. */
    public void setMaxDate(LocalDate maxDate) {
        this.maxDate = maxDate;
    }

    public String getText() {
        return field.getText();
    }

    public void setText(String text) {
        field.setText(text);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (Component part : getComponents()) { // the text box and the calendar button
            part.setEnabled(enabled);
        }
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
        YearMonth month = current != null ? YearMonth.from(current) : YearMonth.from(Library.today());

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(new LineBorder(Theme.FIELD_BORDER, 1));
        popup.setLayout(new BorderLayout());
        popup.add(buildDayView(month, current, popup), BorderLayout.CENTER);

        // Right-aligned under the field -- or above it when the window has no room below
        // (the Borrow/Return date fields sit near the bottom of the screen).
        Dimension size = popup.getPreferredSize();
        javax.swing.JRootPane root = anchor.getRootPane();
        int bottom = javax.swing.SwingUtilities.convertPoint(anchor, 0, anchor.getHeight(), root.getContentPane()).y;
        boolean fitsBelow = bottom + size.height <= root.getContentPane().getHeight();
        popup.show(anchor, anchor.getWidth() - size.width, fitsBelow ? anchor.getHeight() : -size.height);
    }

    // ---- DAY: the usual one-month grid. Its heading drills UP to MONTH. ----

    /** Rebuilt fresh every time the popup opens or navigates. */
    private JPanel buildDayView(YearMonth month, LocalDate selected, JPopupMenu popup) {
        JPanel root = popupRoot();

        JButton heading = headingButton(
            month.getMonth().getDisplayName(TextStyle.FULL, Locale.US) + " " + month.getYear());
        heading.addActionListener(e -> swap(popup, buildMonthView(month.getYear(), selected, popup)));

        JButton prev = navButton(RowIcons.triangle(9, false, Theme.TEXT_PRIMARY));
        JButton next = navButton(RowIcons.triangle(9, true, Theme.TEXT_PRIMARY));
        prev.addActionListener(e -> swap(popup, buildDayView(month.minusMonths(1), selected, popup)));
        next.addActionListener(e -> swap(popup, buildDayView(month.plusMonths(1), selected, popup)));
        root.add(popupHeader(prev, heading, next), BorderLayout.NORTH);

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
        LocalDate today = Library.today();
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            boolean disabled = (minDate != null && date.isBefore(minDate)) || (maxDate != null && date.isAfter(maxDate));
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFont(Theme.FONT_LABEL);
            dayButton.setMargin(new Insets(2, 2, 2, 2));
            dayButton.setFocusPainted(false);
            Theme.handCursor(dayButton);
            boolean isSelected = date.equals(selected);
            dayButton.setBackground(isSelected ? Theme.NAVY : Theme.CARD_BG);
            dayButton.setForeground(disabled ? Theme.TEXT_MUTED : (isSelected ? Color.WHITE : Theme.TEXT_PRIMARY));
            dayButton.setBorder(new LineBorder(date.equals(today) ? Theme.BLUE_ACCENT : Theme.DIVIDER, 1));
            dayButton.setEnabled(!disabled);
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

    // ---- MONTH: one year's 12 months. Heading drills UP to YEAR; picking a month drills DOWN to DAY. ----

    private JPanel buildMonthView(int year, LocalDate selected, JPopupMenu popup) {
        JPanel root = popupRoot();

        JButton heading = headingButton(String.valueOf(year));
        heading.addActionListener(e -> swap(popup, buildYearView(pageStartFor(year), selected, popup)));

        JButton prev = navButton(RowIcons.triangle(9, false, Theme.TEXT_PRIMARY));
        JButton next = navButton(RowIcons.triangle(9, true, Theme.TEXT_PRIMARY));
        prev.addActionListener(e -> swap(popup, buildMonthView(year - 1, selected, popup)));
        next.addActionListener(e -> swap(popup, buildMonthView(year + 1, selected, popup)));
        root.add(popupHeader(prev, heading, next), BorderLayout.NORTH);

        YearMonth thisMonth = YearMonth.from(Library.today());
        JPanel grid = new JPanel(new GridLayout(3, 4, 4, 4));
        grid.setOpaque(false);
        for (int m = 1; m <= 12; m++) {
            YearMonth candidate = YearMonth.of(year, m);
            // Out of range if the whole month falls before minDate or after maxDate.
            boolean disabled = (minDate != null && candidate.atEndOfMonth().isBefore(minDate))
                || (maxDate != null && candidate.atDay(1).isAfter(maxDate));
            boolean isSelected = selected != null && selected.getYear() == year && selected.getMonthValue() == m;
            JButton cell = cellButton(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.US),
                isSelected, disabled, candidate.equals(thisMonth));
            final int monthValue = m;
            cell.addActionListener(e -> swap(popup, buildDayView(YearMonth.of(year, monthValue), selected, popup)));
            grid.add(cell);
        }
        root.add(grid, BorderLayout.CENTER);

        return root;
    }

    // ---- YEAR: one page of 12 years -- the top level. Picking a year drills DOWN to MONTH. ----

    private JPanel buildYearView(int pageStart, LocalDate selected, JPopupMenu popup) {
        JPanel root = popupRoot();

        int pageEnd = pageStart + YEAR_PAGE_SIZE - 1;
        JLabel heading = new JLabel(pageStart + "\u2013" + pageEnd, SwingConstants.CENTER);
        heading.setFont(Theme.display(12));
        heading.setForeground(Theme.HEADING);

        JButton prev = navButton(RowIcons.triangle(9, false, Theme.TEXT_PRIMARY));
        JButton next = navButton(RowIcons.triangle(9, true, Theme.TEXT_PRIMARY));
        prev.addActionListener(e -> swap(popup, buildYearView(pageStart - YEAR_PAGE_SIZE, selected, popup)));
        next.addActionListener(e -> swap(popup, buildYearView(pageStart + YEAR_PAGE_SIZE, selected, popup)));
        root.add(popupHeader(prev, heading, next), BorderLayout.NORTH);

        int thisYear = Library.today().getYear();
        JPanel grid = new JPanel(new GridLayout(3, 4, 4, 4));
        grid.setOpaque(false);
        for (int y = pageStart; y <= pageEnd; y++) {
            LocalDate firstOfYear = LocalDate.of(y, 1, 1);
            LocalDate lastOfYear = LocalDate.of(y, 12, 31);
            // Out of range if the whole year falls before minDate or after maxDate.
            boolean disabled = (minDate != null && lastOfYear.isBefore(minDate))
                || (maxDate != null && firstOfYear.isAfter(maxDate));
            boolean isSelected = selected != null && selected.getYear() == y;
            JButton cell = cellButton(String.valueOf(y), isSelected, disabled, y == thisYear);
            final int year = y;
            cell.addActionListener(e -> swap(popup, buildMonthView(year, selected, popup)));
            grid.add(cell);
        }
        root.add(grid, BorderLayout.CENTER);

        return root;
    }

    // ---- Shared popup-building helpers ----

    private JPanel popupRoot() {
        JPanel root = new JPanel(new BorderLayout(0, 4));
        root.setBackground(Theme.CARD_BG);
        root.setBorder(new EmptyBorder(6, 6, 6, 6));
        root.setPreferredSize(new Dimension(214, 168)); // fits a six-week month
        return root;
    }

    private JPanel popupHeader(JButton prev, JComponent heading, JButton next) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(prev, BorderLayout.WEST);
        header.add(heading, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        return header;
    }

    /** Looks like the plain title label it replaces, but drills up a level on click. */
    private JButton headingButton(String text) {
        JButton b = new JButton(text);
        b.setFont(Theme.display(12));
        b.setForeground(Theme.HEADING);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        b.setContentAreaFilled(false);
        Theme.handCursor(b);
        return b;
    }

    private JButton navButton(javax.swing.Icon icon) {
        JButton b = new JButton(icon);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        Theme.handCursor(b);
        return b;
    }

    /** One month or year cell in the MONTH/YEAR grids -- same look as a day cell in the DAY grid. */
    private JButton cellButton(String text, boolean selected, boolean disabled, boolean isCurrent) {
        JButton b = new JButton(text);
        b.setFont(Theme.FONT_LABEL);
        b.setMargin(new Insets(6, 2, 6, 2));
        b.setFocusPainted(false);
        Theme.handCursor(b);
        b.setBackground(selected ? Theme.NAVY : Theme.CARD_BG);
        b.setForeground(disabled ? Theme.TEXT_MUTED : (selected ? Color.WHITE : Theme.TEXT_PRIMARY));
        b.setBorder(new LineBorder(isCurrent ? Theme.BLUE_ACCENT : Theme.DIVIDER, 1));
        b.setEnabled(!disabled);
        return b;
    }

    /** Swaps the popup's content for a different view/page without closing it. */
    private void swap(JPopupMenu popup, JPanel content) {
        popup.removeAll();
        popup.add(content, BorderLayout.CENTER);
        popup.revalidate();
        popup.repaint();
    }

    /** The 12-year page (aligned to multiples of YEAR_PAGE_SIZE) that contains this year. */
    private static int pageStartFor(int year) {
        return Math.floorDiv(year, YEAR_PAGE_SIZE) * YEAR_PAGE_SIZE;
    }
}
