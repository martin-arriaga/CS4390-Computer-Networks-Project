import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;


class LogClass {
    private static final Logger serverlogs = Logger.getLogger("serverlogger");
    private static final Logger userlogs = Logger.getLogger("userlogger");
    static {
        try{
        FileHandler serverlogHandler = new FileHandler("serverlogs.log", true);
        serverlogHandler.setFormatter(new SimpleFormatter());
        serverlogs.addHandler(serverlogHandler);
        serverlogs.setLevel(Level.INFO);

        FileHandler userlogHandler = new FileHandler("userlogs.log", true);
        userlogHandler.setFormatter(new SimpleFormatter());
        userlogs.addHandler(userlogHandler);
        userlogs.setLevel(Level.INFO);

        }catch(IOException exp){
        exp.printStackTrace();
        }
    }
    public static Logger getserverlogs(){
        return serverlogs;
    }
    public static Logger getuserlogs(){
        return userlogs;
    }

    public static void loguserinfo(UserInfo user){
        Logger logtofile = getuserlogs();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");
        String timeofconnection = format.format(user.getTimeofconnection());
        String timeofdisconnected = format.format(user.getTimedisconnected());
        Duration uptime = Duration.between(user.getTimeofconnection(), user.getTimedisconnected());
        StringBuilder fullstring = new StringBuilder();
        fullstring.append("\n").append("--------------").append("\n")
                .append("User ID: " + user.getUserID()).append("\n")
                        .append("IP Address: "+ user.getIPAddress()).append("\n")
                                .append("Port Number: " + user.getPortNumber()).append("\n")
                                        .append("Queries Made: " + user.getQueries()).append("\n")
                                                .append("Time of Connection: "+ timeofconnection).append("\n")
                                                        .append("Time Disconnected: " + timeofdisconnected).append("\n")
                                                                .append("Up time: "+ uptime).append("\n")
                                                                        .append("--------------");
        logtofile.info(fullstring.toString());

    }
}
public class Server {
    // test
    private static final int PORT = 12345; //New server with port #
    private static final List<ClientSession> clients = Collections.synchronizedList(new ArrayList<>()); // List of client sessions
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Math Server started on port " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    public static synchronized void addClient(ClientSession session) {
        clients.add(session);
    }

    // Basically just removes a user when they disconnect the session
    public static synchronized void removeClient(String name) {
        clients.removeIf(client -> client.name.equals(name));
    }

    public static void printActiveClients() {
        System.out.println("---- ACTIVE CLIENTS ----");
        for (ClientSession session : clients) {
            long duration = (System.currentTimeMillis() - session.connectTime) / 1000;
            System.out.println("Name: " + session.name + " | Connected for: " + duration + " seconds");
        }
        System.out.println("------------------------");
    }

    static class ClientSession {
        String name;
        Socket socket;
        long connectTime;

        ClientSession(String name, Socket socket, long connectTime) {
            this.name = name;
            this.socket = socket;
            this.connectTime = connectTime;
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientName;
    private BufferedReader in;
    private PrintWriter out;
    private long connectTime;


    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        UserInfo user = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            clientName = in.readLine();
            connectTime = System.currentTimeMillis();
            Server.addClient(new Server.ClientSession(clientName, clientSocket, connectTime));
            log("CONNECTED");
            out.println("ACK: Connected to Math Server as " + clientName);
            InetAddress clientIP = clientSocket.getInetAddress();
            String IP = clientIP.getHostAddress();
            LocalDateTime currenttime = LocalDateTime.now();

            user = new UserInfo(IP, clientSocket.getPort(), currenttime, clientName);

            String input;
            while ((input = in.readLine()) != null) {
                if (input.equalsIgnoreCase("exit")) {
                    log("DISCONNECTED");
                    break;
                }
                try {
                    double result = evaluate(input);
                    out.println("Result: " + result);
                    log("REQUEST: " + input);
                    user.addquerycount();
                } catch (Exception e) {
                    out.println("Invalid input. Try again.");
                    log("INVALID REQUEST: " + input);
                }
            }
        } catch (IOException e) {
            log("ERROR: Connection reset or IO issue");
        } finally {
            try {
                if (clientName != null && user !=null ) {
                    Server.removeClient(clientName);
                    user.setdissconectiontime();
                    LogClass.loguserinfo(user);
                    log("SESSION CLOSED. Duration: " + getDuration() + " seconds");
                }
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(String msg) {
        String timestamp = Server.sdf.format(new Date());
        System.out.println("[" + timestamp + "] [" + clientName + "] " + msg);
    }

    private long getDuration() {
        return (System.currentTimeMillis() - connectTime) / 1000;
    }

    // Takes in a string expression and evaluates the expression. It also recognizes of a string has spaces, and if it does gets rid of the spaces then finds the operation.
    private double evaluate(String expression) throws Exception {
        expression = expression.replaceAll("\\s+", "");
        String[] tokens;
        String operator;

        if (expression.contains("+")) {
            tokens = expression.split("\\+");
            operator = "+";
        } else if (expression.contains("-")) {
            tokens = expression.split("-");
            operator = "-";
        } else if (expression.contains("*")) {
            tokens = expression.split("\\*");
            operator = "*";
        } else if (expression.contains("/")) {
            tokens = expression.split("/");
            operator = "/";
        } else {
            throw new IllegalArgumentException("Invalid operator");
        }

        if (tokens.length != 2) throw new IllegalArgumentException("Invalid format");

        double a = Double.parseDouble(tokens[0]);
        double b = Double.parseDouble(tokens[1]);

        switch (operator) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": if (b == 0) throw new ArithmeticException("Divide by zero"); return a / b;
            default: throw new IllegalArgumentException("Unknown operator");
        }
    }
}
