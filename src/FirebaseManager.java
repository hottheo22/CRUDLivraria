
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

    public class FirebaseManager {
        private static final String FIREBASE_URL = "https://desafio01bim-default-rtdb.firebaseio.com";
        private static final String API_KEY = carregarApiKey();

        private static String carregarApiKey() {
            Properties props = new Properties();
            try {
                props.load(new FileInputStream("config.properties"));
                return props.getProperty("API_KEY");
            } catch (IOException e) {
                throw new RuntimeException("Não foi possível carregar a API key", e);
            }
        }



        public static boolean adicionarLivro(String id, String titulo, String autor) {
            try {
                HttpClient client = HttpClient.newHttpClient();

                String jsonData = String.format(
                        "{\"id\":\"%s\",\"titulo\":\"%s\",\"autor\":\"%s\",\"timestamp\":%d}",
                        id, titulo, autor, System.currentTimeMillis()
                );


                String urlCompleta = FIREBASE_URL + "/livros/" + id + ".json?key=" + API_KEY;
                System.out.println("📤 URL: " + urlCompleta);
                System.out.println("📦 JSON: " + jsonData);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlCompleta))
                        .header("Content-Type", "application/json")
                        .PUT(BodyPublishers.ofString(jsonData))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


                System.out.println("📥 Status Code: " + response.statusCode());
                System.out.println("📥 Resposta: " + response.body());
                System.out.println("📥 Headers: " + response.headers());

                return response.statusCode() == 200;

            } catch (Exception e) {
                System.out.println("ERRO ");
                e.printStackTrace();
                return false;
            }
        }
        public static boolean editarLivro(String id, String novoTitulo, String novoAutor) {
            try {
                HttpClient client = HttpClient.newHttpClient();


                String jsonData = String.format(
                        "{\"titulo\":\"%s\",\"autor\":\"%s\",\"timestamp\":%d}",
                        novoTitulo, novoAutor, System.currentTimeMillis()
                );


                String urlCompleta = FIREBASE_URL + "/livros/" + id + ".json?key=" + API_KEY;
                System.out.println("📤 URL de edição: " + urlCompleta);
                System.out.println("📦 JSON de atualização: " + jsonData);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlCompleta))
                        .header("Content-Type", "application/json")
                        .method("PATCH", BodyPublishers.ofString(jsonData))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Log da resposta
                System.out.println("📥 Status Code: " + response.statusCode());
                System.out.println("📥 Resposta: " + response.body());
                System.out.println("📥 Headers: " + response.headers());

                return response.statusCode() == 200;

            } catch (Exception e) {
                System.out.println(" Erro ao editar livro: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        public static String buscarLivros() {
            try {
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(FIREBASE_URL + "/livros.json?key=" + API_KEY))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("Busca - Status: " + response.statusCode());
                System.out.println("Busca - Dados: " + response.body());

                if (response.statusCode() == 200) {
                    return response.body();
                }

            } catch (Exception e) {
                System.out.println("ERRO ao buscar: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }


        public static boolean excluirLivro(String id) {
            try {
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(FIREBASE_URL + "/livros/" + id + ".json?key=" + API_KEY))
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 200;

            } catch (Exception e) {
                System.out.println("ERRO ao excluir: " + e.getMessage());
                return false;
            }
        }


    }

