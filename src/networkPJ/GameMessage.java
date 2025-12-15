package networkPJ;
import javax.swing.ImageIcon;
import java.io.Serializable;

/*
 네트워크 카드 게임에서 사용되는 메시지 클래스
 게임 상태와 이벤트를 직렬화하여 네트워크를 통해 전송
 Serializable 인터페이스를 구현하여 객체 직렬화 지원
*/
public class GameMessage implements Serializable {
    
   // 직렬화 버전 UID
    private static final long serialVersionUID = 1L;

    // 메시지 타입을 정의하는 상수들
    // 게임 내 다양한 이벤트와 상태 변화를 나타냄
    public static final int LOGIN = 1;           // 로그인 메시지
    public static final int POSITION = 2;        // 클라이언트 위치 메시지
    public static final int FLIP_CARD = 3;       // 카드 뒤집기 메시지
    public static final int RING_BELL = 4;       // 종 치기 메시지
    public static final int TURN_UPDATE = 5;     // 턴 업데이트 메시지
    public static final int SCORE_UPDATE = 6;    // 점수 업데이트 메시지
    public static final int LOGOUT = 7;          // 로그아웃 메시지
    public static final int GAME_OVER = 8;       // 게임 종료 메시지
    
    // 메시지의 주요 속성들
    private int type;           // 메시지의 유형
    private String clientId;    // 메시지를 보낸 클라이언트의 고유 식별자
    private String position;    // 클라이언트의 게임 내 위치
    private ImageIcon image;    // 메시지와 함께 전송되는 카드 이미지
    private int score;          // 클라이언트의 현재 점수
    private String cardImageName; // 카드 이미지의 이름

    //기본 메시지 생성자
    public GameMessage(int type, String clientId, String position) {
        this.type = type;
        this.clientId = clientId;
        this.position = position;
    }

    //이미지를 포함하는 메시지 생성자
    public GameMessage(int type, String clientId, String position, ImageIcon image) {
        this.type = type;
        this.clientId = clientId;
        this.position = position;
        this.image = image;
    }

    //이미지와 이미지 이름을 포함하는 메시지 생성자
    public GameMessage(int type, String clientId, String position, ImageIcon image, String cardImageName) {
        this(type, clientId, position, image);
        this.cardImageName = cardImageName;
    }

    //점수 업데이트를 위한 메시지 생성자
    public GameMessage(int type, String clientId, String position, int score) {
        this.type = type;
        this.clientId = clientId;
        this.position = position;
        this.score = score;
    }

    // Getter 메서드들: 각 속성에 대한 접근자 제공

    //메시지 유형 반환
    public int getType() { return type; }

    //클라이언트 식별자 반환
    public String getClientId() { return clientId; }

    //클라이언트 위치 반환
    public String getPosition() { return position; }

    //메시지의 이미지 반환
    public ImageIcon getImage() { return image; }

    //클라이언트 점수 반환
    public int getScore() { return score; }

    //카드 이미지 이름 반환
    public String getCardImageName() { return cardImageName; }
}