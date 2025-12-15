package networkPJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class NetworkCardGameClient extends CardGameGUI {
    // 네트워크 소켓 및 통신 관련 변수들
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientId; // 클라이언트 고유 식별자
    private String position; // 클라이언트의 게임 내 위치
    private boolean isMyTurn = false; // 현재 차례인지 확인하는 플래그

    // 클라이언트 점수 추적을 위한 맵
    private Map<String, Integer> clientScores = new HashMap<>();
    private JPanel scorePanel;
    private Map<String, JLabel> scoreLabels = new HashMap<>();
    
    // 접속 종료 버튼
    protected JButton disconnectButton = new JButton("접속 종료");

    // 네트워크 클라이언트 생성자
    // IP, 포트, 사용자명을 받아 초기화
    public NetworkCardGameClient(String ip, String port, String username) {
        super();
        this.clientId = username;

        // 점수 패널 생성
        createScorePanel();

        // 네트워크 설정 (IP와 포트로 서버 연결)
        setupNetworking(ip, Integer.parseInt(port));

        // 버튼 이벤트 설정
        setupButtonEvents();
        
        // 접속 종료 버튼 설정
        setupDisconnectButton();
    }
    
    // 접속 종료 버튼 설정 메서드
    private void setupDisconnectButton() {
        disconnectButton.setPreferredSize(new Dimension(150, 40));
        
        // 기존 버튼 패널에 접속 종료 버튼 추가
        Component[] components = getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel && ((JPanel) comp).getComponentCount() > 0 
                && ((JPanel) comp).getComponent(0) instanceof JButton) {
                JPanel buttonPanel = (JPanel) comp;
                buttonPanel.add(disconnectButton);
                break;
            }
        }

        // 접속 종료 버튼 클릭 이벤트 설정
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 접속 종료 확인 다이얼로그
                int response = JOptionPane.showConfirmDialog(
                    NetworkCardGameClient.this, 
                    "정말 접속을 종료하시겠습니까?", 
                    "접속 종료", 
                    JOptionPane.YES_NO_OPTION
                );
                
                // 사용자가 예를 선택한 경우
                if (response == JOptionPane.YES_OPTION) {
                    // 로그아웃 메시지 전송
                    sendMessage(new GameMessage(GameMessage.LOGOUT, clientId, position));
                    
                    // 현재 창 위치 저장
                    Point location = getLocation();
                    
                    // 현재 창 닫기
                    dispose();
                    
                    // 네트워크 연결 종료
                    closeConnection();
                    
                    // 로그인 화면으로 돌아가기
                    CardGameLogin loginScreen = CardGameLogin.getInstance();  
                    loginScreen.setLocation(location);
                    loginScreen.setVisible(true);
                }
            }
        });
    }

    // 네트워크 설정 메서드
    private void setupNetworking(String serverAddress, int serverPort) {
        try {
            // 서버에 소켓 연결
            socket = new Socket(serverAddress, serverPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // 초기 카드 설정 (뒷면)
            setAllRandomCards(backCard);
            setAllBackCards(backCard);
            
            // 로그인 메시지 전송
            sendMessage(new GameMessage(GameMessage.LOGIN, clientId, null));

            // 메시지 수신 스레드 시작
            new Thread(new IncomingReader()).start();

            // 초기 버튼 상태 설정
            ringBellButton.setEnabled(true);
            flipButton.setEnabled(false);

        } catch (IOException e) {
            // 서버 연결 실패 시 처리
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + e.getMessage());
            
            // 로그인 화면으로 돌아가기
            CardGameLogin loginScreen = CardGameLogin.getInstance();
            loginScreen.setVisible(true);
        }
    }

    // 버튼 이벤트 설정 메서드
    private void setupButtonEvents() {
        // 카드 뒤집기 버튼 이벤트 재설정
        flipButton.removeActionListener(flipButton.getActionListeners()[0]);
        flipButton.addActionListener(e -> {
            // 자신의 차례일 때만 카드 뒤집기 가능
            if (isMyTurn) {
                // 랜덤 카드 뒤집기
                ImageIcon newImage = getRandomImageExcludingBackCard();
                String cardImageName = getImageName(newImage);
                // 카드 뒤집기 메시지 전송
                sendMessage(new GameMessage(GameMessage.FLIP_CARD, clientId, position, newImage, cardImageName));
            } else {
                JOptionPane.showMessageDialog(null, "아직 당신의 차례가 아닙니다!");
            }
        });

        // 종 누르기 버튼 이벤트 재설정
        ringBellButton.removeActionListener(ringBellButton.getActionListeners()[0]);
        ringBellButton.addActionListener(e -> {
            // 종 누르기 메시지 전송
            sendMessage(new GameMessage(GameMessage.RING_BELL, clientId, position));
        });
    }

    // 점수 패널 생성 메서드
    private void createScorePanel() {
        // 점수 표시 패널 생성 (1행 4열)
        scorePanel = new JPanel(new GridLayout(1, 4));
        scorePanel.setBorder(BorderFactory.createTitledBorder("플레이어 점수"));
        
        // 각 위치별 점수 레이블 생성
        for (String position : new String[]{"topLeft", "topRight", "bottomLeft", "bottomRight"}) {
            JLabel scoreLabel = new JLabel(position + ": 대기 중", SwingConstants.CENTER);
            scoreLabels.put(position, scoreLabel);
            scorePanel.add(scoreLabel);
        }

        // 점수 패널을 프레임 상단에 추가
        getContentPane().add(scorePanel, BorderLayout.NORTH);
    }
    
    // 점수 업데이트 메서드
    private void updateScore(String clientId, String position, int score) {
        SwingUtilities.invokeLater(() -> {
            // 해당 위치의 점수 레이블 업데이트
            if (position != null) {
                JLabel scoreLabel = scoreLabels.get(position);
                if (scoreLabel != null) {
                    scoreLabel.setText(clientId + ": " + score + "점");
                }
            }
            
            // 클라이언트 점수 맵 업데이트
            clientScores.put(clientId, score);
        });
    }

    // 메시지 전송 메서드
    private void sendMessage(GameMessage message) {
        try {
            // 메시지 직렬화하여 서버로 전송
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "메시지 전송 실패: " + e.getMessage());
        }
    }

    // 메시지 수신 스레드
    private class IncomingReader implements Runnable {
        public void run() {
            try {
                // 지속적으로 서버로부터 메시지 수신
                while (true) {
                    GameMessage message = (GameMessage) in.readObject();
                    processMessage(message);
                }
            } catch (Exception e) {
                // 연결 종료 시 로그인 화면으로 돌아가기
                SwingUtilities.invokeLater(() -> {
                    Point location = getLocation();
                    
                    dispose();
                    
                    closeConnection();
                    
                    CardGameLogin loginScreen = CardGameLogin.getInstance();  
                    loginScreen.setLocation(location);
                    loginScreen.setVisible(true);
                });
            }
        }
    }

    // 수신된 메시지 처리 메서드
    private void processMessage(GameMessage message) {
        switch (message.getType()) {
            case GameMessage.POSITION:
                // 클라이언트 위치 설정
                position = message.getPosition();
                JLabel positionLabel = scoreLabels.get(position);
                if (positionLabel != null) {
                    positionLabel.setText(clientId + ": 0점");
                }
                // 프레임 타이틀 업데이트
                setTitle("카드 게임 클라이언트 - " + clientId + " (" + position + ")");
                break;

            case GameMessage.TURN_UPDATE:
                // 차례 업데이트
                isMyTurn = message.getPosition().equals(position);
                flipButton.setEnabled(isMyTurn);
                // 타이틀에 현재 차례 표시
                if (isMyTurn) {
                    setTitle("카드 게임 클라이언트 - " + clientId + " (" + position + ") - 당신의 차례입니다!");
                } else {
                    setTitle("카드 게임 클라이언트 - " + clientId + " (" + position + ")");
                }
                break;

            case GameMessage.FLIP_CARD:
                // 카드 이미지 업데이트
                updateCardImage(message.getPosition(), message.getImage());
                break;

            case GameMessage.RING_BELL:
                // 종 울리기 처리
                handleBellRing();
                break;

            case GameMessage.SCORE_UPDATE:
                // 점수 업데이트
                updateScore(message.getClientId(), message.getPosition(), message.getScore());
                break;

            case GameMessage.GAME_OVER:
                // 게임 종료 처리
                handleGameOver(message.getClientId(), message.getScore());
                break;
        }
    }

    // 게임 종료 처리 메서드
    private void handleGameOver(String winnerClientId, int winnerScore) {
        SwingUtilities.invokeLater(() -> {
            // 모든 버튼 비활성화
            flipButton.setEnabled(false);
            ringBellButton.setEnabled(false);
            
            // 승자 메시지 표시
            JOptionPane.showMessageDialog(
                this, 
                winnerClientId + "님이 " + winnerScore + "점으로 게임에서 승리했습니다!", 
                "게임 종료", 
                JOptionPane.INFORMATION_MESSAGE
            );
            
            // 로그인 화면으로 돌아가기
            Point location = getLocation();
            dispose();
            closeConnection();
            
            CardGameLogin loginScreen = CardGameLogin.getInstance();  
            loginScreen.setLocation(location);
            loginScreen.setVisible(true);
        });
    }

    // 카드 이미지 업데이트 메서드
    private void updateCardImage(String position, ImageIcon image) {
        SwingUtilities.invokeLater(() -> {
            // 각 위치의 카드 이미지 변경
            switch (position) {
                case "topLeft":
                    topLeftRandomCard.setIcon(image);
                    break;
                case "topRight":
                    topRightRandomCard.setIcon(image);
                    break;
                case "bottomLeft":
                    bottomLeftRandomCard.setIcon(image);
                    break;
                case "bottomRight":
                    bottomRightRandomCard.setIcon(image);
                    break;
            }
        });
    }

    // 종 울리기 처리 메서드
    private void handleBellRing() {
        SwingUtilities.invokeLater(() -> {
            // 종 이미지 변경
            bellLabel.setIcon(bell[1]);

            // 1초 후 종 이미지와 카드 초기화
            Timer timer = new Timer(1000, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    bellLabel.setIcon(bell[0]);
                    setAllRandomCards(backCard);
                    setAllBackCards(backCard);
                    usedIndexes.clear();
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    // 네트워크 연결 종료 메서드
    private void closeConnection() {
        try {
            // 입력 스트림 닫기
            if (in != null) {
                in.close();
                in = null;
            }
            // 출력 스트림 닫기
            if (out != null) {
                out.close();
                out = null;
            }
            // 소켓 닫기
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}