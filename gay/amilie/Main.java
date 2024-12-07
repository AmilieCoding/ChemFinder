package gay.amilie;

public class Main {
    public static void main(String[] args) {
        try {
            GUI.UserGUI();
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}