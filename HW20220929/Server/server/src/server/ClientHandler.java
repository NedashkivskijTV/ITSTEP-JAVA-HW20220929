package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler implements Runnable {
    // екземпляр сервера
    private Server server;

    // вихідне повідомлення
    private PrintWriter outMessage;

    // вхідне повідомлення
    private Scanner inMessage;

    // вузел та порт для відправки повідомлень
    private static final String HOST = "localhost";
    private static final int PORT = 3443;

    // сокет клієнта
    private Socket clientSocket = null;

    // кількість клієнтів у чаті (статичне поле)
    private static int clients_count = 0;

    // конструктор,  що приймає сокет клієнта та сервер
    public ClientHandler(Socket socket, Server server) {
        try {
            clients_count++;
            this.server = server;
            this.clientSocket = socket;
            this.outMessage = new PrintWriter(socket.getOutputStream());
            this.inMessage = new Scanner(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Перевизначення методу run(), що викликається при створенні нового потоку (new Thread(client).start();)
    @Override
    public void run() {
        try {
            while (true) {
                // сервер відправляє повідомлення
                server.sendMessageToOllClients("Новий учасник увійшов до чату!");
                server.sendMessageToOllClients("Учасників у чаті - " + clients_count);
                break;
            }

            while (true) {
                // якщо від клієнта прийшло повідомлення
                if (inMessage.hasNext()) {
                    String clientMessage = inMessage.nextLine();

                    // ##session##end## - якщо клієнт відправив дане повідомлення - цикл переривається, та клієнт виходить з чату
                    //if (clientMessage.equalsIgnoreCase("##session##end##")) {
                    if (clientMessage.contains("##session##end##")) {
                        String clientName = clientMessage.substring("##session##end##".length());
                        server.removeClientName(clientName);
                        break;
                    }

                    // додавання імені нового клієнта до списку імен після отримання послідовності символів ##client##name##
                    if (clientMessage.contains("##client##name##")) {
                        String newName = clientMessage.substring("##client##name##".length());
                        server.addNewClientName(newName);
                        server.sendMessageToOllClients(server.getClientsNamesString());
                    } else {
                        // виведення повідомлення в консоль сервера - для тестування
                        System.out.println(clientMessage);

                        // відправлення повідомлення усім клієнтам
                        server.sendMessageToOllClients(clientMessage);
                    }
                }
                // пауза в роботі потоку на 100 мс
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }

    // відправлення повідомлення
    public void sendMsg(String msg) {
        try {
            outMessage.println(msg);
            outMessage.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // вихід клієнта з чату - не працює коли робота клієнта завершується не натисканням Х на вікні !!!
    public void close() {
        // видалення клієнта зі списку
        server.removeClient(this);
        clients_count--;
        server.sendMessageToOllClients("Учасників у чаті - " + clients_count);
        server.sendMessageToOllClients(server.getClientsNamesString());
    }
}
