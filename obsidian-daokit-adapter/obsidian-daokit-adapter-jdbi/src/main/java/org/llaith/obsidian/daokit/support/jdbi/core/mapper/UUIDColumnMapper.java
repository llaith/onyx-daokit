package org.llaith.obsidian.daokit.support.jdbi.core.mapper;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 *
 */
public enum UUIDColumnMapper implements ResultColumnMapper<UUID> {

    WRAPPER;

    @Override
    public UUID mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final UUID value = (UUID)r.getObject(columnNumber);
        return r.wasNull() ? null : value;
    }

    @Override
    public UUID mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final UUID value = (UUID)r.getObject(columnLabel);
        return r.wasNull() ? null : value;
    }
}