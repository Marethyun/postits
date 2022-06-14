package fr.marethyun.postit;

import com.google.gson.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;

import java.io.File;
import java.util.*;

import static spark.Spark.*;

public class App implements Runnable {

    private final int port;
    private final Jdbi jdbi;
    private final Gson gson;

    private HashMap<String, User> sessions = new HashMap<>();

    public static final String GET_COLORS = "SELECT color FROM colors";

    public App(int port, File database) {
        this.port = port;
        this.jdbi = Jdbi.create(String.format("jdbc:sqlite:%s", database))
                .installPlugin(new SQLitePlugin());

        // Add support for enum serialization
        this.gson = new GsonBuilder().registerTypeAdapter(Error.class, (JsonSerializer<Error>) (src, typeOfSrc, context) -> {
            JsonObject object = new JsonObject();
            object.addProperty("code", src.code);
            return object;
        }).create();
    }

    public static void main(String[] args) {
        int port = 8080;
        String dbPath = "postits.db";

        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }

        if (args.length >= 2) {
            dbPath = args[1];
        }

        File database = new File(dbPath);
        if (!database.exists()) {
            System.err.println(String.format("File %s does not exists", database.getAbsolutePath()));
            System.exit(-1);
        }

        System.out.println(String.format("Starting with port=%s and database=%s", port, database.getPath()));
        new App(port, database).run();
    }

    @Override
    public void run() {
        port(this.port);

        staticFileLocation("/static");

        exception(Exception.class, (exception, request, response) -> {
            exception.printStackTrace();

            response.type("application/json");
            response.status(500); // Internal server error
            response.body(gson.toJson(new ErrorResponse(Error.ERROR.code)));
        });

        // Auth
        // {"username" : "toto", "password" : "toto"}
        post("/auth", (request, response) -> {
            response.type("application/json");

            AuthRequest authRequest = gson.fromJson(request.body(), AuthRequest.class);

            Optional<User> user = jdbi.withHandle(handle -> handle.select(User.GET_USERNAME_QUERY, authRequest.username).map(new User.Mapper()).findOne());
            if (!user.isPresent()) return gson.toJson(new ErrorResponse(Error.INVALID_USERNAME.code));

            if (!user.get().password.equals(authRequest.password))
                return gson.toJson(new ErrorResponse(Error.INVALID_PASSWORD.code));

            // Is he already logged in ?
            if (sessions.values().stream().anyMatch(usr -> usr.name.equals(authRequest.username)))
                return gson.toJson(new ErrorResponse(Error.ALREADY_LOGGED.code));

            String token = UUID.randomUUID().toString();
            sessions.put(token, user.get());

            return gson.toJson(new AuthResponse(token));
        });

        // Disconnect
        // /disconnect?token=X
        post("/disconnect", (request, response) -> {
            response.type("application/json");

            String token = request.queryParams("token");
            if (!sessions.containsKey(token)) return gson.toJson(new ErrorResponse(Error.NO_SESSION.code));

            sessions.remove(token);

            response.status(200);
            return gson.toJson(new JsonObject()); // Empty response
        });

        // Get ALL postits for user
        // /postits?token=X
        get("/postit", (request, response) -> {
            response.type("application/json");

            String token = request.queryParams("token");
            if (!sessions.containsKey(token)) return gson.toJson(new ErrorResponse(Error.NO_SESSION.code));

            User user = sessions.get(token);

            return gson.toJson((List<Postit>) jdbi.withHandle(handle -> handle.select(Postit.GET_ALL_QUERY, user.name).map(new Postit.Mapper()).list()));
        });

        // Add a postit
        // token=X
        post("/postit", (request, response) -> {
            response.type("application/json");

            String token = request.queryParams("token");
            if (!sessions.containsKey(token)) return gson.toJson(new ErrorResponse(Error.NO_SESSION.code));

            int id = jdbi.withHandle(handle -> {
                handle.createUpdate(Postit.INSERT_USER)
                        .bind("owner_id", sessions.get(token).id)
                        .execute();

                return handle.createQuery("SELECT LAST_INSERT_ROWID()").mapTo(Integer.class).one();
            });

            return gson.toJson(new IdMessage(id));
        });

        // Modify a postit
        // Postit object with User set as null
        // token=X
        put("/postit", (request, response) -> {
            response.type("application/json");

            String token = request.queryParams("token");
            if (!sessions.containsKey(token)) return gson.toJson(new ErrorResponse(Error.NO_SESSION.code));

            Postit postit = gson.fromJson(request.body(), Postit.class);

            int count = jdbi.withHandle(handle -> handle.createUpdate(Postit.UPDATE)
                    .bind("id", postit.id)
                    .bind("owner_id", sessions.get(token).id)
                    .bind("color", postit.color)
                    .bind("content", postit.content)
                    .bind("rx", postit.relativeX)
                    .bind("ry", postit.relativeY)
                    .execute()
            );

            if (count < 1) return gson.toJson(new ErrorResponse(Error.ERROR.code)); // Something bad happened

            return gson.toJson(new JsonObject());
        });

        // Delete a postit
        // token=X
        delete("/postit", (request, response) -> {
            response.type("application/json");

            String token = request.queryParams("token");
            if (!sessions.containsKey(token)) return gson.toJson(new ErrorResponse(Error.NO_SESSION.code));

            IdMessage message = gson.fromJson(request.body(), IdMessage.class);

            int count = jdbi.withHandle(handle -> handle.createUpdate(Postit.DELETE)
                    .bind("id", message.id)
                    .bind("owner_id", sessions.get(token).id)
                    .execute()
            );

            if (count < 1) return gson.toJson(new ErrorResponse(Error.ERROR.code));

            return gson.toJson(new JsonObject());
        });

        get("/colors", (request, response) -> {
            response.type("application/json");
            return gson.toJson((List<String>) jdbi.withHandle(handle -> handle.createQuery(GET_COLORS).mapTo(String.class).list()));
        });

        // Retrieve error messages
        get("/errors", (request, response) -> {
            response.type("application/json");

            JsonArray array = new JsonArray();

            for (Error value : Error.values()) {
                JsonObject object = new JsonObject();
                object.addProperty("code", value.code);
                object.addProperty("message", value.genericMessage);

                array.add(object);
            }

            return gson.toJson(array);
        });

        get("/stop", (request, response) -> {
            stop();
            return "Stopped";
        });
    }
}
