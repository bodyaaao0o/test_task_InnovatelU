import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> documents = new HashMap<>();
        /**
         * Implementation of this method should upsert the document to your storage
         * And generate unique id if it does not exist, don't change [created] field
         *
         * @param document - document content and author data
         * @return saved document
         */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Cannot be null");
        }

        String id = document.getId();
        if (id == null) {
            id = UUID.randomUUID().toString();
            document = document.toBuilder().id(id).created(Instant.now()).build();
        } else {
            Document existingDocument = documents.get(id);
            if (existingDocument != null) {
                document = document.toBuilder().created(existingDocument.getCreated()).build();
            }
        }
        documents.put(id, document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<Document> result = new ArrayList<>();
        for (Document document : documents.values()) {
            if (requestsFilters(document, request)) {
                result.add(document);
            }
        }
        return result;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    private boolean requestsFilters(Document document, SearchRequest request) {
        if (request.getTitlePrefixes() != null) {
            boolean titleMatches = request.getTitlePrefixes().stream().anyMatch(document.getTitle()::startsWith);
            if (!titleMatches) return false;
        }

        if (request.getContainsContents() != null) {
            boolean contentMatches = request.getContainsContents().stream().anyMatch(document.getContent()::contains);
            if (!contentMatches) return false;
        }

        if (request.getAuthorIds() != null) {
            if (!request.getAuthorIds().contains(document.getAuthor().getId())) return false;
        }

        if (request.getCreatedFrom() != null) {
            if (document.getCreated().isBefore(request.getCreatedFrom())) return false;
        }

        if (request.getCreatedTo() != null) {
            if (document.getCreated().isAfter(request.getCreatedTo())) return false;
        }

        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder(toBuilder = true)
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}