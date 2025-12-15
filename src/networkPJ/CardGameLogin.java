package networkPJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 할리 갈리 배틀 게임의 로그인 화면을 구현하는 클래스
 싱글톤 패턴을 사용하여 단일 인스턴스 생성
*/
public class CardGameLogin extends JFrame {
    // 싱글톤 패턴을 위한 정적 인스턴스 변수
    private static CardGameLogin instance;
    
    // 서버 연결 정보 입력 필드
    private JTextField txtIp;
    private JTextField txtPort;
    private JTextField txtUserName;
    private JButton btnConnect;

    /*
     싱글톤 인스턴스를 반환하는 정적 메서드
     스레드 안전한 인스턴스 생성 보장
     return CardGameLogin의 단일 인스턴스
     */
    public static synchronized CardGameLogin getInstance() {
        if (instance == null) {
            instance = new CardGameLogin();
        }
        return instance;
    }

    /*
     기본 생성자 (private)
     UI 컴포넌트 초기화 및 서버 정보 로드
    */
    private CardGameLogin() {
        initializeComponents();
        loadServerInfo();
    }

    /*
     UI 컴포넌트 초기화 메서드
     로그인 화면의 레이아웃, 디자인, 컴포넌트 설정
     */
    private void initializeComponents() {
        // 프레임 기본 설정
        setTitle("로그인 화면");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 메인 패널 생성 및 레이아웃 설정
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 로고 레이블 생성
        JLabel lblLogo = new JLabel("Halli Galli Battle: Item Smash");
        lblLogo.setFont(createFont("Arial", Font.BOLD, 28));
        lblLogo.setForeground(new Color(70, 130, 180));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblLogo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 65)));

        // 입력 필드 생성 (IP, 포트, 사용자 이름)
        mainPanel.add(createInputField("서버 IP:", ""));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createInputField("포트 번호:", ""));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createInputField("사용자 이름:", ""));

        // 접속 버튼 설정
        btnConnect = new JButton("접속하기");
        btnConnect.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnConnect.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConnect.setMaximumSize(new Dimension(150, 40));
        btnConnect.setFont(createFont("SansSerif", Font.BOLD, 16));

        // 버튼 색상 및 스타일 설정
        Color defaultBackground = Color.GRAY;
        Color defaultForeground = Color.WHITE;
        Color hoverBackground = new Color(70, 130, 180);
        Color hoverForeground = Color.WHITE;

        btnConnect.setBackground(defaultBackground);
        btnConnect.setForeground(defaultForeground);
        btnConnect.setFocusPainted(false);
        btnConnect.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // 마우스 hover 효과 추가
        btnConnect.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnConnect.setBackground(hoverBackground);
                btnConnect.setForeground(hoverForeground);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnConnect.setBackground(defaultBackground);
                btnConnect.setForeground(defaultForeground);
            }
        });

        // 접속 버튼 클릭 이벤트 리스너
        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToGame();
            }
        });

        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(btnConnect);

        add(mainPanel, BorderLayout.CENTER);

        // 배경색 설정
        Color backgroundColor = new Color(240, 248, 255);
        getContentPane().setBackground(backgroundColor);
        mainPanel.setBackground(backgroundColor);

        setLocationRelativeTo(null);
    }

    /*
     입력 필드 생성 메서드
     labelText 입력 필드의 레이블 텍스트
     defaultValue 입력 필드의 기본값
     return 생성된 입력 필드 패널
     */
    private JPanel createInputField(String labelText, String defaultValue) {
        // 입력 필드 패널 생성 및 레이아웃 설정
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setMaximumSize(new Dimension(300, 30));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBackground(new Color(240, 248, 255));

        // 레이블 생성 및 스타일 설정
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(100, 25));
        label.setFont(createFont("SansSerif", Font.PLAIN, 14));
        label.setForeground(new Color(70, 130, 180));
        panel.add(label);

        // 텍스트 필드 생성 및 스타일 설정
        JTextField textField = new JTextField(defaultValue);
        textField.setFont(createFont("SansSerif", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        panel.add(textField);

        // 각 입력 필드의 참조 저장
        if (labelText.contains("IP")) txtIp = textField;
        else if (labelText.contains("포트")) txtPort = textField;
        else if (labelText.contains("사용자")) txtUserName = textField;

        return panel;
    }

    /*
     서버 정보 로드 메서드
     클래스패스의 server_info.txt 파일에서 서버 IP와 포트 정보 읽기
     */
    private void loadServerInfo() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("server_info.txt")) {
            if (inputStream == null) {
                throw new IOException("server_info.txt 파일을 클래스패스에서 찾을 수 없습니다.");
            }

            // 파일에서 IP와 포트 정보 읽기
            List<String> lines = new BufferedReader(new InputStreamReader(inputStream))
                    .lines()
                    .collect(Collectors.toList());

            if (lines.size() >= 2) {
                txtIp.setText(lines.get(0).trim());
                txtPort.setText(lines.get(1).trim());
            } else {
                throw new IOException("server_info.txt 파일의 형식이 잘못되었습니다.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버 정보를 읽는 데 실패했습니다: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
     게임 접속 메서드
     입력된 정보로 게임 클라이언트 생성 및 연결
     */
    private void connectToGame() {
        String ip = txtIp.getText().trim();
        String port = txtPort.getText().trim();
        String username = txtUserName.getText().trim();

        // 사용자 이름 유효성 검사
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "사용자 이름을 입력하세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 로그인 창 숨기기 및 게임 클라이언트 생성
        setVisible(false);
        new NetworkCardGameClient(ip, port, username);
    }

    /*
     폰트 생성 메서드
     커닝 및 리가처 효과가 적용된 폰트 생성
    */
    private Font createFont(String name, int style, int size) {
        Font font = new Font(name, style, size);
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        attributes.put(TextAttribute.LIGATURES, TextAttribute.LIGATURES_ON);
        return font.deriveFont(attributes);
    }

    /*
     메인 메서드
     애플리케이션 시작점
     시스템 룩앤필 설정 및 로그인 화면 생성
     */
    public static void main(String[] args) {
        try {
            // 시스템 룩앤필 설정
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 스레드 안전한 UI 생성
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getInstance().setVisible(true);
            }
        });
    }
}