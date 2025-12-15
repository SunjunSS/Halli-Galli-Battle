package networkPJ;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;

// 네트워크 카드 게임의 서버 메인 클래스
public class NetworkCardGameServer extends JFrame {
    // 서버 관련 변수
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private String[] positions = {"topLeft", "topRight", "bottomLeft", "bottomRight"}; // 플레이어 위치
    private int currentTurn = 0; // 현재 턴
    
    // GUI 컴포넌트
    private JTextArea t_display;
    private JButton b_start;
    
    // 서버 상태 변수
    private boolean isServerRunning = false;
    private Thread serverThread;
    private String ipAddress;
    private int port;

    // 게임 상태 관리용 맵
    private Map<String, Integer> clientScores = new HashMap<>(); // 클라이언트 점수
    private Map<String, ImageIcon> currentCardPositions = new HashMap<>(); // 현재 카드 위치
    private Map<String, List<CardInfo>> currentFlippedCards = new HashMap<>(); // 뒤집힌 카드 정보

    // 카드 정보를 저장하는 내부 클래스
    private class CardInfo {
        String fruit;
        int value;
        ImageIcon image;

        CardInfo(String fruit, int value, ImageIcon image) {
            this.fruit = fruit;
            this.value = value;
            this.image = image;
        }
    }

    // 생성자
    public NetworkCardGameServer() {
        super("네트워크 카드 게임 서버");
        clients = new ArrayList<>();
        readServerInfo();     // 서버 정보 읽기
        buildGUI();          // GUI 구성
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    // 서버 정보 파일 읽기
    private void readServerInfo() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("server_info.txt")) {
            if (inputStream == null) {
                throw new IOException("server_info.txt 파일을 클래스패스에서 찾을 수 없습니다.");
            }

            List<String> lines = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.toList());

            if (lines.size() >= 2) {
                ipAddress = lines.get(0).trim();
                port = Integer.parseInt(lines.get(1).trim());
            } else {
                throw new IOException("server_info.txt 파일의 형식이 잘못되었습니다.");
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 정보를 읽는 데 실패했습니다: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    // GUI 구성
    private void buildGUI() {
        add(createDisplayPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    // 디스플레이 패널 생성
    private JPanel createDisplayPanel() {
        JPanel p = new JPanel(new BorderLayout());
        t_display = new JTextArea();
        t_display.setEditable(false);
        p.add(new JScrollPane(t_display), BorderLayout.CENTER);
        return p;
    }

    // 컨트롤 패널 생성
    private JPanel createControlPanel() {
        JPanel p = new JPanel(new BorderLayout());

        b_start = new JButton("서버 시작");
        JButton b_exit = new JButton("종료");

        // 서버 시작/중지 버튼 이벤트 처리
        b_start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isServerRunning) {
                    startServer();
                    b_start.setText("서버 중지");
                    isServerRunning = true;
                } else {
                    stopServer();
                    b_start.setText("서버 시작");
                    isServerRunning = false;
                }
            }
        });

