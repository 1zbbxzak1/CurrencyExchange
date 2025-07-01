package ru.julia.currencyexchange.utils.configuration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseCleaner {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SpringLiquibase springLiquibase;

    public void resetDatabase() {
        try (var connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            String changelog = springLiquibase.getChangeLog().replaceFirst("^classpath:", "");

            Liquibase liquibase = new Liquibase(
                    changelog,
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.dropAll();
            liquibase.update();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
