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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Restore cosmetic data from backup and convert it to an array on the players document
 * @since 11/11/2017
 */
public class CosmeticsObjectToArrayMigration extends Migration {
    public CosmeticsObjectToArrayMigration(MigrationManager manager) {
        super("CosmeticsBackupObjectToArray", manager);
    }

    @Override
    public boolean init() {
        MongoCollection<Document> collection = manager.getCollection("players");
        MongoCollection<Document> backupCollection = manager.getCollection("playersBackup");

        long total = collection.count();
        migrationCount(total);

        FindIterable<Document> all = backupCollection.find();
        List<UpdateOneModel<Document>> updates = new ArrayList<>();

        for (Document document : all) {
            Object cosmeticsDoc = document.get("unlockedCosmetics");

            if (cosmeticsDoc == null) continue;

            Document unlockedCosmetics = (Document) cosmeticsDoc;
            Set<Document> setDocs = new HashSet<>();

            for (String cosmeticId : unlockedCosmetics.keySet()) {
                Document cosmeticData = (Document) unlockedCosmetics.get(cosmeticId);

                Document newCosmetic = new Document("cosmeticId", cosmeticId);
                newCosmetic.put("equipped", cosmeticData.getBoolean("equipped", false));

                if (cosmeticData.containsKey("ammo")) {
                    newCosmetic.put("ammo", cosmeticData.getInteger("ammo"));
                }

                setDocs.add(newCosmetic);
            }

            updates.add(new UpdateOneModel<>(
                Filters.eq("_id", document.get("_id")),
                new Document("$set", new Document("unlockedCosmetics", setDocs)),
                OPTIONS
            ));
        }

        BulkWriteResult result = collection.bulkWrite(updates);

        return result.wasAcknowledged();
    }
}