        // 종료 버튼 이벤트 처리
        b_exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
                System.exit(0);
            }
        });

        p.add(b_start, BorderLayout.WEST);
        p.add(b_exit, BorderLayout.EAST);

        return p;
    }

    // 디스플레이에 메시지 출력
    private void printDisplay(String msg) {
        t_display.append(msg + "\n");
        t_display.setCaretPosition(t_display.getDocument().getLength());
    }

    // 서버 시작
    private void startServer() {
        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    printDisplay("서버가 시작되었습니다: " + ipAddress + ":" + port);
                    
                    while (isServerRunning) {
                        Socket clientSocket = serverSocket.accept();
                        String clientAddress = clientSocket.getInetAddress().getHostAddress();
                        
                        String availablePosition = findAvailablePosition();
                        
                        if (availablePosition != null) {
                            printDisplay("클라이언트가 연결되었습니다: " + clientAddress);
                            
                            ClientHandler handler = new ClientHandler(clientSocket, availablePosition);
                            clients.add(handler);
                            handler.start();
                        } else {
                            printDisplay("모든 자리가 차있어 클라이언트 연결을 거부합니다: " + clientAddress);
                            clientSocket.close();
                        }
                    }
                } catch (IOException e) {
                    if (isServerRunning) {
                        printDisplay("서버 오류: " + e.getMessage());
                    }
                }
            }
        });
        serverThread.start();
    }

    // 사용 가능한 위치 찾기
    private String findAvailablePosition() {
        for (String pos : positions) {
            boolean positionAvailable = clients.stream()
                .noneMatch(client -> client.getPosition().equals(pos));
            
            if (positionAvailable) {
                return pos;
            }
        }
        return null;
    }

    // 서버 중지
    private void stopServer() {
        isServerRunning = false;
        for (ClientHandler client : clients) {
            client.closeConnection();
        }
        clients.clear();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                printDisplay("서버가 중지되었습니다.");
            }
        } catch (IOException e) {
            printDisplay("서버 종료 오류: " + e.getMessage());
        }
    }

    // 모든 클라이언트에게 메시지 브로드캐스트
    private void broadcast(GameMessage message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    // 클라이언트 핸들러 내부 클래스
    private class ClientHandler extends Thread {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String clientId;
        private String position;

        // 클라이언트 핸들러 생성자
        public ClientHandler(Socket socket, String position) {
            this.socket = socket;
            this.position = position;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                printDisplay("스트림 생성 오류: " + e.getMessage());
            }
        }
        
        // 위치 반환 메소드
        public String getPosition() {
            return this.position;
        }

        // 클라이언트 스레드 실행
        public void run() {
            try {
                while (isServerRunning) {
                    GameMessage message = (GameMessage) in.readObject();
                    processMessage(message);
                }
            } catch (SocketException e) {
                // 소켓 연결 종료 시 특별한 로그 없이 종료 처리
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    printDisplay("클라이언트 통신 오류: " + e.getMessage());
                }
            } finally {
                closeConnection();
            }
        }

        // 클라이언트 점수 업데이트
        private void updateClientScore(String clientId, int score) {
            clientScores.put(clientId, score);   
        }

     // 클라이언트로부터 받은 메시지 처리
        private void processMessage(GameMessage message) {
            switch (message.getType()) {
                case GameMessage.LOGIN: // 로그인 처리
                    clientId = message.getClientId();
                    sendMessage(new GameMessage(GameMessage.POSITION, clientId, position));
                    clientScores.put(clientId, 0); // 초기 점수 설정

                    printDisplay(clientId + "님이 " + position + " 위치로 입장하셨습니다.");

                    // 기존 클라이언트들의 정보를 새로운 클라이언트에게 전송
                    for (ClientHandler existingClient : clients) {
                        if (existingClient != this) {
                            // 점수 정보 전송
                            sendMessage(new GameMessage(
                                GameMessage.SCORE_UPDATE,
                                existingClient.clientId,
                                existingClient.position,
                                clientScores.get(existingClient.clientId)
                            ));

                            // 현재 카드 상태 전송
                            if (currentCardPositions.containsKey(existingClient.position)) {
                                sendMessage(new GameMessage(
                                    GameMessage.FLIP_CARD, 
                                    existingClient.clientId, 
                                    existingClient.position, 
                                    currentCardPositions.get(existingClient.position)
                                ));
                            }
                        }
                    }

                    // 현재 턴과 새로운 플레이어 정보를 모든 클라이언트에게 브로드캐스트
                    broadcast(new GameMessage(GameMessage.TURN_UPDATE, null, positions[currentTurn]));
                    broadcast(new GameMessage(GameMessage.SCORE_UPDATE, clientId, position, 0));
                    printDisplay("현재 접속 클라이언트 수: " + clients.size());
                    broadcast(new GameMessage(GameMessage.TURN_UPDATE, null, positions[currentTurn]));
                    break;
                            
                case GameMessage.LOGOUT: // 로그아웃 처리
                    handleClientLogout(message.getClientId(), message.getPosition());
                    break;
                            
                case GameMessage.FLIP_CARD: // 카드 뒤집기 처리
                    if (position.equals(positions[currentTurn])) {
                        CardInfo cardInfo = extractCardInfo(message.getCardImageName(), message.getImage());
                        List<CardInfo> positionCards = currentFlippedCards.computeIfAbsent(position, k -> new ArrayList<>());

                        // 이전 카드가 있으면 제거
                        if (!positionCards.isEmpty()) {
                            positionCards.clear();
                        }

                        // 새로운 카드 추가
                        positionCards.add(cardInfo);
                        currentCardPositions.put(position, message.getImage());

                        String cardFileName = extractFileName(message.getCardImageName());
                        
                        // 특수 카드 처리 로그
                        if (cardFileName.contains("plus")) {
                            printDisplay(clientId + "님이 plus 카드를 뒤집었습니다.");
                        } else if (cardFileName.contains("minus")) {
                            printDisplay(clientId + "님이 minus 카드를 뒤집었습니다.");
                        } else {
                            printDisplay(clientId + "님이 " + cardFileName + " 카드를 뒤집었습니다.");
                        }

                        // 카드 상태 브로드캐스트 및 다음 턴 처리
                        broadcast(message);
                        currentTurn = (currentTurn + 1) % 4;
                               
                        // 다음 턴 플레이어 찾기
                        String nextTurnClientId = null;
                        for (ClientHandler client : clients) {
                            if (client.getPosition().equals(positions[currentTurn])) {
                                nextTurnClientId = client.clientId;
                                break;
                            }
                        }
                               
                        broadcast(new GameMessage(GameMessage.TURN_UPDATE, null, positions[currentTurn]));
                        printDisplay("다음 차례: " + nextTurnClientId + "님");
                    }
                    break;
                            
                case GameMessage.RING_BELL: // 종치기 처리
                    int scoreChange = checkBellRingingConditions(position);

                    if (scoreChange == 1) { // 유효한 종치기
                        int currentScore = clientScores.get(clientId);
                        updateClientScore(clientId, currentScore + 1);

                        // 카드 초기화
                        currentFlippedCards.clear();
                        currentCardPositions.clear();

                        broadcast(new GameMessage(GameMessage.SCORE_UPDATE, clientId, position, currentScore + 1));
                        broadcast(message);

                        printDisplay(clientId + "님이 종을 쳤습니다. 점수: " + (currentScore + 1));
                        
                        // 승리 조건(10점) 체크
                        if (currentScore + 1 >= 10) {
                            printDisplay(clientId + "님이 " + (currentScore + 1) + "점으로 게임에서 승리했습니다!");
                            broadcast(new GameMessage(GameMessage.GAME_OVER, clientId, null, currentScore + 1));
                        }
                    } else if (scoreChange == -1) { // 잘못된 종치기
                        int currentScore = clientScores.get(clientId);
                        int newScore = Math.max(0, currentScore - 1);
                        updateClientScore(clientId, newScore);

                        broadcast(new GameMessage(GameMessage.SCORE_UPDATE, clientId, position, newScore));
                        printDisplay(clientId + "님이 잘못된 종치기로 1점 감점. 현재 점수: " + newScore);
                    } 

                    broadcast(new GameMessage(GameMessage.TURN_UPDATE, null, positions[currentTurn]));
                    break;
                            
                case GameMessage.SCORE_UPDATE: // 점수 업데이트
                    updateClientScore(message.getClientId(), message.getScore());
                    break;
            }
        }
        
        // 종치기 조건 확인
        private int checkBellRingingConditions(String ringingPosition) {
            Map<String, List<CardInfo>> fruitGroups = new HashMap<>();
          
            boolean hasPlusCard = false;
            boolean hasMinusCard = false;
            
            // 특수 카드(plus/minus) 존재 여부 확인
            for (List<CardInfo> cards : currentFlippedCards.values()) {
                for (CardInfo card : cards) {
                    if (card.fruit.equals("plus")) {
                        hasPlusCard = true;
                    } else if (card.fruit.equals("minus")) {
                        hasMinusCard = true;
                    }
                }
            }
            
            // 특수 카드 조합에 따른 점수 처리
            if (hasPlusCard && hasMinusCard) {
                printDisplay("plus 카드로 인해 1점을 얻었습니다.");
                return 1;
            }
            
            if (hasPlusCard) {
                printDisplay("plus 카드로 인해 1점을 얻었습니다.");
                return 1;
            }
            
            if (hasMinusCard) {
                printDisplay("minus 카드로 인해 1점을 잃었습니다.");
                return -1;
            }

            // 일반 카드들을 과일별로 그룹화
            for (List<CardInfo> cards : currentFlippedCards.values()) {
                for (CardInfo card : cards) {
                    fruitGroups.computeIfAbsent(card.fruit, k -> new ArrayList<>()).add(card);
                }
            }
            
            // 과일별 합이 정확히 5인 경우 체크
            for (Map.Entry<String, List<CardInfo>> entry : fruitGroups.entrySet()) {
                List<CardInfo> samefruitCards = entry.getValue();
                String fruitName = samefruitCards.isEmpty() ? "Unknown" : samefruitCards.get(0).fruit;
                int totalSum = samefruitCards.stream().mapToInt(card -> card.value).sum();        

                if (totalSum == 5){
                   printDisplay("합이 5로 인해 1점을 얻었습니다.");
                   return 1;
                }
            }
            
            // 과일별 합이 5를 초과하는 경우 체크
            for (List<CardInfo> samefruitCards : fruitGroups.values()) {
                int totalSum = samefruitCards.stream().mapToInt(card -> card.value).sum();
                if (totalSum > 5) {
                   printDisplay("합이 5를 초과하여 1점을 잃었습니다.");
                    return -1;
                }
            }
            
            printDisplay("합이 5 미만으로 인해 1점을 잃었습니다.");
            return -1;
        }

        // 카드 조합 생성 메소드
        private List<List<CardInfo>> generateCombinations(List<CardInfo> cards, int size) {
            List<List<CardInfo>> result = new ArrayList<>();
            generateCombinationsHelper(cards, size, 0, new ArrayList<>(), result);
            return result;
        }

        // 카드 조합 생성 보조 메소드
        private void generateCombinationsHelper(List<CardInfo> cards, int size, 
                                                int start, List<CardInfo> current, 
                                                List<List<CardInfo>> result) {
            if (current.size() == size) {
                result.add(new ArrayList<>(current));
                return;
            }

            for (int i = start; i < cards.size(); i++) {
                current.add(cards.get(i));
                generateCombinationsHelper(cards, size, i + 1, current, result);
                current.remove(current.size() - 1);
            }
        }

        // 카드 이미지 정보 추출
        private CardInfo extractCardInfo(String cardImageName, ImageIcon image) {
            String fileName = extractFileName(cardImageName).toLowerCase();
            
            String fruit = "";
            int value = 0;

            // 특수 카드(plus/minus) 처리
            if (fileName.contains("plus")) {
                fruit = "plus";
                value = 0;
            } else if (fileName.contains("minus")) {
                fruit = "minus";
                value = 0;
            } else {
                // 일반 과일 카드 처리
                String[] fruits = {"apple", "grape", "orange", "lime", "banana"};
                for (String f : fruits) {
                    if (fileName.contains(f)) {
                        fruit = f;
                        break;
                    }
                }

                // 카드 숫자 값 추출
                for (int i = 1; i <= 5; i++) {
                    if (fileName.contains(String.valueOf(i))) {
                        value = i;
                        break;
                    }
                }
            }

            return new CardInfo(fruit, value, image);
        }

        // 모든 플레이어의 카드 초기화
        private void resetAllPlayersCards() {
            currentFlippedCards.clear();
            currentCardPositions.clear();
            
            for (ClientHandler client : clients) {
                broadcast(new GameMessage(GameMessage.TURN_UPDATE, null, positions[currentTurn]));
            }
        }
        
        // 파일 이름 추출
        private String extractFileName(String fullPath) {
            if (fullPath == null) return "unknown";
            
            try {
                fullPath = java.net.URLDecoder.decode(fullPath, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // 디코딩 실패 시 원래 경로 유지
            }
            
            // 경로에서 파일명만 추출
            int lastSlashIndex = fullPath.lastIndexOf('/');
            int lastBackslashIndex = fullPath.lastIndexOf('\\');
            int startIndex = Math.max(lastSlashIndex, lastBackslashIndex) + 1;
            String fileName = fullPath.substring(startIndex);
            
            // 확장자 제거
            int lastDotIndex = fileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return fileName.substring(0, lastDotIndex);
            }
            
            return fileName;
        }
        
        // 클라이언트 로그아웃 처리
        private void handleClientLogout(String logoutClientId, String logoutPosition) {
            clients.removeIf(client -> client.clientId.equals(logoutClientId));
            clientScores.remove(logoutClientId);
            broadcast(new GameMessage(GameMessage.SCORE_UPDATE, logoutClientId, logoutPosition, -1));
            printDisplay(logoutClientId + "님이 게임에서 나갔습니다.");
            adjustTurnAfterLogout(logoutPosition);
            printDisplay("현재 접속 클라이언트 수: " + clients.size());
        }

        // 로그아웃 후 턴 조정
        private void adjustTurnAfterLogout(String logoutPosition) {
            for (int i = 0; i < positions.length; i++) {
                if (positions[i].equals(logoutPosition)) {
                    if (currentTurn == i) {
                        currentTurn = (currentTurn + 1) % 4;
                    }
                    break;
                }
            }
            
            if (!clients.isEmpty()) {
                broadcast(new GameMessage(GameMessage.TURN_UPDATE, null, positions[currentTurn]));
            }
        }

        // 전체 점수 브로드캐스트
        private void broadcastScores() {
            for (Map.Entry<String, Integer> entry : clientScores.entrySet()) {
                broadcast(new GameMessage(GameMessage.SCORE_UPDATE, entry.getKey(), null, entry.getValue()));
            }
        }

        // 메시지 전송
        public void sendMessage(GameMessage message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                printDisplay("메시지 전송 오류: " + e.getMessage());
            }
        }

        // 연결 종료
        public void closeConnection() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                printDisplay("연결 종료 오류: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new NetworkCardGameServer();
    }
}

