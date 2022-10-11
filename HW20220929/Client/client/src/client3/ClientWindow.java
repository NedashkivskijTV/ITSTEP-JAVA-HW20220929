package client3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientWindow extends JFrame {
    // адреса сервера
    private static final String SERVER_HOST = "localhost";

    // порт
    private static final int SERVER_PORT = 3443;

    // сокет клієнта
    private Socket clientSocket;

    // вхідне повідомлення
    private Scanner inMessage;

    // вихідне повідомлення
    private PrintWriter outMessage;

    // поля - елементи форми
    private JTextField jtfMessage;
    private JTextField jtfName;
    private JTextArea jtaTextAreaMessage;
    private JLabel jlNumberOfClients;
    private JLabel jlNamesOfClients;

    // ім'я клієнта
    private String clientName = "";

    // прапорець відправлення імені клієнта на сервер
    private boolean isNameSending = false;

    // геттер - отримання імені клієнта
    public String getClientName() {
        return clientName;
    }

    // конструктор
    public ClientWindow() {
        // підключення до сервера
        serverConnection();

        // налаштування елементів форми
        formSettings();

        // у окремому потоці відбувається робота з сервером
        serverThread();
    }


    // підключення до сервера
    public void serverConnection() {
        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            inMessage = new Scanner(clientSocket.getInputStream());
            outMessage = new PrintWriter(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void formSettings() {
        // налаштування елементів форми
        setBounds(600, 300, 600, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jtaTextAreaMessage = new JTextArea();
        jtaTextAreaMessage.setEditable(false);
        jtaTextAreaMessage.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jtaTextAreaMessage);
        add(jsp, BorderLayout.CENTER);

        // label, що відображатиме кількість клієнтів у чаті
        jlNumberOfClients = new JLabel("Кількість клієнтів у чаті:");
        add(jlNumberOfClients, BorderLayout.NORTH);

        // label - імені учасників чату
        jlNamesOfClients = new JLabel("Список учасників чату:");
        jlNamesOfClients.setVerticalAlignment(SwingConstants.TOP); // положення тексту у JLabel
        //jlNamesOfClients.setPreferredSize(new Dimension(150, 500)); // розміри вікна
        add(jlNamesOfClients, BorderLayout.EAST);

        // кнопка відправлення
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSendMessage = new JButton("Відправити");
        bottomPanel.add(jbSendMessage, BorderLayout.EAST);

        // текстові поля
        jtfMessage = new JTextField("Введіть ваше повідомлення: ");
        bottomPanel.add(jtfMessage, BorderLayout.CENTER);
        jtfName = new JTextField("Введіть ваше ім'я: ");
        bottomPanel.add(jtfName, BorderLayout.WEST);

        // обробник події - натискання кнопки відправки повідомлення
        jbSendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageSendingWithConditions();
            }
        });

        // обробник події - при натисканні комбінації клавіш ctrl+S відбувається відправка повідомлення
        KeyStroke keyStroke = KeyStroke.getKeyStroke("ctrl S"); // встанволення комбінації клавіш по якому спрацює подія
        InputMap inputMap = jtfMessage.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW); // встановлення випадку спрацювання події - фокус на вікні
        String key = "send message"; // вибір унікального ключа - зазвичай називається по дії, що відбуватиметься
        inputMap.put(keyStroke, key); // вказується, що дана робота виконуватиметься по такій комбінації клавіш
        ActionMap actionMap = jtfMessage.getActionMap(); // отримання об'єкта ActionMap - є у кожної jPanel...
        actionMap.put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageSendingWithConditions();
            }
        });

        // при фокусі поле повідомлення буде очищено
        jtfMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                jtfMessage.setText("");
            }
        });

        // при фокусі поле ім'я буде очищено
        jtfName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                //jtfName.setText("");
                jtfName.setText(clientName);
            }
        });

        // обробник події - закриття вікна клієнтського застосунку
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    // перевірка чи ім'я клієнта непусте та не дорівнює значенню за замовчуванням
                    //if (!clientName.isEmpty() && clientName != "Введіть ваше ім'я: ") {
                    if (!clientName.isEmpty() && !clientName.equals("Введіть ваше ім'я: ")) {
                        outMessage.println(clientName + " вийшов з чату!");
                    } else {
                        outMessage.println("Участник вийшов з чату, не представившись!");
                    }

                    // відправка службового повідомлення, яке є ознакою того, що клієнт вийшов з чату
                    //outMessage.println("##session##end##");
                    outMessage.println("##session##end##" + clientName);
                    outMessage.flush();
                    outMessage.close();
                    inMessage.close();
                    clientSocket.close();
                } catch (IOException exc) {
                    //exc.printStackTrace();
                }
            }
        });

        // відображення форми
        setVisible(true);
    }

    public void serverThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // нескінченний цикл
                    while (true) {
                        // якщо є вхідне повідомлення
                        if (inMessage.hasNext()) {
                            // читаємо нове повідомлення
                            String inMes = inMessage.nextLine();
                            String clientsInChat = "Учасників у чаті - ";
                            String clientsNames = "Список учасників чату :";
                            if (inMes.indexOf(clientsInChat) == 0) {
                                jlNumberOfClients.setText(inMes);
                            } else if (inMes.contains(clientsNames)) {
                                // при отриманні повідомлення, що містить список імен клієнтів
                                //System.out.println("список отримано");
                                //System.out.println(inMes);
                                jlNamesOfClients.setText(inMes);
                            } else {
                                // виводимо повідомлення
                                jtaTextAreaMessage.append(inMes);
                                // додаємо рядок переходу
                                jtaTextAreaMessage.append("\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }).start();
    }

    // метод, що містить алгоритм попередніх переврок
    // та команду на відправку повідомлення
    public void messageSendingWithConditions() {
        // якщо поля ім'я клієнта та повідомлення не пусті - відправляється повідомлення
        if (!jtfMessage.getText().trim().isEmpty() && !jtfName.getText().trim().isEmpty()) {
            clientName = jtfName.getText();
            sendMsg();
            // фокус на текстове поле з повідомленням
            jtfMessage.grabFocus();
        }
    }

    // відправлення повідомлення
    public void sendMsg() {
        // формування повідомлення для відправлення на сервер
        String messageStr = jtfName.getText() + ": " + jtfMessage.getText();

        // відправлення на сервер імені клієнта
        if (!isNameSending) {
            outMessage.println("##client##name##" + clientName);
            outMessage.flush();
            jtfName.setEditable(false); // після відправки імені користувача на сервер поле стає незмінним
            isNameSending = true;
        }

        // відправлення повідомлення
        outMessage.println(messageStr);
        outMessage.flush();
        jtfMessage.setText("");
    }

}
