package networkPJ;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;

public class CardGameGUI extends JFrame {
    private static final int IMG_WIDTH = 100;
    private static final int IMG_HEIGHT = 100;
    

    protected ImageIcon resizeImage(ImageIcon icon) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(IMG_WIDTH, IMG_HEIGHT, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }

    // 이미지 로딩 메서드 추가
    protected ImageIcon loadImageIcon(String path) {
        URL imageURL = getClass().getClassLoader().getResource(path);
        if (imageURL != null) {
            return new ImageIcon(imageURL);
        } else {
            System.err.println("이미지를 찾을 수 없습니다: " + path);
            return null;
        }
    }
    
 // 이미지명을 추출하는 메서드 추가
    protected String getImageName(ImageIcon imageIcon) {
        for (int i = 0; i < image.length; i++) {
            if (image[i] == imageIcon) {
                // 리소스 경로에서 실제 파일명 추출
                URL url = getClass().getClassLoader().getResource("networkImage/" + getImageFileName(i));
                return url != null ? url.getFile() : "unknown";
            }
        }
        return "unknown";
    }

    // 이미지 배열 인덱스에 해당하는 파일명을 반환하는 메서드
    private String getImageFileName(int index) {
        String[] fileNames = {
            "lime1.png", "lime2.png", "lime3.png", "lime4.png", "lime5.png",
            "banana1.png", "banana2.png", "banana3.png", "banana4.png", "banana5.png",
            "apple1.png", "apple2.png", "apple3.png", "apple4.png", "apple5.png",
            "orange1.png", "orange2.png", "orange3.png", "orange4.png", "orange5.png",
            "grape1.png", "grape2.png", "grape3.png", "grape4.png", "grape5.png",
            "plus1.png","minus1.png"
        };
        return fileNames[index];
    }

    protected ImageIcon image[] = {
        resizeImage(loadImageIcon("networkImage/lime1.png")),
        resizeImage(loadImageIcon("networkImage/lime2.png")),
        resizeImage(loadImageIcon("networkImage/lime3.png")),
        resizeImage(loadImageIcon("networkImage/lime4.png")),
        resizeImage(loadImageIcon("networkImage/lime5.png")),
        resizeImage(loadImageIcon("networkImage/banana1.png")),
        resizeImage(loadImageIcon("networkImage/banana2.png")),
        resizeImage(loadImageIcon("networkImage/banana3.png")),
        resizeImage(loadImageIcon("networkImage/banana4.png")),
        resizeImage(loadImageIcon("networkImage/banana5.png")),
        resizeImage(loadImageIcon("networkImage/apple1.png")),
        resizeImage(loadImageIcon("networkImage/apple2.png")),
        resizeImage(loadImageIcon("networkImage/apple3.png")),
        resizeImage(loadImageIcon("networkImage/apple4.png")),
        resizeImage(loadImageIcon("networkImage/apple5.png")),
        resizeImage(loadImageIcon("networkImage/orange1.png")),
        resizeImage(loadImageIcon("networkImage/orange2.png")),
        resizeImage(loadImageIcon("networkImage/orange3.png")),
        resizeImage(loadImageIcon("networkImage/orange4.png")),
        resizeImage(loadImageIcon("networkImage/orange5.png")),
        resizeImage(loadImageIcon("networkImage/grape1.png")),
        resizeImage(loadImageIcon("networkImage/grape2.png")),
        resizeImage(loadImageIcon("networkImage/grape3.png")),
        resizeImage(loadImageIcon("networkImage/grape4.png")),
        resizeImage(loadImageIcon("networkImage/grape5.png")),
        resizeImage(loadImageIcon("networkImage/plus1.png")),
        resizeImage(loadImageIcon("networkImage/minus1.png"))
    };
    
    protected ImageIcon backCard = resizeImage(loadImageIcon("networkImage/카드뒷면.png"));
    
    protected ImageIcon bell[] = {
        resizeImage(loadImageIcon("networkImage/종 멈춤.png")),
        resizeImage(loadImageIcon("networkImage/종 움직임.png"))
    };

