import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        allUsersInfo();
        userByIdInfo(3);//number should be 1 to 10
        byUsernameInfo("Karianne");//choose between "Bret", "Antonette", "Samantha", "Karianne", "Kamren", "Leopoldo_Corkery", "Elwyn.Skiles", "Maxime_Nienow", "Delphine" and "Moriah.Stanton"
        newUser();
        updateUser(6);
        deleteUser();
        userXpostYcomments(1);
        unfinishedTasks(1);
    }

    private static final String USERS_URL = "https://jsonplaceholder.typicode.com/users";

    private static void newUser() throws IOException {
        //створення нового об'єкта в https://jsonplaceholder.typicode.com/users
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(post))
                .build();

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println);
        System.out.println("_______________________________________________");
    }

    private static void updateUser(int a) throws IOException {
//--оновлення об'єкта в https://jsonplaceholder.typicode.com/users. Вважаємо, що метод працює правильно, якщо у відповідь ви отримаєте
//  оновлений JSON (він повинен бути таким самим, що ви відправили).
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(USERS_URL + "/" + a);
        httpPut.addHeader("Accept", "application/json");
        httpPut.addHeader("Content-type", "application/json");
        StringEntity stringEntity = new StringEntity(post);
        httpPut.setEntity(stringEntity);

        InputStream inputStream = httpPut.getEntity().getContent();
        String response = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining());
        System.out.println("Updated data for user " + a + " = " + response);
        System.out.println("_______________________________________________");
    }

    private static void deleteUser() {
//--видалення об'єкта з https://jsonplaceholder.typicode.com/users.
//  Тут будемо вважати коректним результат - статус відповіді з групи 2xx (наприклад, 200).
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://jsonplaceholder.typicode.com/users/5"))
                .header("Content-Type", "application/json")
                .DELETE().build();

        HttpClient.newHttpClient()
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(x -> System.out.println("The response for delete request is = " + x));
        System.out.println("_______________________________________________");
    }

    private static void allUsersInfo() throws IOException {
        //отримання інформації про всіх користувачів https://jsonplaceholder.typicode.com/users
        Document doc = Jsoup.connect(USERS_URL).ignoreContentType(true).get();
        String text = doc.text();
        System.out.println("Information about all users = " + text);
        System.out.println("_______________________________________________");
    }

    private static void userByIdInfo(int a) throws IOException {
        //отримання інформації про користувача за id https://jsonplaceholder.typicode.com/users/{id}
        String url = "https://jsonplaceholder.typicode.com/users/" + a;
        String text = Jsoup.connect(url)
                .ignoreContentType(true)
                .get().text();
        System.out.println("Information about user " + a + " = " + text);
        System.out.println("_______________________________________________");
    }

    private static void byUsernameInfo(String username) throws IOException {
        //отримання інформації про користувача за username - https://jsonplaceholder.typicode.com/users?username={username}
        String url = "https://jsonplaceholder.typicode.com/users?username=" + username;
        String text = Jsoup.connect(url)
                .ignoreContentType(true)
                .get().text();
        System.out.println("Information about user with username " + username + " = " + text);
        System.out.println("_______________________________________________");
    }

    private static void unfinishedTasks(int a) throws IOException {
        String url = "https://jsonplaceholder.typicode.com/users/" + a + "/todos";
        Document doc = Jsoup.connect(url).ignoreContentType(true).get();

        String data = doc.body().text();
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(data).getAsJsonArray();
        System.out.println("User " + a + " unfinished tasks:");
        for (JsonElement el : jsonArray) {
            Tasks obj = gson.fromJson(el, Tasks.class);
            if (!obj.isCompleted()) System.out.println(obj);
        }
        System.out.println("_______________________________________________");
    }
    
    private static void userXpostYcomments(int x) throws IOException {
        String urlUser = "https://jsonplaceholder.typicode.com/users/" + x + "/posts";

        Document docUser = Jsoup.connect(urlUser).ignoreContentType(true).get();
        String dataUser = docUser.body().text();
        Gson gson = new Gson();
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(dataUser).getAsJsonArray();
        int size = jsonArray.size();
        JsonElement el = jsonArray.get(size - 1);
        UsersPost obj = gson.fromJson(el, UsersPost.class);
        int i = obj.getId();
        String urlComments = "https://jsonplaceholder.typicode.com/posts/" + i + "/comments";

        Document docComments = Jsoup.connect(urlComments).ignoreContentType(true).get();
        String dataComments = docComments.body().text();
        JsonArray jsonArray1 = jsonParser.parse(dataComments).getAsJsonArray();
        int y = jsonArray1.size();
        System.out.println("User " + x + " has " + y + " comments " + dataComments);
        System.out.println("_______________________________________________");

        String filename = "user-" + x + "-post-" + y + "-comments.json";
        File userComments = new File(filename);
        try (FileWriter writer = new FileWriter(userComments)) {
            writer.write(dataComments);
            writer.flush();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    private static String post = "{\n" +
            "  \"name\": \"John\",\n" +
            "  \"username\": \"Doe\",\n" +
            "  \"email\": \"JDoe@test.com\",\n" +
            "  \"address\": {\n" +
            "    \"street\": \"Grim camp\",\n" +
            "    \"suite\": \"Apt. 007\",\n" +
            "    \"city\": \"OmgTown\",\n" +
            "    \"zipcode\": \"86325\",\n" +
            "    \"geo\": {\n" +
            "      \"lat\": \"-14.408\",\n" +
            "      \"lng\": \"-71.300\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"phone\": \"1-424-28-71-18-00\",\n" +
            "  \"website\": \"zeroforksleft.org\",\n" +
            "  \"company\": {\n" +
            "    \"name\": \"Otherworld cervices\",\n" +
            "    \"catchPhrase\": \"Grim town cervices - zero f**ks left\",\n" +
            "    \"bs\": \"why bother, if we're already at these coordinates\"\n" +
            "  }\n" +
            "}";

    private class UsersPost {
        public int userId;
        public int id;
        public String title;
        public String body;

        public UsersPost(int userId, int id, String title, String body) {
            this.userId = userId;
            this.id = id;
            this.title = title;
            this.body = body;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return "{" +
                    "userId=" + userId +
                    ", id=" + id +
                    ", title='" + title + '\'' +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    private class Tasks {

        public int userId;
        public int id;
        public String title;
        public boolean completed;

        public Tasks(int userId, int id, String title, boolean completed) {
            this.userId = userId;
            this.id = id;
            this.title = title;
            this.completed = completed;
        }

        public int getUserId() {
            return userId;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public boolean isCompleted() {
            return completed;
        }

        @Override
        public String toString() {
            return "{" +
                    "userId=" + userId +
                    ", id=" + id +
                    ", title='" + title + '\'' +
                    ", completed=" + completed +
                    '}';
        }
    }
}