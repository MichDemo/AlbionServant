package com.albionservant.integration.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class SqliteDataSourceConfiguration {

    @Bean
    public Path albionServantDatabasePath(LocalIntegrationProperties properties) throws IOException {
        String configured = properties.getSqlite().getPath();
        Path path;

        if (configured != null && !configured.isBlank()) {
            path = Path.of(configured.trim());
            if (!path.isAbsolute()) {
                path = Path.of("").toAbsolutePath().normalize().resolve(path).normalize();
            }
        } else {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) {
                path = Path.of(localAppData, "AlbionServant", "data", "albionservant.db");
            } else {
                path = Path.of(System.getProperty("user.home"), ".albionservant", "data", "albionservant.db");
            }
        }

        Path parent = path.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return path.toAbsolutePath().normalize();
    }

    @Bean(destroyMethod = "close")
    public DataSource dataSource(
            LocalIntegrationProperties properties,
            Path albionServantDatabasePath
    ) {
        SQLiteConfig sqliteConfig = new SQLiteConfig();
        sqliteConfig.setBusyTimeout(Math.max(1_000, properties.getSqlite().getBusyTimeoutMs()));
        sqliteConfig.enforceForeignKeys(true);
        sqliteConfig.setJournalMode(SQLiteConfig.JournalMode.WAL);
        sqliteConfig.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);

        SQLiteDataSource sqliteDataSource = new SQLiteDataSource(sqliteConfig);
        String normalizedPath = albionServantDatabasePath.toString().replace('\\', '/');
        sqliteDataSource.setUrl("jdbc:sqlite:" + normalizedPath);

        HikariConfig hikari = new HikariConfig();
        hikari.setDataSource(sqliteDataSource);
        hikari.setPoolName("AlbionServant-SQLite");
        hikari.setMaximumPoolSize(Math.max(1, properties.getSqlite().getPoolSize()));
        hikari.setMinimumIdle(1);
        hikari.setConnectionTimeout(Math.max(5_000, properties.getSqlite().getBusyTimeoutMs()));
        hikari.setAutoCommit(true);

        return new HikariDataSource(hikari);
    }
}
