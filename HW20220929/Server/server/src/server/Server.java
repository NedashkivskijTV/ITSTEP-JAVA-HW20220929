package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    // порт, що буде прослуховуватись сервером
    static final int PORT = 3443;

    // список клієнтів, які будуть підключатись до сервера
    private ArrayList<ClientHandler> clients = new ArrayList<>();

    // список імен клієнтів, підключених до сервера
    private ArrayList<String> clientsNames = new ArrayList<>();

    // сокет клієнта - поток, що підключатиметься до сервера по адресі(хосту) та порту
    Socket clientSocket = null;

    // сокет сервера
    ServerSocket serverSocket = null;

    // конструктор
    public Server() {
        // створення серверного сокета по визначеному порту
        serverCreation();
    }

    // створення серверного сокета по визначеному порту
    public void serverCreation() {
        try {
            // створення серверного сокета по визначеному порту
            serverSocket = new ServerSocket(PORT);
            System.out.println("Сервер запущено!");

            // запуск нескінченного циклу - очікуватиме підключення клієнта
            while (true) {
                // очікування підключень від сервера
                clientSocket = serverSocket.accept();

                // створення обробника для клієнта, що підключиться до сервера (у параметрах this - наш сервер)
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);

                // кожне підключення клієнта обробляється у новому потоці
                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // процедура закриття (припинення роботи) сервера
                clientSocket.close();
                System.out.println("Сервер зупинено");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // відправлення повідомлення усім клієнтам
    public void sendMessageToOllClients(String message) {
        for (ClientHandler client : clients) {
            client.sendMsg(message);
        }
    }

    // видалення клієнта з колекції при виході з чату
    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    // додавання імені нового клієнта до списку
    public void addNewClientName(String clientName) {
        clientsNames.add(clientName);

        // тестовий рядок для сервера
        getClientsNamesText();
    }

    // видалення імені клієнта зі списку
    public void removeClientName(String clientName) {
        clientsNames.remove(clientName);

        // тестовий рядок для сервера
        getClientsNamesText();
    }

    // список імен користувачів, що знаходяться у чаті -
    // формування рядка з html тегами для коректного відображення у відповідній JPanel клієнта
    // (вертикальний список з заголовком)
    public String getClientsNamesString() {
        //String temp = "Список учасників чату : \n";
        String[] namesString = {"<html>Список учасників чату :"};
        for (String clientsName : clientsNames) {
            namesString[0] = String.join("<br> ", namesString[0], clientsName);
        }
        namesString[0] = String.join(" ", namesString[0], "<html>");
        return namesString[0];
    }

    // тестове повідомлення в консоль сервера - список підключених клієнтів в рідок
    public void getClientsNamesText() {
        System.out.print("Список учасників чату: ");
        System.out.println(String.join(" ", clientsNames));
    }
}
