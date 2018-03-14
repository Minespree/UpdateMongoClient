package net.minespree.migrations.impls;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import net.minespree.migrations.Migration;
import net.minespree.migrations.MigrationManager;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes old unused fields from each player document
 * @since 28/10/2017
 */
public class CleanupMigration extends Migration {
    private String[] REMOVE_FIELDS = new String[] {"lowerKnownNames", "lowerLastKnownName", "/statistics\\.GLOBAL\\.longs\\.latestJoinMcVersion.+/", "achievements", "settings", "staffChat", "unlockedCosmetics"};

    public CleanupMigration(MigrationManager manager) {
        super("Cleanup", manager);
    }

    @Override
    public boolean init() {
        MongoCollection<Document> collection = manager.getCollection("players");

        long total = collection.count();
        migrationCount(total);

        FindIterable<Document> all = collection.find();
        List<UpdateOneModel<Document>> updates = new ArrayList<>();

        Document unsetDoc = new Document();

        for (String field : REMOVE_FIELDS) {
            unsetDoc.put(field, "");
        }

        for (Document document : all) {
            updates.add(new UpdateOneModel<>(
                    Filters.eq("_id", document.get("_id")),
                    new Document("$unset", unsetDoc),
                    OPTIONS
            ));
        }

        BulkWriteResult result = collection.bulkWrite(updates);

        return result.wasAcknowledged();
    }
}
