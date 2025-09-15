import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TelaLivros extends JFrame {
    private JTable tabelaLivros;
    private DefaultTableModel tableModel;
    private JTextField campoTitulo, campoAutor, campoID;
    private JButton btnAdicionar, btnEditar, btnExcluir, btnLimpar, btnCarregar;

    public TelaLivros() {
        configurarJanela();
        inicializarComponentes();
        organizarLayout();
        configurarEventos();
    }

    private void configurarJanela() {
        setTitle("Sistema Livraria - Firebase");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void inicializarComponentes() {
        String[] colunas = {"ID", "Título", "Autor"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaLivros = new JTable(tableModel);
        tabelaLivros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        campoTitulo = new JTextField(20);
        campoAutor = new JTextField(20);
        campoID = new JTextField(15);

        btnAdicionar = new JButton("Adicionar");
        btnEditar = new JButton("Editar");
        btnExcluir = new JButton("Excluir");
        btnLimpar = new JButton("Limpar");
        btnCarregar = new JButton("Carregar Firebase");

        btnAdicionar.setBackground(new Color(76, 175, 80));
        btnExcluir.setBackground(new Color(244, 67, 54));
        btnCarregar.setBackground(new Color(33, 150, 243));
        btnEditar.setBackground(new Color(255, 193, 7));
        btnLimpar.setBackground(new Color(158, 158, 158));
    }

    private void organizarLayout() {
        setLayout(new BorderLayout(10, 10));

        JPanel painelForm = new JPanel(new GridLayout(3, 2, 10, 10));
        painelForm.setBorder(BorderFactory.createTitledBorder("Dados do Livro"));
        painelForm.add(new JLabel("ID:"));
        painelForm.add(campoID);
        painelForm.add(new JLabel("Título:"));
        painelForm.add(campoTitulo);
        painelForm.add(new JLabel("Autor:"));
        painelForm.add(campoAutor);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnCarregar);

        JPanel painelSuperior = new JPanel(new BorderLayout());
        painelSuperior.add(painelForm, BorderLayout.CENTER);
        painelSuperior.add(painelBotoes, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(tabelaLivros);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Catálogo de Livros"));

        add(painelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void configurarEventos() {
        btnAdicionar.addActionListener(e -> {
            String id = campoID.getText();
            String titulo = campoTitulo.getText();
            String autor = campoAutor.getText();

            if (!id.isEmpty() && !titulo.isEmpty() && !autor.isEmpty()) {
                tableModel.addRow(new Object[]{id, titulo, autor});

                boolean sucesso = FirebaseManager.adicionarLivro(id, titulo, autor);

                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "Livro salvo no Firebase! ");
                    limparCampos();
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar no Firebase ");
                    tableModel.removeRow(tableModel.getRowCount() - 1);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
            }
        });

        btnExcluir.addActionListener(e -> excluirLivro());
        btnLimpar.addActionListener(e -> limparCampos());
        btnCarregar.addActionListener(e -> carregarDadosFirebase());

        btnEditar.addActionListener(e -> {
            try {
                String id = campoID.getText();
                String novoTitulo = campoTitulo.getText();
                String novoAutor = campoAutor.getText();

                if (id.isEmpty() || novoTitulo.isEmpty() || novoAutor.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
                    return;
                }

                // Mostra confirmação antes de editar
                int confirmacao = JOptionPane.showConfirmDialog(
                        this,
                        "Deseja realmente editar este livro?",
                        "Confirmar Edição",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmacao != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean sucesso = FirebaseManager.editarLivro(id, novoTitulo, novoAutor);

                if (sucesso) {

                    atualizarLinhaEspecifica(id, novoTitulo, novoAutor);

                    limparCampos();
                    JOptionPane.showMessageDialog(this, "✅ Livro editado com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Erro ao editar livro!");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        });

        tabelaLivros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaLivros.getSelectedRow() != -1) {
                preencherCamposComSelecao();

                btnEditar.setEnabled(true);
            } else {
                btnEditar.setEnabled(false);
            }
        });
    }

    private void excluirLivro() {
        int linhaSelecionada = tabelaLivros.getSelectedRow();
        if (linhaSelecionada != -1) {
            String id = tableModel.getValueAt(linhaSelecionada, 0).toString();

            boolean sucesso = FirebaseManager.excluirLivro(id);

            if (sucesso) {
                tableModel.removeRow(linhaSelecionada);
                JOptionPane.showMessageDialog(this, "Livro excluído! ✅");
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir ❌");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um livro para excluir!");
        }
    }

    private void carregarDadosFirebase() {
        String dados = FirebaseManager.buscarLivros();

        System.out.println("Dados recebidos do Firebase: " + dados);

        if (dados != null && !dados.equals("null")) {
            tableModel.setRowCount(0);

            try {
                if (dados.startsWith("{") && dados.endsWith("}")) {
                    dados = dados.substring(1, dados.length() - 1);
                }

                if (dados.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nenhum livro cadastrado ainda");
                    return;
                }

                String[] registros = dados.split("(?<=\"),");

                for (String registro : registros) {
                    if (registro.trim().isEmpty()) continue;

                    System.out.println("Processando: " + registro);

                    String id = extrairValor(registro, "id");
                    String titulo = extrairValor(registro, "titulo");
                    String autor = extrairValor(registro, "autor");

                    if (id != null && titulo != null && autor != null) {
                        tableModel.addRow(new Object[]{id, titulo, autor});
                    }
                }

                JOptionPane.showMessageDialog(this, "Dados carregados com sucesso! ✅");

            } catch (Exception e) {
                System.out.println("Erro ao processar dados: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Erro ao processar dados do Firebase");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nenhum livro encontrado no Firebase");
        }
    }

    private String extrairValor(String registro, String campo) {
        try {
            String busca = "\"" + campo + "\":\"";
            int start = registro.indexOf(busca);
            if (start != -1) {
                start += busca.length();
                int end = registro.indexOf("\"", start);
                return registro.substring(start, end);
            }
        } catch (Exception e) {
            System.out.println("Erro ao extrair " + campo + ": " + e.getMessage());
        }
        return null;
    }

    private void preencherCamposComSelecao() {
        int linhaSelecionada = tabelaLivros.getSelectedRow();
        if (linhaSelecionada != -1) {
            campoID.setText(tableModel.getValueAt(linhaSelecionada, 0).toString());
            campoTitulo.setText(tableModel.getValueAt(linhaSelecionada, 1).toString());
            campoAutor.setText(tableModel.getValueAt(linhaSelecionada, 2).toString());
        }
    }

    private void limparCampos() {
        campoID.setText("");
        campoTitulo.setText("");
        campoAutor.setText("");
        tabelaLivros.clearSelection();
    }



    private void atualizarLinhaEspecifica(String id, String novoTitulo, String novoAutor) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String idAtual = tableModel.getValueAt(i, 0).toString();
            if (idAtual.equals(id)) {
                tableModel.setValueAt(novoTitulo, i, 1);
                tableModel.setValueAt(novoAutor, i, 2);

                // Atualiza a seleção visual na tabela
                tabelaLivros.repaint();
                break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TelaLivros().setVisible(true);
        });
    }
}