package net.minespree.migrations;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.minespree.migrations.impls.*;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @since 28/10/2017
 */
@Getter
public class MigrationManager {
    private Migration[] MIGRATIONS = new Migration[] {
        new KitMigration(this),
        new CleanupMigration(this),
        new FieldRenameMigration(this),
        new FirstJoinMigration(this),
        new CosmeticsObjectToArrayMigration(this)
    };

    private Logger logger;
    private BufferedReader reader;

    private MongoClient client;
    private MongoDatabase database;

    public static void main(String[] args) throws IOException {
        new MigrationManager();
    }

    public MigrationManager() throws IOException {
        logger = Logger.getLogger(MigrationManager.class.getSimpleName());
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new LogFormatter());
        logger.addHandler(handler);

        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);

        reader = new BufferedReader(new InputStreamReader(System.in));

        logger.info("Mongo hostname:");
        String host = reader.readLine();

        logger.info("Mongo port: (27017)");
        int port = Integer.parseInt(reader.readLine());

        logger.info("Mongo username:");
        String user = reader.readLine();

        logger.info("Mongo password");
        String password = reader.readLine();

        logger.info("Mongo database");
        String database = reader.readLine();

        logger.log(Level.INFO, "Connecting to Mongo...");

        client = new MongoClient(
            Collections.singletonList(new ServerAddress(host, port)),
            Collections.singletonList(MongoCredential.createCredential(user, database, password.toCharArray()))
        );

        this.database = client.getDatabase(database);

        logger.info("Connected successfully");
        logger.info("Loading migrations");

        Migration[] migrations = getMigrationsToRun();

        int current = 1;
        int total = migrations.length;

        for (Migration migration : migrations) {
            logger.info("Starting " + migration.getName() + " migration");

            boolean success = migration.init();

            if (success) {
                logger.info("Migration completed successfully (" + (current++) + "/" + total + ")");
            } else {
                logger.severe("Migration failed! Exiting");
                System.exit(1);
                return;
            }
        }

        logger.info("Done! All migrations were completed");

        client.close();
        System.exit(0);
    }

    public Migration[] getMigrationsToRun() {
        logger.info("Select migrations to run... (Use comma separated numbers to run multiple ones)");

        int count = 1;
        for (Migration migration : MIGRATIONS) {
            logger.info("[" + (count++) + "] " + migration.getName());
        }

        while (true) {
            try {
                String list = reader.readLine();
                List<Migration> migrations = new ArrayList<>();

                for (String id : list.replace(" ", "").split(",")) {
                    try {
                        int index = Integer.parseInt(id) - 1;

                        if (index < 0 || index >= MIGRATIONS.length) {
                            logger.severe("Invalid migration ID inputted, ignoring");
                            continue;
                        }

                        migrations.add(MIGRATIONS[index]);
                    } catch (NumberFormatException ignored) {
                        logger.severe("Invalid number inputted, use comma separated ids:");
                        continue;
                    }
                }

                return migrations.toArray(new Migration[migrations.size()]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public MongoCollection<Document> getCollection(String name) {
        return database.getCollection(name);
    }
}
