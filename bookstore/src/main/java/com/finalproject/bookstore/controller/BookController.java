package com.finalproject.bookstore.controller;


import com.finalproject.bookstore.model.Author;
import com.finalproject.bookstore.model.Book;
import com.finalproject.bookstore.repository.AuthorRepository;
import com.finalproject.bookstore.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookController(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    // -------------------------
    // GET: List all books (with filters)
    // -------------------------
    @Operation(
            summary = "Get list of books",
            description = "Returns paginated list of books. Supports optional filtering by authorId and title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Book.class)))
    })
    @GetMapping
    public Page<Book> list(
            @Parameter(description = "Filter by author ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "Filter by partial title match") @RequestParam(required = false) String title,
            @Parameter(description = "Pagination and sorting parameters") Pageable pageable) {

        if (authorId == null && (title == null || title.isBlank())) {
            return bookRepository.findAll(pageable);
        }
        return bookRepository.findByAuthorAndTitle(authorId, title, pageable);
    }

    // -------------------------
    // GET: Get book by ID
    // -------------------------
    @Operation(summary = "Get book by ID", description = "Fetch a specific book by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Book> get(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -------------------------
    // POST: Create new book
    // -------------------------
    @Operation(summary = "Create new book", description = "Add a new book to the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or author not found")
    })
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Book book) {
        Long authorId = (book.getAuthor() != null) ? book.getAuthor().getId() : null;
        if (authorId == null) {
            return ResponseEntity.badRequest().body("author.id is required");
        }

        Optional<Author> authorOpt = authorRepository.findById(authorId);
        if (authorOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Author not found");
        }

        book.setAuthor(authorOpt.get());
        Book saved = bookRepository.save(book);
        return ResponseEntity.created(URI.create("/api/books/" + saved.getId())).body(saved);
    }

    // -------------------------
    // PUT: Update existing book
    // -------------------------
    @Operation(summary = "Update book", description = "Update details of an existing book.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Book book) {
        return bookRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(book.getTitle());
                    existing.setIsbn(book.getIsbn());
                    existing.setPublishedDate(book.getPublishedDate());

                    if (book.getAuthor() != null && book.getAuthor().getId() != null) {
                        authorRepository.findById(book.getAuthor().getId())
                                .ifPresent(existing::setAuthor);
                    }

                    Book updated = bookRepository.save(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // -------------------------
    // DELETE: Delete book by ID
    // -------------------------
    @Operation(summary = "Delete book", description = "Delete a book by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return bookRepository.findById(id)
                .map(book -> {
                    bookRepository.delete(book);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
