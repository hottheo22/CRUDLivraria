import javax.swing.*;
    public class SistemaLivraria {
        public static void main(String[] args) {

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }


            SwingUtilities.invokeLater(() -> {
                TelaLivros tela = new TelaLivros();
                tela.setVisible(true);
            });
        }
    }

