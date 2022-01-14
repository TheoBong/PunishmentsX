package io.github.punishmentsx.utils;

import io.github.punishmentsx.PunishmentsX;
import lombok.Data;
import org.bson.Document;

public @Data class DocumentUtil {
    private Document document;
    private PunishmentsX plugin;
    private String collection;
    private String field;
    private Object id;

    public DocumentUtil(PunishmentsX plugin, String collection, String field, Object id) {
        this.plugin = plugin;
        this.collection = collection;
        this.field = field;
        this.id = id;

        get();
    }

    public void get() {
        plugin.getMongo().getDocument(false, collection, field, id, document -> {
            this.document = document;
        });
    }
}
