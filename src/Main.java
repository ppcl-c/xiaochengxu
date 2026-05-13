import jakarta.json.*;
import com.sun.net.httpserver.*;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.*;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Main {
    String ip;
    JsonObject json;
static     String mss = "";
static String akl = "";
    public static void main(String[] args)throws IOException {
        Main m =new Main();
        HttpServer server = HttpServer.create(new InetSocketAddress(8000),0);
        server.setExecutor(Executors.newFixedThreadPool(20));
        server.createContext("/", t -> {
            InetSocketAddress address = t.getLocalAddress();
            m.ip = address.getAddress().getHostAddress();
           File file = new File("main1.html");
           byte[] html = Files.readAllBytes(file.toPath());
           t.getResponseHeaders().add("Content-Type","text/html");
           t.sendResponseHeaders(200,html.length);
           OutputStream os = t.getResponseBody();
           os.write(html);
           os.close();
        });
        server.createContext("/注册", t -> {
            InetSocketAddress address = t.getLocalAddress();
            m.ip = address.getAddress().getHostAddress();
            File file = new File("main2.html");
            byte[] html = Files.readAllBytes(file.toPath());
            t.getResponseHeaders().add("Content-Type","text/html");
            t.sendResponseHeaders(200,html.length);
            OutputStream os = t.getResponseBody();
            os.write(html);
            os.close();
        });
        server.createContext("/az", t -> {
            InetSocketAddress address = t.getRemoteAddress();
            m.ip = address.getAddress().getHostAddress();
            String query = t.getRequestURI().getQuery();
            String den = "";
            String pwd ="";
            if (query != null && query.contains("den")) {
                den = query.split("den=")[1].split("&")[0];

            }  if (query != null&& query.contains("pwd")) {
                pwd = query.split("pwd=")[1];

            }
            System.out.println(den);
            System.out.println(pwd);
            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("den",den)
                    .add("pwd",pwd)
                    .build();
            System.out.println(m.ip);
            Files.write(Paths.get(m.ip +".json"),jsonObject.toString().getBytes());
            t.sendResponseHeaders(200,0);
            t.getResponseBody().write(jsonObject.toString().getBytes());
            t.close();
        });
        server.createContext("/al", t -> {
            InetSocketAddress cc = t.getRemoteAddress();
            m.ip = cc.getAddress().getHostAddress();
            t.sendResponseHeaders(200,0);
            File file = new File(m.ip +".json");
            if (!file.exists()) {
                m.json = Json.createObjectBuilder()
                        .add("den","Nane")
                        .add("pwd","Nane")
                        .build();
            } else {
                try(JsonReader reader = Json.createReader(new FileReader(file))){
                    JsonObject jnon = reader.readObject();
                    String den = jnon.getString("den");
                    String pwq = jnon.getString("pwd");
                    m.json = Json.createObjectBuilder()
                            .add("den",den)
                            .add("pwd",pwq)
                            .build();

                }
            }
            t.getResponseBody().write(m.json.toString().getBytes());
            t.getResponseBody().close();
        });
        server.createContext("/lp1", t -> {
            InetSocketAddress cc = t.getRemoteAddress();
            m.ip = cc.getAddress().getHostAddress();
            int cl = cc.getPort();
            String query = t.getRequestURI().getQuery();
            if (query != null && query.contains("mss")) {
                String mak = URLDecoder.decode(query.substring(4), "UTF-8");
                mss += "来自" + akl + "的信息:"+ mak + "\n";
                System.out.println("来自"+akl +"的信息:"+mak);
            }
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write(mss.getBytes());
            os.close();
        });
        server.createContext("/lp", t -> {
            InetSocketAddress cc =  t.getRemoteAddress();
            m.ip = cc.getAddress().getHostAddress();
            try(JsonReader reader = Json.createReader(new FileReader(m.ip +".json"))) {
                JsonObject jsonObject = reader.readObject();
                akl = jsonObject.getString("den");
            }catch (Exception e) {
                System.out.println("没有用户" + m.ip);
            }

            String html = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <title>聊天</title>
            </head>
            <body>
                <p id="msg" style="white-space: pre-wrap;">%s</p>
                <input type="text" id="input" placeholder="请输入">
                <button onclick = "window.location.href='/'">返回</button>
                <button onclick="send()">发送</button>
                <button onclick = "location.href = '/down'"> 下载 </button>

                <script>
                   
                    function send() {
                        let text = document.getElementById("input").value.trim();
                        if (!text) return;
                        
                        fetch("/lp1?mss=" + encodeURIComponent(text))
                        .then(() => location.reload());
                        
                        document.getElementById("input").value = "";
                    }

                   
                    setInterval(() => {
                                         fetch("/lp")
                                         .then(res => res.text())
                                                 .then(html => {
                                                    let start = html.indexOf("来自")
                                                    let end = html.indexOf('</p>')
                                                    let newMsg = html.substring(start, end)
                                                    document.getElementById("msg").innerText = newMsg;
                                                })
                                        },500)
                </script>
            </body>
            </html>
        """.formatted("来自" + akl + "的信息:\n" + mss);

            t.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            os.write(html.getBytes());
            os.close();
        });
        server.start();
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String s = sc.nextLine();
                if (s.equals("exit")) {
                    server.stop(0);
                    break;
                } else if (s.equals("jsonto")) {
                    System.out.println("请输入ip");
                    String ip = sc.nextLine();
                    try(JsonReader reader = Json.createReader(new FileReader(ip +".json"))) {
                        JsonObject jsonObject = reader.readObject();
                        String p = jsonObject.getString("pwd");
                        String d = jsonObject.getString("den");
                        System.out.println("该IP的用户名是" + d + "密码是" + p);
                    } catch (FileNotFoundException e) {
                        System.out.println("没有用户" + ip +e);
                    }
                }
            }
        }).start();

    }

}