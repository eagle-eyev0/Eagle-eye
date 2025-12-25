package com.eagleeye.app;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DuplicateScanner {

    // Returns number of duplicate files found (by display name)
    public static int scanDuplicates(Context ctx, Uri folderUri) {
        if (ctx == null || folderUri == null) return 0;

        Map<String, Integer> seen = new HashMap<>();
        Set<String> dups = new HashSet<>();

        scanTree(ctx, folderUri, seen, dups);

        // Count of duplicate files (not groups)
        int dupCount = 0;
        for (int c : seen.values()) {
            if (c > 1) dupCount += (c - 1);
        }
        return dupCount;
    }

    private static void scanTree(Context ctx, Uri treeUri, Map<String, Integer> seen, Set<String> dups) {
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
        );

        Cursor cursor = null;
        try {
            cursor = ctx.getContentResolver().query(
                    childrenUri,
                    new String[] {
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            DocumentsContract.Document.COLUMN_MIME_TYPE
                    },
                    null,
                    null,
                    null
            );
            if (cursor == null) return;

            while (cursor.moveToNext()) {
                String docId = cursor.getString(0);
                String name = cursor.getString(1);
                String mime = cursor.getString(2);

                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                    Uri subTree = DocumentsContract.buildTreeDocumentUri(treeUri.getAuthority(), docId);
                    scanTree(ctx, subTree, seen, dups);
                } else {
                    int count = seen.getOrDefault(name, 0) + 1;
                    seen.put(name, count);
                    if (count > 1) dups.add(name);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
