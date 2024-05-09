package com.distalgo.client.gui;

//import com.distalgo.client.controller.ClientController;
import com.distalgo.client.service.ClientPublisherService;
import com.distalgo.saga.events.InventoryStatus;
import com.distalgo.saga.events.OrderStatus;
import com.distalgo.saga.events.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import static java.lang.String.format;


// TODO: assume the cost of all events are the same -- for simplicity
public class ClientGUI {
    private String sessionID;
    private String clientIP;
    private String clientPort;
//    private OrderCallbackService orderCallbackService;

//    @Autowired
    ClientPublisherService publisherService;


    private JFrame frame;
    private JPanel contentPanel;
    private JPanel interactivePanel;
    private JPanel inputPanel;
    private JPanel buttonPanel;
    private JPanel outputPanel;
    private JLabel userIDLabel;
    private JLabel eventIDLabel;
    private JLabel numSeatsLabel;
    private JTextField userIDInput;
    private JTextField eventIDInput;
    private JTextField numSeatsInput;
    private JButton confirmButton;
    private JScrollPane outputWrapper;
    private JTextArea outputBox;

    public ClientGUI(String sessionID, ClientPublisherService publisherService) {
        this.sessionID = sessionID;
        this.publisherService = publisherService;

        frame = new JFrame();
        frame.setBackground(SystemColor.window);
        frame.setTitle("Ticket Reservation Window");
        frame.setSize(450, 300);

        // content panel encapsulates both interactive panel and output panel
        contentPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 2;
        c.gridheight = 2;
        contentPanel.setBackground(SystemColor.window);

        contentPanel.setLayout(gridbag);

        // make this a grid bag layout - this encapsulates the
        // input components on the L side and the confirmation button on the R side
        interactivePanel = new JPanel();
        c.gridwidth = 2;
        c.gridheight = 1;
        interactivePanel.setLayout(gridbag);
        gridbag.setConstraints(interactivePanel, c);

        addInputComponents();
        addConfirmationButton();
        addOutputArea();

        c.gridx = 1;
        gridbag.setConstraints(outputPanel, c);

        contentPanel.add(interactivePanel);
        contentPanel.add(outputPanel);
        frame.getContentPane().add(contentPanel);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void updateOutputBox(Integer orderID, OrderStatus orderStatus, InventoryStatus inventoryStatus, PaymentStatus paymentStatus) {
        showcaseOrderStatus(orderID, orderStatus, inventoryStatus, paymentStatus);
    }

    public void updateOutputBox(String update) {
        showcaseServiceUpdate(update);
    }


    private void addInputComponents() {
        inputPanel = new JPanel();
        inputPanel.setBackground(SystemColor.window);
        GridBagLayout gridbag = new GridBagLayout();

        GridBagConstraints c = new GridBagConstraints();
        inputPanel.setLayout(gridbag);

        userIDLabel = new JLabel("User ID: ");
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(userIDLabel, c);

        eventIDLabel = new JLabel("Event ID: ");
        c.gridx = 0;
        c.gridy = 1;
        gridbag.setConstraints(eventIDLabel, c);

        numSeatsLabel = new JLabel("Seats To Reserve: ");
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(numSeatsLabel, c);

        userIDInput = new JTextField();
        c.gridx = 1;
        c.gridy = 0;
        userIDInput.setPreferredSize(new Dimension(100, userIDInput.getPreferredSize().height));
        gridbag.setConstraints(userIDInput, c);

        eventIDInput = new JTextField();
        c.gridx = 1;
        c.gridy = 1;
        eventIDInput.setPreferredSize(new Dimension(100, eventIDInput.getPreferredSize().height));
        gridbag.setConstraints(eventIDInput, c);

        numSeatsInput = new JTextField();
        c.gridx = 1;
        c.gridy = 2;
        numSeatsInput.setPreferredSize(new Dimension(100, numSeatsInput.getPreferredSize().height));
        gridbag.setConstraints(numSeatsInput, c);

        inputPanel.add(userIDLabel);
        inputPanel.add(userIDInput);
        inputPanel.add(eventIDLabel);
        inputPanel.add(eventIDInput);
        inputPanel.add(numSeatsLabel);
        inputPanel.add(numSeatsInput);

        interactivePanel.add(inputPanel);
    }

    private void addConfirmationButton() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
//        ClientController clientController = new ClientController();

        buttonPanel = new JPanel();
        buttonPanel.setLayout(gridbag);

        confirmButton = new JButton("Confirm Reservation");
        c.insets = new Insets(0,20,0,0);

        gridbag.setConstraints(confirmButton, c);
        buttonPanel.add(confirmButton);
        interactivePanel.add(buttonPanel);

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Integer> input = verifyInput(); // includes all the input
                // use sessionID here/pass sessionID to the client controller
                System.out.println("input after clicking confirm: " + input);
                if (input != null) {
                    System.out.println("going to send order request");
//                    clientController.sendOrderRequest(sessionID, input, clientIP, clientPort);
                    // publish needs the clientEventDTO
                    publisherService.publishClientEvent(sessionID, input);
                    System.out.println("sent order request");
                } else {
                    System.out.println("Wrong input was entered");
                }
            }
        });
    }

    private void addOutputArea() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        outputPanel = new JPanel();
        outputPanel.setLayout(gridbag);

        outputBox = new JTextArea();
        outputBox.setEditable(false);

        outputWrapper = new JScrollPane(outputBox);
        c.ipadx = 400;
        c.ipady = 100;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(30,0,0,0);
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        gridbag.setConstraints(outputWrapper, c);
        outputPanel.add(outputWrapper, c);
    }

    /**
     * Verifies the client has input 3 fields, and that all fields entered are integers
     */
    private ArrayList<Integer> verifyInput() {
        String userID = userIDInput.getText();
        String eventID = eventIDInput.getText();
        String numSeats = numSeatsInput.getText();

        if (userID.isEmpty() || eventID.isEmpty() || numSeats.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields before confirming.");
            return null;
        }
        return checkInputType(userID, eventID, numSeats);
    }

    /**
     * Checks whether the input given is valid (i.e., is an integer) and not any other types
     * @return returns a list containing all correct input given by the client
     */
    private ArrayList<Integer> checkInputType(String user, String event, String seats) {
        ArrayList<Integer> input = new ArrayList<>();
        try {
            input.add(Integer.valueOf(user));
            input.add(Integer.valueOf(event));
            input.add(Integer.valueOf(seats));
            return input;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter valid integers for all fields.");
            return null;
        }
    }

    /**
     * Shows the order status to the client
     */
    private void showcaseOrderStatus(Integer orderID, OrderStatus orderStatus, InventoryStatus inventoryStatus,
                                     PaymentStatus paymentStatus) {
        outputBox.setText("");
        outputBox.append(format("Your order (#%d) ", orderID));

        if (orderStatus == OrderStatus.ORDER_SUCCESS) {
            outputBox.append("was successful! Enjoy the event~\n");
        } else {
            outputBox.append("has failed: ");
            showcaseInventoryStatus(inventoryStatus);
            showcasePaymentStatus(paymentStatus);
        }
    }

    /**
     * Shows the inventory status to the client
     */
    private void showcaseInventoryStatus(InventoryStatus inventoryStatus) {
        if (inventoryStatus != null) {
            switch (inventoryStatus) {
                case INVENTORY_CHECK_FAILED:
                    outputBox.append("unfortunately, there is insufficient seats for given event.\n");
                    break;
                case NO_EVENT:
                    outputBox.append("no such event exists, please verify that the correct eventID was provided.\n");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Shows the payment status to the client
     */
    private void showcasePaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus != null) {
            switch (paymentStatus) {
                case PAYMENT_FAILED:
                    outputBox.append("insufficient balance in the users' account.\n");
                    break;
                case USER_NOT_FOUND:
                    outputBox.append("no such user exists, please verify that the correct userID was used.\n");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Informs the client that at least one of the service is down
     */
    private void showcaseServiceUpdate(String string) {
        outputBox.setText("");
        outputBox.append(string);
    }
}


