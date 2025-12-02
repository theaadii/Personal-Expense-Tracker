import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Date;
import java.text.SimpleDateFormat;

class Expense {
    String category;
    double amount;
    String date; // stored as "yyyy-MM-dd"

    public Expense(String category, double amount, String date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }
}

public class ExpenseTracker extends JFrame {

    private JTextField tfAmount;
    private JSpinner dateSpinner;                 // <- date picker
    private JComboBox<String> cbCategory;
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private JLabel lblTotal;

    public ExpenseTracker() {
        setTitle("Expense Tracker");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top Label
        JLabel heading = new JLabel("Personal Expense Tracker", JLabel.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(new Color(0, 102, 153));
        heading.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(heading, BorderLayout.NORTH);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        inputPanel.setBackground(new Color(230, 245, 255));

        cbCategory = new JComboBox<>(new String[]{"Food", "Transport", "Bills", "Shopping", "Others"});
        cbCategory.setEditable(true); // allow user to type custom category
        JButton btnAddCategory = new JButton("Add Category");
        tfAmount = new JTextField();

        // Date picker using JSpinner (calendar-style control)
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now())); // default: today

        JButton btnAdd = new JButton("Add Expense");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnAnalysis = new JButton("Month-wise Analysis");

        inputPanel.add(new JLabel("Category:"));
        // group the category combo box and the add button
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        categoryPanel.setBackground(new Color(230, 245, 255));
        categoryPanel.add(cbCategory);
        categoryPanel.add(btnAddCategory);
        inputPanel.add(categoryPanel);
        inputPanel.add(new JLabel("Amount:"));
        inputPanel.add(tfAmount);
        inputPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        inputPanel.add(dateSpinner);
        inputPanel.add(btnAdd);
        inputPanel.add(btnDelete);
        inputPanel.add(btnAnalysis);
        inputPanel.add(new JLabel("")); // empty placeholder

        add(inputPanel, BorderLayout.WEST);

        // Table Panel
        tableModel = new DefaultTableModel(new String[]{"Category", "Amount", "Date"}, 0);
        expenseTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal = new JLabel("Total: ₹0.0");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
        bottomPanel.add(lblTotal);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event Listeners
        btnAddCategory.addActionListener(e -> {
            String newCat = JOptionPane.showInputDialog(this, "Enter new category:", "Add Category", JOptionPane.PLAIN_MESSAGE);
            if (newCat != null) {
                newCat = newCat.trim();
                if (!newCat.isEmpty()) {
                    // avoid duplicates (case-insensitive)
                    DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cbCategory.getModel();
                    boolean exists = false;
                    for (int i = 0; i < model.getSize(); i++) {
                        if (model.getElementAt(i).equalsIgnoreCase(newCat)) { exists = true; break; }
                    }
                    if (!exists) {
                        cbCategory.addItem(newCat);
                    }
                    cbCategory.setSelectedItem(newCat);
                }
            }
        });
        btnAdd.addActionListener(e -> addExpense());
        btnDelete.addActionListener(e -> deleteSelected());
        btnAnalysis.addActionListener(e -> showMonthlyAnalysis());
    }

    private void addExpense() {
        String category = cbCategory.getSelectedItem().toString();
        double amount;

        try {
            amount = Double.parseDouble(tfAmount.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get date from spinner
        Date selectedDate = (Date) dateSpinner.getValue();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(selectedDate);

        Expense exp = new Expense(category, amount, dateStr);
        expenseList.add(exp);

        tableModel.addRow(new Object[]{category, "₹" + amount, dateStr});
        tfAmount.setText("");
        updateTotal();
    }

    private void deleteSelected() {
        int selected = expenseTable.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }
        expenseList.remove(selected);
        tableModel.removeRow(selected);
        updateTotal();
    }

    private void updateTotal() {
        double total = 0;
        for (Expense e : expenseList) {
            total += e.amount;
        }
        lblTotal.setText("Total: ₹" + total);
    }

    // Month-wise spending analysis
    private void showMonthlyAnalysis() {
        if (expenseList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No expenses to analyze.");
            return;
        }

        // TreeMap to keep months sorted
        Map<String, Double> monthTotals = new TreeMap<>();

        for (Expense e : expenseList) {
            try {
                // e.date format: yyyy-MM-dd
                String monthKey = e.date.substring(0, 7); // "yyyy-MM"
                monthTotals.put(monthKey, monthTotals.getOrDefault(monthKey, 0.0) + e.amount);
            } catch (Exception ex) {
                // ignore invalid date strings
            }
        }

        DefaultTableModel model = new DefaultTableModel(new String[]{"Month (YYYY-MM)", "Total (₹)"}, 0);
        for (Map.Entry<String, Double> entry : monthTotals.entrySet()) {
            model.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }

        JTable table = new JTable(model);
        JScrollPane pane = new JScrollPane(table);

        JDialog dialog = new JDialog(this, "Month-wise Spending Analysis", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(pane, BorderLayout.CENTER);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExpenseTracker().setVisible(true));
    }
}
