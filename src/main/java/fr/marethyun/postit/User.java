package fr.marethyun.postit;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class User {

    public static final String GET_USERNAME_QUERY = "SELECT * FROM users WHERE name = ?";

    public final int id;
    public final String name;
    public transient final String password; // non-serialized password

    public User(int id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public static final class Mapper implements RowMapper<User> {
        @Override
        public User map(ResultSet rs, StatementContext ctx) throws SQLException {
            return new User(rs.getInt("id"), rs.getString("name"), rs.getString("password"));
        }
    }
}
