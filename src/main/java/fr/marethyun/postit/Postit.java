package fr.marethyun.postit;

import com.google.gson.annotations.SerializedName;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Postit {

    public static final String GET_ALL_QUERY = "SELECT * FROM postits JOIN users ON postits.owner_id = users.id WHERE name = ? ORDER BY time_used DESC";
    public static final String INSERT_USER = "INSERT INTO postits(owner_id) VALUES(:owner_id)";
    public static final String UPDATE = "UPDATE postits SET color = :color, content = :content, relative_x = :rx, relative_y = :ry, time_used = strftime('%s', 'now') WHERE id = :id AND owner_id = :owner_id";
    public static final String DELETE = "DELETE FROM postits WHERE id = :id AND owner_id = :owner_id";

    public final int id;
    public final User owner;
    public final String color;
    public final String content;
    @SerializedName("relative_x")
    public final int relativeX;
    @SerializedName("relative_y")
    public final int relativeY;
    @SerializedName("time_used")
    public final long timeUsed;

    public Postit(int id, User owner, String color, String content, int relativeX, int relativeY, long timeUsed) {
        this.id = id;
        this.owner = owner;
        this.color = color;
        this.content = content;
        this.relativeX = relativeX;
        this.relativeY = relativeY;
        this.timeUsed = timeUsed;
    }

    public static class Mapper implements RowMapper<Postit> {
        @Override
        public Postit map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new Postit(
                    rs.getInt("id"),
                    new User(
                            rs.getInt("owner_id"),
                            rs.getString("name"),
                            rs.getString("password")),
                    rs.getString("color"),
                    rs.getString("content"),
                    rs.getInt("relative_x"),
                    rs.getInt("relative_y"),
                    rs.getLong("time_used")
            );
        }
    }
}
