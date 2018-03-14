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
 * Gives the player the first join achievement.
 * @since 28/10/2017
 */
public class FirstJoinMigration extends Migration {
    public FirstJoinMigration(MigrationManager manager) {
        super("FirstJoin", manager);
    }

    @Override
    public boolean init() {
        MongoCollection<Document> collection = manager.getCollection("players");

        long total = collection.count();
        migrationCount(total);

        Document updateDoc = new Document();

        updateDoc.put("$set", new Document("achievements.firstJoin", "true"));

        Document incDoc = new Document();

        incDoc.put("coins", 50);
        incDoc.put("experience", 100);

        updateDoc.put("$inc", incDoc);

        FindIterable<Document> all = collection.find();
        List<UpdateOneModel<Document>> updates = new ArrayList<>();

        for (Document document : all) {
            updates.add(new UpdateOneModel<>(
                    Filters.eq("_id", document.get("_id")),
                    updateDoc,
                    OPTIONS
            ));
        }

        BulkWriteResult result = collection.bulkWrite(updates);

        return result.wasAcknowledged();
    }
}