    protected JLabel topLeftRandomCard = new JLabel();
    protected JLabel topLeftBackCard = new JLabel();
    protected JLabel topRightRandomCard = new JLabel();
    protected JLabel topRightBackCard = new JLabel();
    protected JLabel bottomLeftRandomCard = new JLabel();
    protected JLabel bottomLeftBackCard = new JLabel();
    protected JLabel bottomRightRandomCard = new JLabel();
    protected JLabel bottomRightBackCard = new JLabel();
    protected JLabel bellLabel = new JLabel();

    protected JButton flipButton = new JButton("카드뒤집기");
    protected JButton ringBellButton = new JButton("종치기");
    protected Set<Integer> usedIndexes;

    public CardGameGUI() {
        setTitle("카드 뒤집기 게임");
        setSize(600, 500); // 창 크기 증가
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container c = getContentPane();
        c.setLayout(new BorderLayout());
        c.setBackground(Color.BLACK);

        usedIndexes = new HashSet<>();

        // 메인 패널 (카드와 종을 담을 패널)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.BLACK);

        // GridBagConstraints 설정
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 각 섹션의 패널 생성
        JPanel topLeftSection = createCardSection(topLeftRandomCard, topLeftBackCard);
        JPanel topRightSection = createCardSection(topRightRandomCard, topRightBackCard);
        JPanel bottomLeftSection = createCardSection(bottomLeftRandomCard, bottomLeftBackCard);
        JPanel bottomRightSection = createCardSection(bottomRightRandomCard, bottomRightBackCard);

        // 종 레이블 초기화
        bellLabel.setIcon(bell[0]); // 초기 상태는 "종 멈춤"

        // 컴포넌트 배치
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(topLeftSection, gbc);

        gbc.gridx = 2;
        mainPanel.add(topRightSection, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(bellLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(bottomLeftSection, gbc);

        gbc.gridx = 2;
        mainPanel.add(bottomRightSection, gbc);

        // 초기 이미지 설정
        setAllRandomCards(backCard);
        setAllBackCards(backCard);

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        flipButton.setPreferredSize(new Dimension(150, 40));
        ringBellButton.setPreferredSize(new Dimension(150, 40));
        buttonPanel.add(flipButton);
        buttonPanel.add(ringBellButton);

        // 버튼 이벤트 설정
        flipButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ImageIcon newImage = getRandomImageExcludingBackCard();
                setAllRandomCards(newImage);
            }
        });

        // 종치기 버튼 이벤트
        ringBellButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 종 이미지를 "종 움직임"으로 변경
                bellLabel.setIcon(bell[1]);
                
                // 1초 후에 다시 "종 멈춤"으로 변경하고 카드를 초기화
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
            }
        });

        // 전체 레이아웃 구성
        c.add(mainPanel, BorderLayout.CENTER);
        c.add(buttonPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // 카드 섹션을 생성하는 헬퍼 메소드
    protected JPanel createCardSection(JLabel randomCard, JLabel backCard) {
        JPanel section = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        section.setBackground(Color.BLACK);
        section.add(randomCard);
        section.add(backCard);
        return section;
    }

    // 모든 랜덤 카드 이미지 설정
    protected void setAllRandomCards(ImageIcon image) {
        topLeftRandomCard.setIcon(image);
        topRightRandomCard.setIcon(image);
        bottomLeftRandomCard.setIcon(image);
        bottomRightRandomCard.setIcon(image);
    }

    // 모든 뒷면 카드 이미지 설정
    protected void setAllBackCards(ImageIcon image) {
        topLeftBackCard.setIcon(image);
        topRightBackCard.setIcon(image);
        bottomLeftBackCard.setIcon(image);
        bottomRightBackCard.setIcon(image);
    }

 // 뒷면 이미지를 제외한 랜덤 이미지를 가져옴
    protected ImageIcon getRandomImageExcludingBackCard() {
        Random random = new Random();
        int randomIndex = random.nextInt(image.length); // 단순히 랜덤 인덱스를 선택

        return image[randomIndex]; // 바로 반환
    }

    public static void main(String[] args) {
        new CardGameGUI();
    }
}