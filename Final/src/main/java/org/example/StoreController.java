
package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.*;

public class StoreController implements OrderObserver {

    @FXML private Label welcomeLabel;
    @FXML private Label accountLabel;
    @FXML private Label cartTotalLabel;
    @FXML private Label itemDetailsLabel;
    @FXML private Label notificationCountLabel;

    @FXML private TextField searchField;

    @FXML private TableView<Item> catalogTable;
    @FXML private TableColumn<Item, Integer> idColumn;
    @FXML private TableColumn<Item, String> titleColumn;
    @FXML private TableColumn<Item, String> authorColumn;
    @FXML private TableColumn<Item, String> typeColumn;
    @FXML private TableColumn<Item, Double> priceColumn;
    @FXML private TableColumn<Item, Integer> stockColumn;

    @FXML private ListView<String> cartList;
    @FXML private ComboBox<String> paymentCombo;

    @FXML private TabPane mainTabPane;
    @FXML private Tab notificationsTab;
    @FXML private VBox notificationsBox;

    private Customer customer;
    private Order cart;


    private final Map<Order, TitledPane> orderNotifications =
            new IdentityHashMap<>();


    private final Set<Order> unreadOrders =
            Collections.newSetFromMap(new IdentityHashMap<>());

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("itemID"));

        titleColumn.setCellValueFactory(
                new PropertyValueFactory<>("title"));

        authorColumn.setCellValueFactory(
                new PropertyValueFactory<>("author"));

        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("price"));

        typeColumn.setCellValueFactory(
                new PropertyValueFactory<>("type"));

        stockColumn.setCellValueFactory(
                new PropertyValueFactory<>("stock"));

        paymentCombo.setItems(
                FXCollections.observableArrayList(
                        "GCash",
                        "Credit Card"
                )
        );

        paymentCombo.getSelectionModel().selectFirst();

        catalogTable.setItems(
                FXCollections.observableArrayList(
                        AppServices.BOOKSTORE.getCatalog()
                )
        );

        searchField.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        filterCatalog(newValue)
        );

        catalogTable.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldItem, newItem) ->
                                showItemDetails(newItem)
                );

        updateNotificationCount();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        this.cart = AppServices.BOOKSTORE.createOrder(customer);

        welcomeLabel.setText(
                "Welcome back, " + customer.getName() + " ✦"
        );

        accountLabel.setText(customer.getEmail());

        refreshCart();
    }


    @FXML
    private void signOut() {
        try {
            StoreApp.showLogin();
        } catch (Exception exception) {
            alert(
                    "Unable to return to sign in: "
                            + exception.getMessage()
            );
        }
    }


    @FXML
    private void openNotifications() {
        if (mainTabPane != null && notificationsTab != null) {
            mainTabPane.getSelectionModel().select(notificationsTab);
        }
    }

    @FXML
    private void addSelectedItem() {
        Item selected =
                catalogTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            alert("Choose a title from the shelves first.");
            return;
        }

        try {
            cart.addItem(selected);
            refreshCart();
        } catch (RuntimeException exception) {
            alert(exception.getMessage());
        }
    }


    @FXML
    private void removeSelectedCartItem() {
        int selectedIndex =
                cartList.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0) {
            alert("Select a cart item you want to remove.");
            return;
        }

        List<Item> distinctItems =
                new ArrayList<>(
                        new LinkedHashSet<>(cart.getItems())
                );

        if (selectedIndex >= distinctItems.size()) {
            return;
        }

        Item selectedItem = distinctItems.get(selectedIndex);
        int originalIndex = cart.getItems().indexOf(selectedItem);

        try {
            cart.removeItemAt(originalIndex);
            refreshCart();
        } catch (RuntimeException exception) {
            alert(exception.getMessage());
        }
    }


    @FXML
    private void clearCart() {
        if (cart == null) {
            return;
        }

        try {
            while (!cart.isEmpty()) {
                cart.removeItemAt(0);
            }

            refreshCart();
        } catch (RuntimeException exception) {
            alert(exception.getMessage());
        }
    }


    @FXML
    private void checkout() {
        if (cart == null || customer == null) {
            alert("Please sign in again before checking out.");
            return;
        }

        if (cart.isEmpty()) {
            alert("Your cart is empty. Add a book before checking out.");
            return;
        }

        try {

            cart.addObserver(this);

            AppServices.BOOKSTORE.checkout(
                    cart,
                    paymentCombo.getValue()
            );


            ensureOrderNotification(cart);


            refreshCatalogStock();


            cart = AppServices.BOOKSTORE.createOrder(customer);
            refreshCart();

        } catch (RuntimeException exception) {
            alert(
                    exception.getMessage() == null
                            ? "Checkout could not be completed."
                            : exception.getMessage()
            );
        }
    }


    private String buildReceipt(Order order) {
        StringBuilder receipt = new StringBuilder();

        receipt.append("✦  ONLINE BOOKSTORE\n")
                .append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                .append("DIGITAL PURCHASE RECEIPT\n\n")
                .append("Order #: ")
                .append(order.getOrderID())
                .append('\n')
                .append("Customer: ")
                .append(order.getCustomer().getName())
                .append('\n')
                .append("Email: ")
                .append(order.getCustomer().getEmail())
                .append('\n')
                .append("Placed: ")
                .append(order.getCreatedAt())
                .append("\n\n")
                .append("ITEMS\n");

        for (Map.Entry<Item, Long> entry :
                itemQuantities(order).entrySet()) {

            Item item = entry.getKey();
            long quantity = entry.getValue();

            receipt.append(" • ")
                    .append(item.getTitle());

            if (quantity > 1) {
                receipt.append(" ")
                        .append(quantity)
                        .append("x");
            }

            receipt.append(" — ₱")
                    .append(
                            String.format(
                                    Locale.US,
                                    "%.2f",
                                    item.getPrice() * quantity
                            )
                    )
                    .append('\n');
        }

        receipt.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                .append("TOTAL: ₱")
                .append(
                        String.format(
                                Locale.US,
                                "%.2f",
                                order.calculateTotal()
                        )
                )
                .append('\n')
                .append("Payment: ")
                .append(order.getPayment().getPaymentMethod())
                .append('\n')
                .append("Reference: ")
                .append(order.getPayment().getPaymentID())
                .append('\n')
                .append("Status: ")
                .append(order.getStatus())
                .append("\n\n")
                .append("Thanks for making room for another story. ✦\n")
                .append(
                        ""
                );

        return receipt.toString();
    }


    private Map<Item, Long> itemQuantities(Order order) {
        Map<Item, Long> quantities = new LinkedHashMap<>();

        for (Item item : order.getItems()) {
            quantities.merge(item, 1L, Long::sum);
        }

        return quantities;
    }


    private void ensureOrderNotification(Order order) {
        TitledPane pane = orderNotifications.get(order);

        if (pane == null) {
            pane = new TitledPane();
            pane.setExpanded(false);
            pane.setAnimated(true);

            pane.expandedProperty().addListener(
                    (observable, wasExpanded, isExpanded) -> {
                        if (isExpanded) {
                            unreadOrders.remove(order);
                            updateNotificationCount();
                        }
                    }
            );

            orderNotifications.put(order, pane);
            unreadOrders.add(order);


            if (orderNotifications.size() == 1) {
                notificationsBox.getChildren().clear();
            }


            notificationsBox.getChildren().add(0, pane);
        }

        pane.setText(
                "🔔  Order #"
                        + order.getOrderID()
                        + "  •  "
                        + order.getStatus()
                        + "  •  "
                        + order.getCreatedAt()
                        + "   —   Click to view receipt"
        );

        TextArea receiptArea =
                new TextArea(buildReceipt(order));

        receiptArea.setEditable(false);
        receiptArea.setWrapText(true);
        receiptArea.setPrefRowCount(12);

        Button copyButton = new Button("Copy receipt");
        copyButton.getStyleClass().add("accent-button");

        copyButton.setOnAction(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(buildReceipt(order));

            Clipboard.getSystemClipboard()
                    .setContent(content);
        });

        VBox content = new VBox(8, receiptArea, copyButton);
        content.setFillWidth(true);
        content.setStyle("-fx-padding: 12;");

        pane.setContent(content);

        updateNotificationCount();
    }


    private void updateNotificationCount() {
        if (notificationCountLabel == null) {
            return;
        }

        boolean hasUnread = !unreadOrders.isEmpty();

        notificationCountLabel.setText(
                hasUnread
                        ? String.valueOf(unreadOrders.size())
                        : ""
        );

        notificationCountLabel.setVisible(hasUnread);
        notificationCountLabel.setManaged(hasUnread);
    }


    private void refreshCatalogStock() {
        catalogTable.refresh();

        Item selected =
                catalogTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            showItemDetails(selected);
        }
    }


    private void filterCatalog(String query) {
        String search = query == null
                ? ""
                : query.trim().toLowerCase(Locale.ROOT);

        List<Item> filtered =
                AppServices.BOOKSTORE.getCatalog()
                        .stream()
                        .filter(item ->
                                item.getTitle()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(search)
                                        || item.getAuthor()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(search)
                                        || item.getType()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(search)
                                        || item.getDescription()
                                        .toLowerCase(Locale.ROOT)
                                        .contains(search)
                        )
                        .toList();

        catalogTable.setItems(
                FXCollections.observableArrayList(filtered)
        );
    }


    private void showItemDetails(Item item) {
        if (itemDetailsLabel == null) {
            return;
        }

        if (item == null) {
            itemDetailsLabel.setText(
                    "Select a title to see its description."
            );
        } else {
            itemDetailsLabel.setText(
                    item.getDescription()
                            + "   •   "
                            + item.getType()
                            + "   •   by "
                            + item.getAuthor()
                            + "   •   Available: "
                            + item.getStock()
            );
        }
    }


    private void refreshCart() {
        if (cart == null || cartList == null) {
            return;
        }

        List<String> rows = new ArrayList<>();

        for (Map.Entry<Item, Long> entry :
                itemQuantities(cart).entrySet()) {

            Item item = entry.getKey();
            long quantity = entry.getValue();

            String quantityText =
                    quantity > 1
                            ? "  " + quantity + "x"
                            : "";

            String priceText =
                    "   ·   ₱"
                            + String.format(
                            Locale.US,
                            "%.2f",
                            item.getPrice() * quantity
                    );

            rows.add(
                    item.getTitle()
                            + quantityText
                            + priceText
            );
        }

        cartList.setItems(
                FXCollections.observableArrayList(rows)
        );

        cartTotalLabel.setText(
                "₱"
                        + String.format(
                        Locale.US,
                        "%.2f",
                        cart.calculateTotal()
                )
        );
    }


    @Override
    public void onOrderStatusChanged(
            Order order,
            String oldStatus,
            String newStatus
    ) {
        ensureOrderNotification(order);
    }


    private void alert(String message) {
        Alert dialog =
                new Alert(Alert.AlertType.INFORMATION);

        dialog.setTitle("Online Bookstore");
        dialog.setHeaderText(null);

        dialog.setContentText(
                message == null
                        ? "Something went wrong."
                        : message
        );

        dialog.showAndWait();
    }
}